/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.conf;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.ldap.impl.LdapEntryManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.util.StringHelper;
import org.xdi.util.exception.ConfigurationException;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

/**
 * Base OpenId configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 11/02/2015
 */
public abstract class Configuration<C extends AppConfiguration, L extends LdapAppConfiguration> {

	private final Logger logger = LoggerFactory.getLogger(Configuration.class);

	static {
        if (System.getProperty("gluu.base") != null) {
            BASE_DIR = System.getProperty("gluu.base");
        } else if ((System.getProperty("catalina.base") != null) && (System.getProperty("catalina.base.ignore") == null)) {
			BASE_DIR = System.getProperty("catalina.base");
		} else if (System.getProperty("catalina.home") != null) {
			BASE_DIR = System.getProperty("catalina.home");
		} else if (System.getProperty("jboss.home.dir") != null) {
			BASE_DIR = System.getProperty("jboss.home.dir");
		} else {
			BASE_DIR = null;
		}
	}

	private static final String BASE_DIR;
	private static final String DIR = BASE_DIR + File.separator + "conf" + File.separator;

	private static final String SALT_FILE_NAME = "salt";

	private String confDir;
	private String saltFilePath;

	private FileConfiguration ldapConfiguration;
	private C appConfiguration;

	private String cryptoConfigurationSalt;

	private LdapEntryManager ldapEntryManager;

	@SuppressWarnings("unused")
	private long ldapFileLastModifiedTime;

	private AtomicBoolean isActive;

	protected Configuration() {
		this.isActive = new AtomicBoolean(true);
		try {
			create();
		} finally {
			this.isActive.set(false);
		}
	}

	private void create() {
		this.ldapConfiguration = loadLdapConfiguration();

		this.confDir = confDir();
		this.saltFilePath = confDir + SALT_FILE_NAME;

		this.cryptoConfigurationSalt = loadCryptoConfigurationSalt();

		this.ldapEntryManager = createLdapEntryManager();

		if (!createFromLdap()) {
			logger.error("Failed to load configuration from Ldap. Please fix it!!!.");
			throw new ConfigurationException("Failed to load configuration from Ldap.");
		} else {
			logger.info("Configuration loaded successfully.");
		}
	}

	public void destroy() {
		if (this.ldapEntryManager != null) {
			destroyLdapEntryManager(this.ldapEntryManager);
		}
	}

	private FileConfiguration loadLdapConfiguration() {
		String ldapConfigurationFileName = getLdapConfigurationFileName();
		FileConfiguration fileConfiguration = loadLdapConfiguration(ldapConfigurationFileName, false);
		if (fileConfiguration == null) {
			ldapConfigurationFileName = getDefaultLdapConfigurationFileName();
			fileConfiguration = loadLdapConfiguration(ldapConfigurationFileName, true);
		}
		
		return fileConfiguration;
	}

	public FileConfiguration loadLdapConfiguration(String ldapConfigurationFileName, boolean mandatory) {
		try {
			if (StringHelper.isEmpty(ldapConfigurationFileName)) {
				if (mandatory) {
					throw new ConfigurationException("Failed to load Ldap configuration file!");
				} else {
					return null;
				}
			}

			String ldapConfigurationFilePath = DIR + ldapConfigurationFileName;

			FileConfiguration ldapConfiguration = new FileConfiguration(ldapConfigurationFilePath);
			if (ldapConfiguration.isLoaded()) {
				File ldapFile = new File(ldapConfigurationFilePath);
				if (ldapFile.exists()) {
					this.ldapFileLastModifiedTime = ldapFile.lastModified();
				}
	
				return ldapConfiguration;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new ConfigurationException("Failed to load Ldap configuration from " + ldapConfigurationFileName, ex);
		}

		if (mandatory) {
			throw new ConfigurationException("Failed to load Ldap configuration from " + ldapConfigurationFileName);
		}
		
		return null;
	}

	private String loadCryptoConfigurationSalt() {
		try {
			FileConfiguration cryptoConfiguration = new FileConfiguration(this.saltFilePath);

			return cryptoConfiguration.getString("encodeSalt");
		} catch (Exception ex) {
			logger.error("Failed to load configuration from {}", saltFilePath, ex);
			throw new ConfigurationException("Failed to load configuration from " + saltFilePath, ex);
		}
	}

	private String confDir() {
		final String confDir = getLdapConfiguration().getString("confDir");
		if (StringUtils.isNotBlank(confDir)) {
			return confDir;
		}

		return DIR;
	}

	private boolean createFromLdap() {
		logger.info("Loading configuration from Ldap...");
		try {
			final L ldapConf = loadConfigurationFromLdap();
			if (ldapConf != null) {
				this.appConfiguration = (C) ldapConf.getApplication();
				return true;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return false;
	}

	private L loadConfigurationFromLdap(String... returnAttributes) {
		try {
			final String dn = getLdapConfiguration().getString(getApplicationConfigurationPropertyName());

			final L ldapConf = this.ldapEntryManager.find(getAppConfigurationType(), dn, returnAttributes);
			return ldapConf;
		} catch (BaseMappingException ex) {
			logger.error(ex.getMessage());
		}

		return null;
	}

	private LdapEntryManager createLdapEntryManager() {
		Properties connectionProperties = (Properties) this.ldapConfiguration.getProperties();
		Properties decryptedConnectionProperties = PropertiesDecrypter.decryptProperties(connectionProperties, this.cryptoConfigurationSalt);
		
		LdapEntryManagerFactory ldapEntryManagerFactory = new LdapEntryManagerFactory();
		LdapEntryManager ldapEntryManager = ldapEntryManagerFactory.createEntryManager(decryptedConnectionProperties);

		logger.debug("Created LdapEntryManager: {}", ldapEntryManager);

		return ldapEntryManager;
	}

	private void destroyLdapEntryManager(final LdapEntryManager ldapEntryManager) {
		boolean result = ldapEntryManager.destroy();
		if (result) {
			logger.debug("Destoyed LdapEntryManager: {}", ldapEntryManager);
		} else {
			logger.error("Failed to destoy LdapEntryManager: {}", ldapEntryManager);
		}
	}

	public FileConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}

	public String getCryptoConfigurationSalt() {
		return cryptoConfigurationSalt;
	}

	protected String getDefaultLdapConfigurationFileName() {
		return "ox-ldap.properties";
	}

	public C getAppConfiguration() {
		return appConfiguration;
	}

	protected abstract String getLdapConfigurationFileName();

	protected abstract Class<L> getAppConfigurationType();

	protected abstract String getApplicationConfigurationPropertyName();

}
