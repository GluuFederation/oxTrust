/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.config;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.ImportPersonConfig;
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

    public static final String BASE_DIR;
    public static final String DIR = BASE_DIR + File.separator + "conf" + File.separator;

	public static final String LDAP_PROPERTIES_FILE = DIR + "oxtrust-ldap.properties";
	public static final String LDAP_CENTRAL_PROPERTIES_FILE = DIR + "oxtrust-central-ldap.properties";

	public static final String APPLICATION_CONFIGURATION = "oxtrust-config.json";
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
	private String cryptoConfigurationSalt;
	private ImportPersonConfig  importPersonConfig;	////issue 102  : changed by shekhar

    private AtomicBoolean isActive;

    private long ldapFileLastModifiedTime = -1;
    private long ldapCentralFileLastModifiedTime = -1;

    private long loadedRevision = -1;
    private boolean loadedFromLdap = true;
	
	@Create
	public void init() {
        this.isActive = new AtomicBoolean(true);
    	try {
			log.info("Creating oxTrustConfiguration");
			loadLdapConfiguration();
			loadLdapCentralConfiguration();
	    	this.confDir = confDir();
	
	    	this.configFilePath = confDir + APPLICATION_CONFIGURATION;
	    	this.cacheRefreshFilePath = confDir + CACHE_PROPERTIES_FILE;
	    	this.logRotationFilePath = confDir + LOG_ROTATION_CONFIGURATION;
	    	this.saltFilePath = confDir + SALT_FILE_NAME;
	    	
	    	loadCryptoConfigurationSalt();
    	} finally {
	    	this.isActive.set(false);
	    }
	}

	public void create() {
        if (!createFromLdap(true)) {
            log.error("Failed to load configuration from LDAP. Please fix it!!!.");
            throw new ConfigurationException("Failed to load configuration from LDAP.");
        } else {
        	log.info("Configuration loaded successfully.");
        }
	}

    @Observer("org.jboss.seam.postInitialization")
    public void initReloadTimer() {
        final long delayBeforeFirstRun = 30 * 1000L;
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
        // Reload LDAP configuration if needed
        File ldapFile = new File(LDAP_PROPERTIES_FILE);
        if (ldapFile.exists()) {
            final long lastModified = ldapFile.lastModified();
            if (lastModified > ldapFileLastModifiedTime) { // reload configuration only if it was modified
            	loadLdapConfiguration();
                Events.instance().raiseAsynchronousEvent(LDAP_CONFIGUARION_RELOAD_EVENT_TYPE);
            }
        }

        // Reload LDAP central configuration if needed
        File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
        if (ldapCentralFile.exists()) {
            final long lastModified = ldapCentralFile.lastModified();
            if (lastModified > ldapCentralFileLastModifiedTime) { // reload configuration only if it was modified
            	loadLdapCentralConfiguration();
                Events.instance().raiseAsynchronousEvent(LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE);
            }
        } else if (this.ldapCentralConfiguration != null) {
        	// Allow to remove not mandatory configuration file
    		this.ldapCentralConfiguration = null;
            Events.instance().raiseAsynchronousEvent(LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE);
        }

    	if (!loadedFromLdap) {
    		return;
    	}

    	final LdapOxTrustConfiguration conf = loadConfigurationFromLdap("oxRevision");
        if (conf == null) {
        	return;
        }

        if (conf.getRevision() <= this.loadedRevision) {
        	return;
        }

    	createFromLdap(false);
    }

	private boolean createFromLdap(boolean recoverFromFiles) {
		log.info("Loading configuration from LDAP...");
        try {
            final LdapOxTrustConfiguration conf = loadConfigurationFromLdap();
            if (conf != null) {
                init(conf);
                return true;
            }
        } catch (Exception ex) {
        	log.error(ex.getMessage(), ex);
        }

		if (recoverFromFiles) {
			log.warn("Unable to find configuration in LDAP, try to load configuration from file system... ");
			if (createFromFile()) {
				this.loadedFromLdap = false;
                return true;
			}
		}

		return false;
	}
    
    private LdapOxTrustConfiguration loadConfigurationFromLdap(String ... returnAttributes) {
    	final LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");
    	final String configurationDn = getConfigurationDn();
        try {
            final LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn, returnAttributes);

            return conf;
        } catch (LdapMappingException ex) {
            log.error("Failed to load configuration from LDAP", ex);
        }
        
        return null;
    }

    public String confDir() {
        final String confDir = getLdapConfiguration().getString("confDir");
        if (StringUtils.isNotBlank(confDir)) {
            return confDir;
        }

        return DIR;
    }

	private void init(LdapOxTrustConfiguration conf) {
		this.applicationConfiguration = conf.getApplication();
		this.cacheRefreshConfiguration = conf.getCacheRefresh();
		this.loadedRevision = conf.getRevision();
		this.importPersonConfig = conf.getImportPersonConfig();//issue 102   : changed by shekhar
	}

	private boolean createFromFile() {
    	boolean result = reloadAppConfFromFile();
    	
    	return result;
    }

    private boolean reloadAppConfFromFile() {
        final ApplicationConfiguration applicationConfiguration = loadAppConfFromFile();
        if (applicationConfiguration != null) {
            log.info("Reloaded application configuration from file: " + configFilePath);
            this.applicationConfiguration = applicationConfiguration;
            return true;
        } else {
        	log.error("Failed to load application configuration from file: " + configFilePath);
        }

        return false;
    }

	private ApplicationConfiguration loadAppConfFromFile() {
		try {
	        String jsonConfig = FileUtils.readFileToString(new File(configFilePath));
	        ApplicationConfiguration applicationConfiguration = jsonService.jsonToObject(jsonConfig, ApplicationConfiguration.class);

			return applicationConfiguration;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, configFilePath);
		}

		return null;
	}

	private void loadLdapConfiguration() {
		this.ldapConfiguration = createFileConfiguration(LDAP_PROPERTIES_FILE, true);

		File ldapFile = new File(LDAP_PROPERTIES_FILE);
		if (ldapFile.exists()) {
			this.ldapFileLastModifiedTime = ldapFile.lastModified();
		}
	}

	private void loadLdapCentralConfiguration() {
		this.ldapCentralConfiguration = createFileConfiguration(LDAP_CENTRAL_PROPERTIES_FILE, false);

		File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
		if (ldapCentralFile.exists()) {
			this.ldapCentralFileLastModifiedTime = ldapCentralFile.lastModified();
		}
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

    public void loadCryptoConfigurationSalt() {
        try {
            FileConfiguration cryptoConfiguration = createFileConfiguration(saltFilePath, true);

			this.cryptoConfigurationSalt = cryptoConfiguration.getString("encodeSalt");
        } catch (Exception ex) {
			log.error("Failed to load configuration from {0}", ex, this.saltFilePath);
			throw new ConfigurationException("Failed to load configuration from " + this.saltFilePath, ex);
        }
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

	public String getCryptoConfigurationSalt() {
		return cryptoConfigurationSalt;
	}

	public String getCacheRefreshFilePath() {
		return cacheRefreshFilePath;
	}

	public String getLogRotationFilePath() {
		return logRotationFilePath;
	}

	public String getConfigurationDn() {
		return getLdapConfiguration().getString("configurationEntryDN");
	}
	
	// issue 102 - begin : changed by shekhar
	public ImportPersonConfig getImportPersonConfig() {
		return importPersonConfig;
	}

	public void setImportPersonConfig(ImportPersonConfig importPersonConfig) {
		this.importPersonConfig = importPersonConfig;
	}// issue 102 - end : changed by shekhar


	public static OxTrustConfiguration instance() {
		return (OxTrustConfiguration) Component.getInstance(OxTrustConfiguration.class);
	}

}
