/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.config;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.ApplicationConfigurationFile;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.exception.ConfigurationException;
import org.xdi.service.JsonService;
import org.xdi.util.properties.FileConfiguration;

/**
 * @author Yuriy Movchan
 * @version 0.1, 05/15/2013
 */
@Scope(ScopeType.APPLICATION)
@Name("oxTrustConfiguration")
@Startup
public class OxTrustConfiguration {

	public static final String EVENT_INIT_CONFIGURATION = "InitAppConfigurationEvent";
	public static final String EVENT_UPDATE_CONFIGURATION = "UpdateAppConfigurationEvent";

	@Logger
	private Log log;

	@In
	private JsonService jsonService;

	public static final String CONFIGURATION_FILE_LOCAL_LDAP_PROPERTIES_FILE = "oxTrustLdap.properties";
	public static final String CONFIGURATION_FILE_CENTRAL_LDAP_PROPERTIES_FILE = "oxTrustCentralLdap.properties";
	public static final String CONFIGURATION_FILE_APPLICATION_CONFIGURATION = "oxTrust.properties";
	public static final String CONFIGURATION_FILE_CRYPTO_PROPERTIES_FILE = "salt";

	private FileConfiguration ldapConfiguration;
	private FileConfiguration ldapCentralConfiguration;
	private CryptoConfigurationFile cryptoConfiguration;
	private ApplicationConfiguration applicationConfiguration;

	@Create
	public void create() {
		this.ldapConfiguration = createFileConfiguration(CONFIGURATION_FILE_LOCAL_LDAP_PROPERTIES_FILE);
		this.ldapCentralConfiguration = createFileConfiguration(CONFIGURATION_FILE_CENTRAL_LDAP_PROPERTIES_FILE, false);
		createCryptoConfigurationFromFile();
	}

	@Observer(EVENT_INIT_CONFIGURATION)
	public void initConfigurations() {
		createConfigurationFromLdap(true);
	}

	@Observer(EVENT_UPDATE_CONFIGURATION)
	public void updateConfigurations() {
		createConfigurationFromLdap(false);
	}

	private FileConfiguration createFileConfiguration(String fileName) {
		return createFileConfiguration(fileName, true);
	}

	private FileConfiguration createFileConfiguration(String fileName, boolean isMandatory) {
		try {
			FileConfiguration fileConfiguration = new FileConfiguration(fileName);
			if (fileConfiguration.isLoaded()) {
				return fileConfiguration;
			}
		} catch (Exception ex) {
			if (isMandatory) {
				log.error("Failed to load configuration from {0}", ex, fileName);
				throw new ConfigurationException("Failed to load configuration from " + fileName, ex);
			}
		}

		return null;
	}

	private boolean createConfigurationFromLdap(boolean recoverFromFiles) {
		log.trace("Loading configuration from LDAP...");

		LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");

		String configurationDn = getDnForConfiguration();
		LdapOxTrustConfiguration conf = load(ldapEntryManager, configurationDn);
		if (conf != null) {
			initConfigurationFromLdap(conf);
			return true;
		}

		if (recoverFromFiles) {
			log.warn("Unable to find configuration in LDAP, try to create configuration entry in LDAP... ");
			if (getLdapConfiguration().getBoolean("createLdapConfigurationEntryIfNotExist", false)) {
				createConfigurationFromFile();

				LdapOxTrustConfiguration newConf = prepareLdapConfiguration(configurationDn);
				boolean result = persist(ldapEntryManager, newConf);
				return result;
			}
		}

		return false;
	}

	public LdapOxTrustConfiguration load(LdapEntryManager ldapEntryManager, String configurationDn) {
		try {
			LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn);

			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	public boolean persist(LdapEntryManager ldapEntryManager, LdapOxTrustConfiguration conf) {
		try {
			ldapEntryManager.persist(conf);
			log.info("Configuration entry is created in LDAP");
			
			return true;
		} catch (LdapMappingException ex) {
			log.error("Failed to save configuration in LDAP", ex);
		}
		
		return false;
	}

	private void initConfigurationFromLdap(LdapOxTrustConfiguration conf) {
		try {
			this.applicationConfiguration = jsonService.jsonToObject(conf.getApplication(), ApplicationConfiguration.class);
		} catch (Exception ex) {
			log.error("Failed to initialize applicationConfiguration from JSON: {0}", ex, conf.getApplication());
			throw new ConfigurationException("Failed to initialize applicationConfiguration from JSON", ex);
		}
	}

	private void createConfigurationFromFile() {
		try {
			FileConfiguration fileConfiguration = createFileConfiguration(CONFIGURATION_FILE_APPLICATION_CONFIGURATION);
			ApplicationConfigurationFile applicationConfigurationFile = new ApplicationConfigurationFile(fileConfiguration);

			ApplicationConfiguration fileApplicationConfiguration = new ApplicationConfiguration();
			BeanUtilsBean2.getInstance().copyProperties(fileApplicationConfiguration, applicationConfigurationFile);
			this.applicationConfiguration = fileApplicationConfiguration;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, CONFIGURATION_FILE_APPLICATION_CONFIGURATION);
			throw new ConfigurationException("Failed to load configuration from " + CONFIGURATION_FILE_APPLICATION_CONFIGURATION, ex);
		}
	}
	
	private void createCryptoConfigurationFromFile() {
		try {
			FileConfiguration cryptoConfiguration = createFileConfiguration(CONFIGURATION_FILE_CRYPTO_PROPERTIES_FILE);
			CryptoConfigurationFile cryptoConfigurationFile = new CryptoConfigurationFile(cryptoConfiguration);

			this.cryptoConfiguration = cryptoConfigurationFile;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, CONFIGURATION_FILE_CRYPTO_PROPERTIES_FILE);
			throw new ConfigurationException("Failed to load configuration from " + CONFIGURATION_FILE_CRYPTO_PROPERTIES_FILE, ex);
		}
	}

	private LdapOxTrustConfiguration prepareLdapConfiguration(String configurationDn) {
		LdapOxTrustConfiguration conf = new LdapOxTrustConfiguration();
		conf.setDn(configurationDn);
		try {
			conf.setApplication(jsonService.objectToJson(applicationConfiguration));
		} catch (Exception ex) {
			log.error("Failed to prepare LDAP configuration", ex);
			throw new ConfigurationException("Failed to prepare LDAP configuration", ex);
		}

		return conf;
	}

	private String getDnForConfiguration() {
		String baseDn = getLdapConfiguration().getString("baseConfigurationDN");
		return baseDn;
	}

	public FileConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}

	public CryptoConfigurationFile getCryptoConfiguration() {
		return cryptoConfiguration;
	}
	
	public FileConfiguration getLdapCentralConfiguration() {
		return ldapCentralConfiguration;
	}

	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public static OxTrustConfiguration instance() {
		return (OxTrustConfiguration) Component.getInstance(OxTrustConfiguration.class);
	}

}