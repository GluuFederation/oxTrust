/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.util.List;

import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.util.StringHelper;

/**
 * GluuAppliance service
 * 
 * @author Reda Zerrad Date: 08.10.2012
 */
@Scope(ScopeType.STATELESS)
@Name("applianceService")
@AutoCreate
public class ApplianceService {

	@Logger
	private Log log;

	@In
	private LdapEntryManager ldapEntryManager;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	public boolean contains(String applianceDn) {
		return ldapEntryManager.contains(GluuAppliance.class, applianceDn);
	}

	/**
	 * Add new appliance
	 * 
	 * @param appliance
	 *            Appliance
	 */
	public void addAppliance(GluuAppliance appliance) {
		ldapEntryManager.persist(appliance);

	}

	/**
	 * Update appliance entry
	 * 
	 * @param appliance
	 *            GluuAppliance
	 */
	public void updateAppliance(GluuAppliance appliance) {
		ldapEntryManager.merge(appliance);

	}

	/**
	 * Check if LDAP server contains appliance with specified attributes
	 * 
	 * @return True if appliance with specified attributes exist
	 */
	public boolean containsAppliance(GluuAppliance appliance) {
		boolean result = ldapEntryManager.contains(appliance);

		return result;
	}

	/**
	 * Get appliance by inum
	 * 
	 * @param inum
	 *            Appliance Inum
	 * @return Appliance
	 * @throws Exception
	 */
	public GluuAppliance getApplianceByInum(String inum) {
		GluuAppliance result = ldapEntryManager.find(GluuAppliance.class, getDnForAppliance(inum));

		return result;

	}

	/**
	 * Get appliance
	 * 
	 * @return Appliance
	 * @throws Exception
	 */
	public GluuAppliance getAppliance(String[] returnAttributes) {
		GluuAppliance result = null;
		if(ldapEntryManager.contains(GluuAppliance.class, getDnForAppliance(getApplianceInum()))){
			result = ldapEntryManager.find(GluuAppliance.class, getDnForAppliance(getApplianceInum()), returnAttributes);
		}else{
			result = new GluuAppliance();
			result.setInum(getApplianceInum());
			result.setDn(getDnForAppliance(getApplianceInum()));

			ldapEntryManager.persist(result);
			
		}
		return result;

	}

	/**
	 * Get appliance
	 * 
	 * @return Appliance
	 * @throws Exception
	 */
	public GluuAppliance getAppliance() {
		return getAppliance(null);

	}
	
	/**
	 * Get all appliances
	 * 
	 * @return List of attributes
	 * @throws Exception
	 */
	public List<GluuAppliance> getAppliances() throws Exception {
		List<GluuAppliance> applianceList = ldapEntryManager.findEntries(getDnForAppliance(null), GluuAppliance.class, null);

		return applianceList;
	}

	/**
	 * Build DN string for appliance
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified appliance or DN for appliances branch if
	 *         inum is null
	 * @throws Exception
	 */
	public String getDnForAppliance(String inum) {
		String baseDn = OrganizationService.instance().getBaseDn();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=appliances,%s", baseDn);
		}

		return String.format("inum=%s,ou=appliances,%s", inum, baseDn);
	}

	/**
	 * Build DN string for appliance
	 * 
	 * @return DN string for appliance
	 * @throws Exception
	 */
	public String getDnForAppliance() {
		return getDnForAppliance(getApplianceInum());
	}

	public String getApplianceInum() {
		return applicationConfiguration.getApplianceInum();
	}

	/**
	 * Get applianceService instance
	 * 
	 * @return ApplianceService instance
	 */
	public static ApplianceService instance() {
		return (ApplianceService) Component.getInstance(ApplianceService.class);
	}

	/**
	 * Restarts services using puppet trigger file.
	 * 
	 * @author �Oleksiy Tataryn�
	 */
	public void restartServices() {
		String triggerFileName = applicationConfiguration.getServicesRestartTrigger();
		if (StringHelper.isNotEmpty(triggerFileName)) {
			log.info("Removing " + triggerFileName);
			File triggerFile = new File(triggerFileName);
			if (triggerFile.isFile()) {

				log.debug("Result of deletion is : " + triggerFile.delete());
			} else {
				log.error(triggerFileName + " does not exist or not file");
			}
		}
	}

	public AuthenticationScriptUsageType[] getScriptUsageTypes() {
		return new AuthenticationScriptUsageType[] { AuthenticationScriptUsageType.INTERACTIVE, AuthenticationScriptUsageType.SERVICE,
				AuthenticationScriptUsageType.BOTH, AuthenticationScriptUsageType.LOGOUT };
	}

	public ProgrammingLanguage[] getProgrammingLanguages() {
		return new ProgrammingLanguage[] { ProgrammingLanguage.PYTHON };
	}

	public CustomScriptType[] getCustomScriptTypes() {
		return new CustomScriptType[] { CustomScriptType.CUSTOM_AUTHENTICATION, CustomScriptType.CLIENT_REGISTRATION,
				CustomScriptType.USER_REGISTRATION, CustomScriptType.CACHE_REFRESH };
	}

}
