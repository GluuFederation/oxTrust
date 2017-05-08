/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import org.jboss.seam.annotations.Create;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;

import javax.faces.application.FacesMessage;import org.jboss.seam.annotations.Observer;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

@ApplicationScoped
@Named("statusCheckerDaily")
public class StatusCheckerDaily {

    private final static String EVENT_TYPE = "StatusCheckerDailyTimerEvent";
	// Group count and person count will now be checked daily
	public static final long STATUS_CHECKER_DAILY = (long) (1000L * 60 * 60 * 24);

	@Inject
	private Logger log;

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

	@PostConstruct
	public void create() {
		// Initialization Code
	}

    @Observer("org.jboss.seam.postInitialization")
    public void init() {
        log.info("Initializing daily status checker timer");
        this.isActive = new AtomicBoolean(false);

        Events.instance().raiseTimedEvent(EVENT_TYPE, new TimerSchedule(60 * 1000L, STATUS_CHECKER_DAILY));
    }

    @Observer(EVENT_TYPE)
    @Asynchronous
    public void process() {
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
		AppConfiguration appConfiguration = configurationFactory.getappConfiguration();
		if (!appConfiguration.isUpdateApplianceStatus()) {
			return;
		}

		GluuAppliance appliance;
		try {
			appliance = applianceService.getAppliance();
		} catch (LdapMappingException ex) {
			log.error("Failed to load current appliance", ex);
			return;
		}

		// Set LDAP attributes
		setLdapAttributes(appliance);

    	Date currentDateTime = new Date();
		appliance.setLastUpdate(currentDateTime);

		try {
			applianceService.updateAppliance(appliance);
		} catch (LdapMappingException ex) {
			log.error("Failed to update current appliance", ex);
			return;
		}

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
			} catch (LdapMappingException ex) {
				log.error("Failed to update appliance at central server", ex);        
				return;
			}
		}

		log.debug("Daily Appliance status update finished");
	}

	private void setLdapAttributes(GluuAppliance appliance) {
		log.debug("Setting ldap attributes");
		int groupCount = groupService.countGroups();
		int personCount = personService.countPersons();

		appliance.setGroupCount(String.valueOf(groupCount));
		appliance.setPersonCount(String.valueOf(personCount)); 
		appliance.setGluuDSStatus(Boolean.toString(groupCount > 0 && personCount > 0));
	}

	private String toIntString(Number number) {
		return (number == null) ? null : String.valueOf(number.intValue());
	}
}
