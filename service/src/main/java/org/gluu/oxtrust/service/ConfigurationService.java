/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.model.ApplicationType;
import org.gluu.model.AuthenticationScriptUsageType;
import org.gluu.model.ProgrammingLanguage;
import org.gluu.model.ScriptLocationType;
import org.gluu.model.SmtpConfiguration;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

/**
 * GluuConfiguration service
 * 
 * @author Reda Zerrad Date: 08.10.2012
 */
@ApplicationScoped
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
	private EncryptionService encryptionService;

	private static final SimpleDateFormat PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMM");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public boolean contains(String configurationDn) {
		return ldapEntryManager.contains(configurationDn, GluuConfiguration.class);
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
		try {
			ldapEntryManager.merge(configuration);
		} catch (Exception e) {
			log.info("===============================Error Configuragtion");
			log.info("", e);
		}
	}

	public void updateOxtrustStat(GluuOxTrustStat oxTrustStat) {
		try {
			ldapEntryManager.merge(oxTrustStat);
		} catch (Exception e) {
			log.info("===============================Error");
			log.info("", e);
		}

	}

	/**
	 * Check if LDAP server contains configuration with specified attributes
	 * 
	 * @return True if configuration with specified attributes exist
	 */
	public boolean containsConfiguration(String dn) {
		return ldapEntryManager.contains(dn, GluuConfiguration.class);
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
		return ldapEntryManager.find(GluuConfiguration.class, getDnForConfiguration());
	}

	/**
	 * Get configuration
	 * 
	 * @return Configuration
	 * @throws Exception
	 */
	public GluuConfiguration getConfiguration(String[] returnAttributes) {
		GluuConfiguration result = null;
		result = ldapEntryManager.find(getDnForConfiguration(), GluuConfiguration.class, returnAttributes);
		return result;
	}

	public GluuOxTrustStat getOxtrustStat(String[] returnAttributes) {
		GluuOxTrustStat result = null;
		if (ldapEntryManager.contains(getDnForOxtrustStat(), GluuOxTrustStat.class)) {
			result = ldapEntryManager.find(getDnForOxtrustStat(), GluuOxTrustStat.class, returnAttributes);
		} else {
			result = new GluuOxTrustStat();
			result.setDn(getDnForOxtrustStat());
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

	public GluuOxTrustStat getOxtrustStat() {
		return getOxtrustStat(null);
	}

	/**
	 * Get all configurations
	 * 
	 * @return List of attributes
	 * @throws Exception
	 */
	public List<GluuConfiguration> getConfigurations() {
		return ldapEntryManager.findEntries(getDnForConfiguration(), GluuConfiguration.class, null);
	}

	/**
	 * Build DN string for configuration
	 * 
	 * @return DN string for specified configuration or DN for configurations branch
	 *         if inum is null
	 * @throws Exception
	 */
	public String getDnForConfiguration() {
		String baseDn = organizationService.getBaseDn();
		return String.format("ou=configuration,%s", baseDn);
	}

	public String getDnForOxtrustStat() {
		return buildDn(LocalDateTime.now().format(formatter), new Date(), ApplicationType.OX_TRUST);
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
				CustomScriptType.END_SESSION, CustomScriptType.SCIM };
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
	
	
	public String getVersion() {
		String version  = getClass().getPackage().getImplementationVersion();
    	if (version==null) {
    	    Properties prop = new Properties();
    	    try (InputStream is = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
    	        prop.load(is);
    	        version = prop.getProperty("Implementation-Version");
    	    } catch (IOException e) {
    	        log.error(e.toString());
    	    }
    	}
    	log.info("Starting App version "+version);
    	if(version != null){
    		version = version.replace("-SNAPSHOT","");
    		return version;
    	}    		
       	return "";
	}

	private String buildDn(String uniqueIdentifier, Date creationDate, ApplicationType applicationType) {
		final StringBuilder dn = new StringBuilder();
		if (StringHelper.isNotEmpty(uniqueIdentifier) && (creationDate != null) && (applicationType != null)) {
			dn.append(String.format("uniqueIdentifier=%s,", uniqueIdentifier));
		}
		if ((creationDate != null) && (applicationType != null)) {
			dn.append(String.format("ou=%s,", PERIOD_DATE_FORMAT.format(creationDate)));
		}
		if (applicationType != null) {
			dn.append(String.format("ou=%s,", applicationType.getValue()));
		}
		dn.append("ou=statistic,o=metric");
		return dn.toString();
	}

}
