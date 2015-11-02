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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gluu.oxauth.client.AuthClient;
import org.gluu.oxauth.client.exception.ConfigurationException;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

/**
 * Base OpenId configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 11/02/2015
 */
public abstract class Configuration<C extends AppConfiguration> {

	protected final Log log = LogFactory.getLog(getClass());

	static {
		if ((System.getProperty("catalina.base") != null) && (System.getProperty("catalina.base.ignore") == null)) {
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

	// private static class ConfigurationSingleton {
	// static Configuration INSTANCE = new Configuration();
	// }

	// public static Configuration instance() {
	// return ConfigurationSingleton.INSTANCE;
	// }

	private String confDir;
	private String saltFilePath;

	private FileConfiguration ldapConfiguration;
	private C appConfiguration;
	private AuthClient authClient;

	private String cryptoConfigurationSalt;

	private long ldapFileLastModifiedTime;

	private AtomicBoolean isActive;

	private Configuration() {
		this.isActive = new AtomicBoolean(true);
		try {
			this.ldapConfiguration = loadLdapConfiguration();

			this.confDir = confDir();
			this.saltFilePath = confDir + SALT_FILE_NAME;

			this.cryptoConfigurationSalt = loadCryptoConfigurationSalt();

			if (!createFromLdap()) {
				log.error("Failed to load configuration from Ldap. Please fix it!!!.");
				throw new ConfigurationException("Failed to load configuration from Ldap.");
			} else {
				log.info("Configuration loaded successfully.");
			}

			this.authClient = createAuthClient();
		} finally {
			this.isActive.set(false);
		}
	}

	private FileConfiguration loadLdapConfiguration() {
		String ldapConfigurationFileName = getLdapConfigurationFileName();
		try {
			if (StringHelper.isEmpty(ldapConfigurationFileName)) {
				throw new ConfigurationException("Failed to load Ldap configuration file!");
			}

			String ldapConfigurationFilePath = DIR + ldapConfigurationFileName;

			FileConfiguration ldapConfiguration = new FileConfiguration(ldapConfigurationFilePath);

			File ldapFile = new File(ldapConfigurationFilePath);
			if (ldapFile.exists()) {
				this.ldapFileLastModifiedTime = ldapFile.lastModified();
			}

			return ldapConfiguration;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new ConfigurationException("Failed to load Ldap configuration from " + ldapConfigurationFileName, ex);
		}
	}

	private String loadCryptoConfigurationSalt() {
		try {
			FileConfiguration cryptoConfiguration = new FileConfiguration(this.saltFilePath);

			return cryptoConfiguration.getString("encodeSalt");
		} catch (Exception ex) {
			log.error("Failed to load configuration from " + saltFilePath, ex);
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
		log.info("Loading configuration from Ldap...");
		try {
			final C conf = loadConfigurationFromLdap();
			if (conf != null) {
				this.appConfiguration = initAppConfiguration(conf);
				return true;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	private C loadConfigurationFromLdap(String... returnAttributes) {
		final LdapEntryManager ldapEntryManager = createLdapEntryManager();
		try {
			final String dn = getLdapConfiguration().getString("configurationEntryDN");

			final C conf = ldapEntryManager.find(getAppConfigurationType(), dn, returnAttributes);
			return conf;
		} catch (LdapMappingException ex) {
			log.error(ex.getMessage());
		} finally {
			destroyLdapEntryManager(ldapEntryManager);
		}

		return null;
	}

	private LdapEntryManager createLdapEntryManager() {
		Properties connectionProperties = (Properties) this.ldapConfiguration.getProperties();
		Properties decryptedConnectionProperties = PropertiesDecrypter.decryptProperties(connectionProperties, this.cryptoConfigurationSalt);

		LDAPConnectionProvider connectionProvider = new LDAPConnectionProvider(decryptedConnectionProperties);
		LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider, null));

		log.debug("Created LdapEntryManager: " + ldapEntryManager);

		return ldapEntryManager;
	}

	private void destroyLdapEntryManager(final LdapEntryManager ldapEntryManager) {
		boolean result = ldapEntryManager.destroy();
		if (result) {
			log.debug("Destoyed LdapEntryManager: " + ldapEntryManager);
		} else {
			log.error("Failed to destoy LdapEntryManager: " + ldapEntryManager);
		}
	}

	private AuthClient createAuthClient() {
		AuthClient authClient = new AuthClient(this.appConfiguration.getOpenIdProviderUrl(), this.appConfiguration.getOpenIdClientId(),
				this.appConfiguration.getOpenIdClientPassword(), this.appConfiguration.getOpenIdClientScopes(), this.appConfiguration.getOpenIdProviderUrl());

		return authClient;
	}

	public FileConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}

	public String getCryptoConfigurationSalt() {
		return cryptoConfigurationSalt;
	}

	public AuthClient getAuthClient() {
		return authClient;
	}

	protected abstract String getLdapConfigurationFileName();

	protected abstract Class<C> getAppConfigurationType();

	protected abstract C initAppConfiguration(C appConfiguarion);

}
