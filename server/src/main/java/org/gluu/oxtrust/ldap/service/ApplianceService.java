/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.persist.PersistenceEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.SmtpConfiguration;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * GluuConfiguration service
 * 
 * @author Reda Zerrad Date: 08.10.2012
 */
@Stateless
@Named("configurationService")
public class ConfigurationService implements Serializable {

	private static final long serialVersionUID = 8842838732456296435L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private EncryptionService encryptionService;

	public boolean contains(String configurationDn) {
		return ldapEntryManager.contains(GluuConfiguration.class, configurationDn);
	}

	/**
	 * Add new configuration
	 * 
	 * @param configuration
	 *            Configuration
	 */
	public void addConfiguration(GluuConfiguration configuration) {
		ldapEntryManager.persist(configuration);
	}

	/**
	 * Update configuration entry
	 * 
	 * @param configuration
	 *            GluuConfiguration
	 */
	public void updateConfiguration(GluuConfiguration configuration) {
		ldapEntryManager.merge(configuration);
	}

	/**
	 * Check if LDAP server contains configuration with specified attributes
	 * 
	 * @return True if configuration with specified attributes exist
	 */
	public boolean containsConfiguration(GluuConfiguration configuration) {
		return ldapEntryManager.contains(configuration);
	}

	/**
	 * Get configuration by inum
	 * 
	 * @param inum
	 *            Configuration Inum
	 * @return Configuration
	 * @throws Exception
	 */
	public GluuConfiguration getConfigurationByInum(String inum) {
		return ldapEntryManager.find(GluuConfiguration.class, getDnForConfiguration(inum));
	}

	/**
	 * Get configuration
	 * 
	 * @return Configuration
	 * @throws Exception
	 */
	public GluuConfiguration getConfiguration(String[] returnAttributes) {
		GluuConfiguration result = null;
		if (ldapEntryManager.contains(GluuConfiguration.class, getDnForConfiguration(getConfigurationInum()))) {
			result = ldapEntryManager.find(GluuConfiguration.class, getDnForConfiguration(getConfigurationInum()),
					returnAttributes);
		} else {
			result = new GluuConfiguration();
			result.setInum(getConfigurationInum());
			result.setDn(getDnForConfiguration(getConfigurationInum()));

			ldapEntryManager.persist(result);
		}
		return result;
	}

	/**
	 * Get configuration
	 * 
	 * @return Configuration
	 * @throws Exception
	 */
	public GluuConfiguration getConfiguration() {
		return getConfiguration(null);
	}

	/**
	 * Get all configurations
	 * 
	 * @return List of attributes
	 * @throws Exception
	 */
	public List<GluuConfiguration> getConfigurations() {
		return ldapEntryManager.findEntries(getDnForConfiguration(null), GluuConfiguration.class, null);
	}

	/**
	 * Build DN string for configuration
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified configuration or DN for configurations branch if inum
	 *         is null
	 * @throws Exception
	 */
	public String getDnForConfiguration(String inum) {
		String baseDn = organizationService.getBaseDn();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=configurations,%s", baseDn);
		}
		return String.format("inum=%s,ou=configurations,%s", inum, baseDn);
	}

	/**
	 * Build DN string for configuration
	 * 
	 * @return DN string for configuration
	 * @throws Exception
	 */
	public String getDnForConfiguration() {
		return getDnForConfiguration(getConfigurationInum());
	}

	public String getConfigurationInum() {
		return appConfiguration.getConfigurationInum();
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
		return new AuthenticationScriptUsageType[] { AuthenticationScriptUsageType.INTERACTIVE,
				AuthenticationScriptUsageType.SERVICE, AuthenticationScriptUsageType.BOTH };
	}

	public ProgrammingLanguage[] getProgrammingLanguages() {
		return new ProgrammingLanguage[] { ProgrammingLanguage.PYTHON };
	}

	public ScriptLocationType[] getLocationTypes() {
		return new ScriptLocationType[] { ScriptLocationType.LDAP, ScriptLocationType.FILE };
	}

	public CustomScriptType[] getCustomScriptTypes() {
		return new CustomScriptType[] { CustomScriptType.PERSON_AUTHENTICATION, CustomScriptType.CONSENT_GATHERING,
				CustomScriptType.UPDATE_USER, CustomScriptType.USER_REGISTRATION, CustomScriptType.CLIENT_REGISTRATION,
				CustomScriptType.DYNAMIC_SCOPE, CustomScriptType.ID_GENERATOR, CustomScriptType.CACHE_REFRESH,
				CustomScriptType.UMA_RPT_POLICY, CustomScriptType.UMA_CLAIMS_GATHERING, CustomScriptType.INTROSPECTION,
				CustomScriptType.RESOURCE_OWNER_PASSWORD_CREDENTIALS, CustomScriptType.APPLICATION_SESSION,
				CustomScriptType.SCIM };
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
