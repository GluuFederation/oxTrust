/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.SmtpConfiguration;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * GluuAppliance service
 * 
 * @author Reda Zerrad Date: 08.10.2012
 */
@Stateless
@Named("applianceService")
public class ApplianceService implements Serializable {

	private static final long serialVersionUID = 8842838732456296435L;

	@Inject
	private Logger log;

	@Inject
	private LdapEntryManager ldapEntryManager;	
	@Inject
	private OrganizationService organizationService;

	@Inject
	private AppConfiguration appConfiguration;
	
	@Inject
	private EncryptionService encryptionService;

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
		String baseDn = organizationService.getBaseDn();
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
		return appConfiguration.getApplianceInum();
	}

	/**
	 * Restarts services using puppet trigger file.
	 * 
	 * @author �Oleksiy Tataryn�
	 */
	public void restartServices() {
		String triggerFileName = appConfiguration.getServicesRestartTrigger();
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
				AuthenticationScriptUsageType.BOTH };
	}

	public ProgrammingLanguage[] getProgrammingLanguages() {
		return new ProgrammingLanguage[] { ProgrammingLanguage.PYTHON };
	}

	public ScriptLocationType[] getLocationTypes() {
		return new ScriptLocationType[] { ScriptLocationType.LDAP,  ScriptLocationType.FILE };
	}

	public CustomScriptType[] getCustomScriptTypes() {
		return new CustomScriptType[] { CustomScriptType.PERSON_AUTHENTICATION, CustomScriptType.CONSENT_GATHERING, CustomScriptType.UPDATE_USER,
				CustomScriptType.USER_REGISTRATION, CustomScriptType.CLIENT_REGISTRATION, CustomScriptType.DYNAMIC_SCOPE, CustomScriptType.ID_GENERATOR,
				CustomScriptType.CACHE_REFRESH, CustomScriptType.UMA_RPT_POLICY, CustomScriptType.UMA_CLAIMS_GATHERING, CustomScriptType.APPLICATION_SESSION, CustomScriptType.SCIM };
	}

	public void encryptedSmtpPassword(SmtpConfiguration smtpConfiguration) {
		if (smtpConfiguration == null) {
			return;
		}

		String password = smtpConfiguration.getPasswordDecrypted();
		if (StringHelper.isNotEmpty(password)) {
			try {
				String encryptedPassword = encryptionService.encrypt(password);
				smtpConfiguration.setPassword(encryptedPassword);
			} catch (EncryptionException ex) {
				log.error("Failed to encrypt SMTP password", ex);
			}
		}
	}

	public void decryptSmtpPassword(SmtpConfiguration smtpConfiguration) {
		if (smtpConfiguration == null) {
			return;
		}

		String password = smtpConfiguration.getPassword();
		if (StringHelper.isNotEmpty(password)) {
			try {
				smtpConfiguration.setPasswordDecrypted(encryptionService.decrypt(password));
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt SMTP password", ex);
			}
		}
	}

}
