/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.config;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshConfiguration;
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
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.ApplicationConfigurationFile;
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

    public final static String LDAP_CONFIGUARION_RELOAD_EVENT_TYPE = "LDAP_CONFIGUARION_RELOAD";
    public final static String LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE = "LDAP_CENTRAL_CONFIGUARION_RELOAD";

    public static final String EVENT_UPDATE_CONFIGURATION = "UpdateAppConfigurationEvent";
    private final static String EVENT_TYPE = "OxTrustConfigurationTimerEvent";

	private final static int DEFAULT_INTERVAL = 30; // 30 seconds

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

    private static final String CONFIG_RELOAD_MARKER_FILE_PATH = DIR + "oxtrust.config.reload";

	public static final String LDAP_PROPERTIES_FILE = DIR + "oxTrustLdap.properties";
	public static final String LDAP_CENTRAL_PROPERTIES_FILE = DIR + "oxTrustCentralLdap.properties";

	public static final String APPLICATION_CONFIGURATION = "oxTrust.properties";
	public static final String CACHE_PROPERTIES_FILE = "oxTrustCacheRefresh.properties";
	public static final String LOG_ROTATION_CONFIGURATION = "oxTrustLogRotationConfiguration.xml";
	public static final String SALT_FILE_NAME = "salt";

	@Logger
	private Log log;

	@In
	private JsonService jsonService;

	private String confDir, configFilePath, cacheRefreshFilePath, logRotationFilePath, saltFilePath;

	private FileConfiguration ldapConfiguration;
	private FileConfiguration ldapCentralConfiguration;
	private ApplicationConfiguration applicationConfiguration;
	private CacheRefreshConfiguration cacheRefreshConfiguration;
	private CryptoConfigurationFile cryptoConfiguration;
	
	private LdapOxTrustConfiguration ldapOxTrustConfig;

    private AtomicBoolean isActive;

    private long ldapFileLastModifiedTime = -1;
    private long ldapCentralFileLastModifiedTime = -1;
    private long confFileLastModifiedTime = -1;
    private long cacheRefreshFileLastModifiedTime = -1;

	@Create
	public void init() {
		log.info("Creating oxTrustConfiguration");
		this.ldapConfiguration = createFileConfiguration(LDAP_PROPERTIES_FILE);
		this.ldapCentralConfiguration = createFileConfiguration(LDAP_CENTRAL_PROPERTIES_FILE, false);
    	this.confDir = confDir();

    	this.configFilePath = confDir + APPLICATION_CONFIGURATION;
    	this.cacheRefreshFilePath = confDir + CACHE_PROPERTIES_FILE;
    	this.logRotationFilePath = confDir + LOG_ROTATION_CONFIGURATION;
    	this.saltFilePath = confDir + SALT_FILE_NAME;
    	
    	loadCryptoConfigurationFromFile();
	}

	public void create() {
		createConfigurationFromLdap(true);
		reloadCacheRefreshConfigurationFromFile();
    	determineConfigurationLastModificationTime();
	}

    @Observer("org.jboss.seam.postInitialization")
    public void initReloadTimer() {
        this.isActive = new AtomicBoolean(false);

        final long delayBeforeFirstRun = 60 * 1000L;
        Events.instance().raiseTimedEvent(EVENT_TYPE, new TimerSchedule(delayBeforeFirstRun, DEFAULT_INTERVAL * 1000L));
    }

    @Observer(EVENT_TYPE)
    @Asynchronous
    public void reloadConfigurationTimerEvent() {
        if (this.isActive.get()) {
            return;
        }

        if (!this.isActive.compareAndSet(false, true)) {
            return;
        }

        try {
            reloadConfiguration();
        } catch (Throwable ex) {
            log.error("Exception happened while reloading application configuration", ex);
        } finally {
            this.isActive.set(false);
        }
    }

    private void reloadConfiguration() {
        File reloadMarker = new File(CONFIG_RELOAD_MARKER_FILE_PATH);

        if (reloadMarker.exists()) {
            boolean isAnyChanged = false;

            File ldapFile = new File(LDAP_PROPERTIES_FILE);
            File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
            File configFile = new File(configFilePath);
            File cacheRefreshFile = new File(cacheRefreshFilePath);

            if (configFile.exists()) {
                final long lastModified = configFile.lastModified();
                if (lastModified > confFileLastModifiedTime) { // reload configuration only if it was modified
                	reloadConfigurationFromFile();
                    confFileLastModifiedTime = lastModified;
                    isAnyChanged = true;
                }
            }

            if (cacheRefreshFile.exists()) {
                final long lastModified = cacheRefreshFile.lastModified();
                if (lastModified > cacheRefreshFileLastModifiedTime) { // reload configuration only if it was modified
                	reloadCacheRefreshConfigurationFromFile();
                	cacheRefreshFileLastModifiedTime = lastModified;
                	// We don't store this configuration in LDAP yet
//                    isAnyChanged = true;
                }
            }

            if (isAnyChanged) {
                persistToLdap();
            }

            // Reload LDAP configuration after persisting configuration updates
            if (ldapFile.exists()) {
                final long lastModified = ldapFile.lastModified();
                if (lastModified > ldapFileLastModifiedTime) { // reload configuration only if it was modified
                	this.ldapConfiguration = createFileConfiguration(LDAP_PROPERTIES_FILE);
                	ldapFileLastModifiedTime = lastModified;
                    Events.instance().raiseAsynchronousEvent(LDAP_CONFIGUARION_RELOAD_EVENT_TYPE);
                    isAnyChanged = true;
                }
            }

            // Reload central LDAP configuration after persisting configuration updates
            if (ldapCentralFile.exists()) {
                final long lastModified = ldapCentralFile.lastModified();
                if (lastModified > ldapCentralFileLastModifiedTime) { // reload configuration only if it was modified
            		this.ldapCentralConfiguration = createFileConfiguration(LDAP_CENTRAL_PROPERTIES_FILE, false);
                	ldapCentralFileLastModifiedTime = lastModified;
                    Events.instance().raiseAsynchronousEvent(LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE);
                    isAnyChanged = true;
                }
            }
        }
    }

    private void determineConfigurationLastModificationTime() {
        File ldapFile = new File(LDAP_PROPERTIES_FILE);
        File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
        File configFile = new File(configFilePath);
        File cacheRefreshFile = new File(cacheRefreshFilePath);

		if (ldapFile.exists()) {
			this.ldapFileLastModifiedTime = ldapFile.lastModified();
		}

		if (ldapCentralFile.exists()) {
			this.ldapCentralFileLastModifiedTime = ldapCentralFile.lastModified();
		}

		if (configFile.exists()) {
			this.confFileLastModifiedTime = configFile.lastModified();
		}

		if (cacheRefreshFile.exists()) {
			this.cacheRefreshFileLastModifiedTime = cacheRefreshFile.lastModified();
		}
    }

	private boolean createConfigurationFromLdap(boolean recoverFromFiles) {
		log.info("Loading configuration from LDAP...");

		LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");

		String configurationDn = getConfigurationDn();
		ldapOxTrustConfig = load(ldapEntryManager, configurationDn);
		if (ldapOxTrustConfig != null) {
			initConfigurationFromLdap(ldapOxTrustConfig);
			return true;
		}

		if (recoverFromFiles) {
			log.warn("Unable to find configuration in LDAP, try to create configuration entry in LDAP... ");
			if (getLdapConfiguration().getBoolean("createLdapConfigurationEntryIfNotExist", false)) {
				reloadConfigurationFromFile();

				return persistToLdap();
			}
		}

		return false;
	}

    public String confDir() {
        final String confDir = getLdapConfiguration().getString("confDir");
        if (StringUtils.isNotBlank(confDir)) {
            return confDir;
        }

        return DIR;
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

	private void initConfigurationFromLdap(LdapOxTrustConfiguration conf) {
		try {
			log.debug("oxTrustConfig:"+conf.getApplication());
			this.applicationConfiguration = jsonService.jsonToObject(conf.getApplication(), ApplicationConfiguration.class);
			
		} catch (Exception ex) {
			log.error("Failed to initialize applicationConfiguration from JSON: {0}", ex, conf.getApplication());
			throw new ConfigurationException("Failed to initialize applicationConfiguration from JSON", ex);
		}
	}

	public boolean persistToLdap() {
		String configurationDn = getConfigurationDn();
		LdapOxTrustConfiguration conf = prepareLdapConfiguration(configurationDn);

		LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");
		try {
			ldapEntryManager.persist(conf);
			log.info("Configuration entry is created in LDAP");
			
			return true;
		} catch (LdapMappingException ex) {
			try {
				ldapEntryManager.merge(conf);
				log.info("Configuration entry is updated in LDAP");
				
				return true;
			} catch (LdapMappingException ex2) {
				log.error("Failed to save configuration in LDAP", ex2);
			}
		}
		
		return false;
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

	private void reloadConfigurationFromFile() {
		try {
			FileConfiguration fileConfiguration = createFileConfiguration(configFilePath);
	        if (fileConfiguration != null) {
	        	log.info("Reloaded configuration from file: " + configFilePath);
	        } else {
	        	log.error("Failed to load configuration from file: " + configFilePath);
	        	return;
	        }

	        ApplicationConfigurationFile applicationConfigurationFile = new ApplicationConfigurationFile(fileConfiguration);

			ApplicationConfiguration fileApplicationConfiguration = new ApplicationConfiguration();
			BeanUtilsBean2.getInstance().copyProperties(fileApplicationConfiguration, applicationConfigurationFile);
			this.applicationConfiguration = fileApplicationConfiguration;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, configFilePath);
			throw new ConfigurationException("Failed to load configuration from " + configFilePath, ex);
		}
	}

	private void reloadCacheRefreshConfigurationFromFile() {
		try {
			FileConfiguration fileConfiguration = createFileConfiguration(cacheRefreshFilePath);
	        if (fileConfiguration != null) {
	        	log.info("Reloaded configuration from file: " + cacheRefreshFilePath);
	        } else {
	        	log.error("Failed to load configuration from file: " + cacheRefreshFilePath);
	        	return;
	        }

	        CacheRefreshConfiguration cacheRefreshConfiguration = new CacheRefreshConfiguration(fileConfiguration);
			this.cacheRefreshConfiguration = cacheRefreshConfiguration;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, cacheRefreshFilePath);
			throw new ConfigurationException("Failed to load configuration from " + cacheRefreshFilePath, ex);
		}
	}

	public void loadCryptoConfigurationFromFile() {
		try {
			log.info("Creating cryptoConfiguration");
			FileConfiguration cryptoConfiguration = createFileConfiguration(this.saltFilePath);
			CryptoConfigurationFile cryptoConfigurationFile = new CryptoConfigurationFile(cryptoConfiguration);

			this.cryptoConfiguration = cryptoConfigurationFile;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, this.saltFilePath);
			throw new ConfigurationException("Failed to load configuration from " + this.saltFilePath, ex);
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

	public FileConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}
	
	public FileConfiguration getLdapCentralConfiguration() {
		return ldapCentralConfiguration;
	}

	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public CacheRefreshConfiguration getCacheRefreshConfiguration() {
		return cacheRefreshConfiguration;
	}

	public CryptoConfigurationFile getCryptoConfiguration() {
		return cryptoConfiguration;
	}

	public String getCacheRefreshFilePath() {
		return cacheRefreshFilePath;
	}

	public String getLogRotationFilePath() {
		return logRotationFilePath;
	}

	public String getConfigurationDn() {
		return getLdapConfiguration().getString("baseConfigurationDN");
	}

	public static OxTrustConfiguration instance() {
		return (OxTrustConfiguration) Component.getInstance(OxTrustConfiguration.class);
	}

}
