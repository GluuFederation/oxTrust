/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("statusCheckerDaily")
public class StatusCheckerDaily {

    private final static String EVENT_TYPE = "StatusCheckerDailyTimerEvent";
	// Group count and person count will now be checked daily
	public static final long STATUS_CHECKER_DAILY = (long) (1000L * 60 * 60 * 24);

	@Logger
	private Log log;

	@In
	private ApplianceService applianceService;

	@In
	private IGroupService groupService;

	@In
	private IPersonService personService;

	@In
	private CentralLdapService centralLdapService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

    private AtomicBoolean isActive;

	@Create
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
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		if (!applicationConfiguration.isUpdateApplianceStatus()) {
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
