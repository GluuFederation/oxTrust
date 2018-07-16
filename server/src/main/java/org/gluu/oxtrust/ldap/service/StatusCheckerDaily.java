/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerDailyEvent;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.cdi.async.Asynchronous;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;

@ApplicationScoped
@Named("statusCheckerDaily")
public class StatusCheckerDaily {

	// Group count and person count will now be checked daily
	public static final int DEFAULT_INTERVAL = 60 * 60 * 24;

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private IGroupService groupService;

	@Inject
	private IPersonService personService;

	@Inject
	private CentralLdapService centralLdapService;

	@Inject
	private ConfigurationFactory configurationFactory;

    private AtomicBoolean isActive;

    public void initTimer() {
        log.info("Initializing Daily Status Cheker Timer");
        this.isActive = new AtomicBoolean(false);

		final int delay = 1 * 60;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new StatusCheckerDailyEvent(),
				Scheduled.Literal.INSTANCE));
    }

    @Asynchronous
    public void process(@Observes @Scheduled StatusCheckerDailyEvent statusCheckerDailyEvent) {
        if (this.isActive.get()) {
            return;
        }

        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }

        try {
            processInt();
        } finally {
            this.isActive.set(false);
        }
    }

	/**
	 * Gather periodically site and server status
	 * 
	 * @param when
	 *            Date
	 * @param interval
	 *            Interval
	 */
	private void processInt() {
		log.debug("Starting daily status checker");
		AppConfiguration appConfiguration = configurationFactory.getAppConfiguration();
		if (!appConfiguration.isUpdateApplianceStatus()) {
			return;
		}

        log.debug("Getting data from ldap");
        int groupCount = groupService.countGroups();
        int personCount = personService.countPersons();

		GluuAppliance appliance = applianceService.getAppliance();

        log.debug("Setting ldap attributes");
        appliance.setGroupCount(String.valueOf(groupCount));
        appliance.setPersonCount(String.valueOf(personCount)); 
        appliance.setGluuDSStatus(Boolean.toString(groupCount > 0 && personCount > 0));

    	Date currentDateTime = new Date();
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);

		if (centralLdapService.isUseCentralServer()) {
			try {
				GluuAppliance tmpAppliance = new GluuAppliance();
				tmpAppliance.setDn(appliance.getDn());
				boolean existAppliance = centralLdapService.containsAppliance(tmpAppliance);
	
				if (existAppliance) {
					centralLdapService.updateAppliance(appliance);
				} else {
					centralLdapService.addAppliance(appliance);
				}
			} catch (BasePersistenceException ex) {
				log.error("Failed to update appliance at central server", ex);        
				return;
			}
		}

		log.debug("Daily Appliance status update finished");
	}

}
