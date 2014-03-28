package org.gluu.oxtrust.ldap.service;

import java.util.Date;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("statusCheckerDaily")
public class StatusCheckerDaily {

	@Logger
	Log log;

	@In
	ApplianceService applianceService;

	@In
	GroupService groupService;

	@In
	PersonService personService;

	@In
	CentralLdapService centralLdapService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	@Create
	public void create() {
		// Initialization Code
	}

	@Asynchronous
	public QuartzTriggerHandle scheduleStatusChecking(@Expiration Date when, @IntervalDuration Long interval) {
		process(when, interval);
		return null;
	}

	/**
	 * Gather periodically site and server status
	 * 
	 * @param when
	 *            Date
	 * @param interval
	 *            Interval
	 */
	private void process(Date when, Long interval) {
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

		appliance.setLastUpdate(toIntString(System.currentTimeMillis() / 1000));

		try {
			applianceService.updateAppliance(appliance);
		} catch (LdapMappingException ex) {
			log.error("Failed to update current appliance", ex);
			return;
		}

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
