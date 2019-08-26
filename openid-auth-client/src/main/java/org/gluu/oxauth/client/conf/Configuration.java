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
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.persist.model.PersistenceConfiguration;
import org.gluu.persist.service.PersistanceFactoryService;
import org.gluu.persist.service.StandalonePersistanceFactoryService;
import org.gluu.util.StringHelper;
import org.gluu.util.exception.ConfigurationException;
import org.gluu.util.properties.FileConfiguration;
import org.gluu.util.security.PropertiesDecrypter;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base OpenId configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 11/02/2015
 */
public abstract class Configuration<C extends AppConfiguration, L extends LdapAppConfiguration> {

	private final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    static {
        if (System.getProperty("gluu.base") != null) {
            BASE_DIR = System.getProperty("gluu.base");
        } else if ((System.getProperty("catalina.base") != null)
                && (System.getProperty("catalina.base.ignore") == null)) {
            BASE_DIR = System.getProperty("catalina.base");
        } else if (System.getProperty("catalina.home") != null) {
            BASE_DIR = System.getProperty("catalina.home");
        } else if (System.getProperty("jboss.home.dir") != null) {
            BASE_DIR = System.getProperty("jboss.home.dir");
        } else {
            BASE_DIR = null;
        }
    }

    public static final String BASE_DIR;
    public static final String DIR = BASE_DIR + File.separator + "conf" + File.separator;

	private static final String BASE_PROPERTIES_FILE = DIR + "gluu.properties";
    public static final String DEFAULT_PROPERTIES_FILE = DIR + "openid.properties";

	private static final String SALT_FILE_NAME = "salt";

	private String confDir;
	private String saltFilePath;

	private FileConfiguration baseConfiguration;
	private C appConfiguration;

	private PersistanceFactoryService persistanceFactoryService;
	private PersistenceConfiguration persistenceConfiguration;

	private String cryptoConfigurationSalt;

	private PersistenceEntryManager ldapEntryManager;

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
		this.persistanceFactoryService = new StandalonePersistanceFactoryService();

        this.persistenceConfiguration = persistanceFactoryService.loadPersistenceConfiguration(getDefaultConfigurationFileName());
        this.baseConfiguration = loadBaseConfiguration();

		this.confDir = confDir();
		this.saltFilePath = confDir + SALT_FILE_NAME;

		this.cryptoConfigurationSalt = loadCryptoConfigurationSalt();

		this.ldapEntryManager = createPersistenceEntryManager();

		if (!createFromLdap()) {
			LOG.error("Failed to load configuration from Ldap. Please fix it!!!.");
			throw new ConfigurationException("Failed to load configuration from Ldap.");
		} else {
			LOG.info("Configuration loaded successfully.");
		}
	}

	public void destroy() {
		if (this.ldapEntryManager != null) {
			destroyLdapEntryManager(this.ldapEntryManager);
		}
	}

	private FileConfiguration loadBaseConfiguration() {
		FileConfiguration fileConfiguration = createFileConfiguration(BASE_PROPERTIES_FILE, true);
		
		return fileConfiguration;
	}

	private FileConfiguration createFileConfiguration(String fileName, boolean isMandatory) {
		try {
			FileConfiguration fileConfiguration = new FileConfiguration(fileName);

			return fileConfiguration;
		} catch (Exception ex) {
			if (isMandatory) {
				LOG.error("Failed to load configuration from {}", fileName, ex);
				throw new ConfigurationException("Failed to load configuration from " + fileName, ex);
			}
		}

		return null;
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
			LOG.error(ex.getMessage(), ex);
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
			LOG.error("Failed to load configuration from {}", saltFilePath, ex);
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
		LOG.info("Loading configuration from '{}' DB...", baseConfiguration.getString("persistence.type"));
		try {
			final L ldapConf = loadConfigurationFromLdap();
			if (ldapConf != null) {
				this.appConfiguration = (C) ldapConf.getApplication();
				return true;
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}

		return false;
	}

	private L loadConfigurationFromLdap(String... returnAttributes) {
		try {
			final String dn = baseConfiguration.getString(getApplicationConfigurationPropertyName());

			final L ldapConf = this.ldapEntryManager.find(dn, getAppConfigurationType(), returnAttributes);
			return ldapConf;
		} catch (BasePersistenceException ex) {
			LOG.error(ex.getMessage());
		}

		return null;
	}

	protected Properties preparePersistanceProperties() {
		FileConfiguration persistenceConfig = persistenceConfiguration.getConfiguration();
		Properties connectionProperties = (Properties) persistenceConfig.getProperties();

		Properties decryptedConnectionProperties;
		try {
			decryptedConnectionProperties = PropertiesDecrypter.decryptAllProperties(StringEncrypter.defaultInstance(), connectionProperties, this.cryptoConfigurationSalt);
        } catch (EncryptionException ex) {
        	throw new ConfigurationException("Failed to decript configuration properties", ex);
        }

		return decryptedConnectionProperties;
	}

	public PersistenceEntryManager createPersistenceEntryManager() {
		Properties connectionProperties = preparePersistanceProperties();

		PersistenceEntryManagerFactory persistenceEntryManagerFactory = persistanceFactoryService.getPersistenceEntryManagerFactory(persistenceConfiguration);
		PersistenceEntryManager persistenceEntryManager = persistenceEntryManagerFactory.createEntryManager(connectionProperties);
		LOG.info("Created PersistenceEntryManager: {} with operation service: {}",
				new Object[] {persistenceEntryManager,
						persistenceEntryManager.getOperationService() });

		return persistenceEntryManager;
	}

	private void destroyLdapEntryManager(final PersistenceEntryManager ldapEntryManager) {
		boolean result = ldapEntryManager.destroy();
		if (result) {
			LOG.debug("Destoyed LdapEntryManager: {}", ldapEntryManager);
		} else {
			LOG.error("Failed to destoy LdapEntryManager: {}", ldapEntryManager);
		}
	}

	public FileConfiguration getLdapConfiguration() {
		return this.persistenceConfiguration.getConfiguration();
	}

	public String getCryptoConfigurationSalt() {
		return cryptoConfigurationSalt;
	}

	protected String getDefaultLdapConfigurationFileName() {
		return "gluu-ldap.properties";
	}

	public C getAppConfiguration() {
		return appConfiguration;
	}

	protected abstract String getDefaultConfigurationFileName();

	protected abstract Class<L> getAppConfigurationType();

	protected abstract String getApplicationConfigurationPropertyName();

}
