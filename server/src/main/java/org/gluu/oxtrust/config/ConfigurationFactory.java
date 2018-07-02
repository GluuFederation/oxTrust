/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.config;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.gluu.oxtrust.service.custom.LdapCentralConfigurationReload;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.PersistenceEntryManagerFactory;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.AttributeResolverConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.Configuration;
import org.xdi.config.oxtrust.ImportPersonConfig;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.exception.ConfigurationException;
import org.xdi.service.JsonService;
import org.xdi.service.cdi.async.Asynchronous;
import org.xdi.service.cdi.event.ConfigurationEvent;
import org.xdi.service.cdi.event.ConfigurationUpdate;
import org.xdi.service.cdi.event.LdapConfigurationReload;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

/**
 * @author Yuriy Movchan
 * @version 0.1, 05/15/2013
 */
@ApplicationScoped
@Named
public class ConfigurationFactory {

	@Inject
	private Logger log;

	@Inject
	private JsonService jsonService;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private Event<AppConfiguration> configurationUpdateEvent;

	@Inject
	private Event<String> event;

	@Inject
	@Named(AppInitializer.PERSISTENCE_ENTRY_MANAGER_NAME)
	private Instance<PersistenceEntryManager> ldapEntryManagerInstance;

	@Inject
	private Instance<PersistenceEntryManagerFactory> persistenceEntryManagerFactoryInstance;

	@Inject
	private Instance<Configuration> configurationInstance;

	public final static String PERSISTENCE_CONFIGUARION_RELOAD_EVENT_TYPE = "persistenceConfigurationReloadEvent";
	public final static String PERSISTENCE_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE = "persistenceCentralConfigurationReloadEvent";

	private final static int DEFAULT_INTERVAL = 30; // 30 seconds

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

	private static final String GLUU_FILE_PATH = DIR + "gluu.properties";
	public static final String LDAP_PROPERTIES_FILE = DIR + "oxtrust-ldap.properties";
	public static final String LDAP_DEFAULT_PROPERTIES_FILE = DIR + "ox-ldap.properties";
	public static final String LDAP_CENTRAL_PROPERTIES_FILE = DIR + "oxtrust-central-ldap.properties";

	public static final String APPLICATION_CONFIGURATION = "oxtrust-config.json";
	public static final String CACHE_PROPERTIES_FILE = "oxTrustCacheRefresh.properties";
	public static final String LOG_ROTATION_CONFIGURATION = "oxTrustLogRotationConfiguration.xml";
	public static final String SALT_FILE_NAME = "salt";

	private String confDir, configFilePath, cacheRefreshFilePath, logRotationFilePath, saltFilePath;

	private boolean loaded = false;

	private PersistenceConfiguration persistenceConfiguration;

	private FileConfiguration ldapCentralConfiguration;
	private AppConfiguration appConfiguration;
	private CacheRefreshConfiguration cacheRefreshConfiguration;
	private ImportPersonConfig importPersonConfig;
	private AttributeResolverConfiguration attributeResolverConfiguration;
	private String cryptoConfigurationSalt;

	private AtomicBoolean isActive;

	private long ldapCentralFileLastModifiedTime = -1;

	private long loadedRevision = -1;
	private boolean loadedFromLdap = true;

	@PostConstruct
	public void init() {
		this.isActive = new AtomicBoolean(true);
		try {
			log.info("Creating oxTrustConfiguration");
			this.persistenceConfiguration = loadPersistenceConfiguration();
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

    protected PersistenceConfiguration loadPersistenceConfiguration() {
        PersistenceConfiguration currentPersistenceConfiguration = null;

        String gluuFileName = determineGluuConfigurationFileName();
        if (gluuFileName != null) {
            currentPersistenceConfiguration = loadPersistenceConfiguration(gluuFileName);
        }

        // Fall back to old LDAP persistence layer
        if (currentPersistenceConfiguration == null) {
            log.warn("Failed to load persistence configuration. Attempting to use LDAP layer");
            String ldapFileName = determineLdapConfigurationFileName();
            currentPersistenceConfiguration = loadLdapConfiguration(ldapFileName);
        }
        
        return currentPersistenceConfiguration;
    }

	public void create() {
		if (!createFromLdap(true)) {
			log.error("Failed to load configuration from LDAP. Please fix it!!!.");
			throw new ConfigurationException("Failed to load configuration from LDAP.");
		} else {
			log.info("Configuration loaded successfully.");
		}
	}

	public void initTimer() {
		log.debug("Initializing Configuration Timer");

		final int delay = 30;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new ConfigurationEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void reloadConfigurationTimerEvent(@Observes @Scheduled ConfigurationEvent configurationEvent) {
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
        PersistenceConfiguration newPersistenceConfiguration = loadPersistenceConfiguration();

        if (newPersistenceConfiguration != null) {
            if (!StringHelper.equalsIgnoreCase(this.persistenceConfiguration.getFileName(), newPersistenceConfiguration.getFileName()) || (newPersistenceConfiguration.getLastModifiedTime() > this.persistenceConfiguration.getLastModifiedTime())) {
                // Reload configuration only if it was modified
                this.persistenceConfiguration = newPersistenceConfiguration;
                event.select(LdapConfigurationReload.Literal.INSTANCE).fire(PERSISTENCE_CONFIGUARION_RELOAD_EVENT_TYPE);
            }
        }

		// Reload LDAP central configuration if needed
		File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
		if (ldapCentralFile.exists()) {
			final long lastModified = ldapCentralFile.lastModified();
			if (lastModified > ldapCentralFileLastModifiedTime) {
				// Reload configuration only if it was modified
				loadLdapCentralConfiguration();
				event.select(LdapCentralConfigurationReload.Literal.INSTANCE)
						.fire(PERSISTENCE_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE);
			}
		} else if (this.ldapCentralConfiguration != null) {
			// Allow to remove not mandatory configuration file
			this.ldapCentralConfiguration = null;
			event.select(LdapCentralConfigurationReload.Literal.INSTANCE)
					.fire(PERSISTENCE_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE);
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

	public String confDir() {
		final String confDir = this.persistenceConfiguration.getConfiguration().getString("confDir", null);
		if (StringUtils.isNotBlank(confDir)) {
			return confDir;
		}

		return DIR;
	}

    @ApplicationScoped
    public PersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
    }

	public FileConfiguration getLdapCentralConfiguration() {
		return ldapCentralConfiguration;
	}

	@Produces
	@ApplicationScoped
	public AppConfiguration getAppConfiguration() {
		return appConfiguration;
	}

	@Produces
	@ApplicationScoped
	public CacheRefreshConfiguration getCacheRefreshConfiguration() {
		return cacheRefreshConfiguration;
	}

	@Produces
	@ApplicationScoped
	public ImportPersonConfig getImportPersonConfig() {
		return importPersonConfig;
	}

	@Produces
	@ApplicationScoped
	public AttributeResolverConfiguration getAttributeResolverConfiguration() {
		return attributeResolverConfiguration;
	}

	public String getCryptoConfigurationSalt() {
		return cryptoConfigurationSalt;
	}

	public String getConfigurationDn() {
        return this.persistenceConfiguration.getConfiguration().getString("oxtrust_ConfigurationEntryDN");
    }

    private boolean createFromFile() {
		boolean result = reloadAppConfFromFile();

		return result;
	}

	private boolean reloadAppConfFromFile() {
		final AppConfiguration appConfiguration = loadAppConfFromFile();
		if (appConfiguration != null) {
			log.info("Reloaded application configuration from file: " + configFilePath);
			this.appConfiguration = appConfiguration;
			return true;
		} else {
			log.error("Failed to load application configuration from file: " + configFilePath);
		}

		return false;
	}

	private AppConfiguration loadAppConfFromFile() {
		try {
			String jsonConfig = FileUtils.readFileToString(new File(configFilePath));
			AppConfiguration appConfiguration = jsonService.jsonToObject(jsonConfig, AppConfiguration.class);

			return appConfiguration;
		} catch (Exception ex) {
			log.error("Failed to load configuration from {}", configFilePath, ex);
		}

		return null;
	}

	private void loadLdapCentralConfiguration() {
		this.ldapCentralConfiguration = createFileConfiguration(LDAP_CENTRAL_PROPERTIES_FILE, false);

		File ldapCentralFile = new File(LDAP_CENTRAL_PROPERTIES_FILE);
		if (ldapCentralFile.exists()) {
			this.ldapCentralFileLastModifiedTime = ldapCentralFile.lastModified();
		}
	}

	public void loadCryptoConfigurationSalt() {
		try {
			FileConfiguration cryptoConfiguration = createFileConfiguration(saltFilePath, true);

			this.cryptoConfigurationSalt = cryptoConfiguration.getString("encodeSalt");
		} catch (Exception ex) {
			log.error("Failed to load configuration from {}", this.saltFilePath, ex);
			throw new ConfigurationException("Failed to load configuration from " + this.saltFilePath, ex);
		}
	}

	private FileConfiguration createFileConfiguration(String fileName, boolean isMandatory) {
		try {
			FileConfiguration fileConfiguration = new FileConfiguration(fileName);
			if (fileConfiguration.isLoaded()) {
				log.debug("########## fileName = " + fileConfiguration.getFileName());
				log.debug("########## oxtrust_ConfigurationEntryDN = "
						+ fileConfiguration.getString("oxtrust_ConfigurationEntryDN"));
				return fileConfiguration;
			}
		} catch (Exception ex) {
			if (isMandatory) {
				log.error("Failed to load configuration from {}", fileName, ex);
				throw new ConfigurationException("Failed to load configuration from " + fileName, ex);
			}
		}

		return null;
	}

	private boolean createFromLdap(boolean recoverFromFiles) {
		log.info("Loading configuration from LDAP...");
		try {
			final LdapOxTrustConfiguration conf = loadConfigurationFromLdap();
			if (conf != null) {
				init(conf);

				// Destroy old configuration
				if (this.loaded) {
					destroy(AppConfiguration.class);
					destroy(CacheRefreshConfiguration.class);
					destroy(ImportPersonConfig.class);
					destroy(AttributeResolverConfiguration.class);
				}

				this.loaded = true;
				configurationUpdateEvent.select(ConfigurationUpdate.Literal.INSTANCE).fire(this.appConfiguration);

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

	public void destroy(Class<? extends Configuration> clazz) {
		Instance<? extends Configuration> confInstance = configurationInstance.select(clazz);
		configurationInstance.destroy(confInstance.get());
	}

	public LdapOxTrustConfiguration loadConfigurationFromLdap(String... returnAttributes) {
		final PersistenceEntryManager ldapEntryManager = ldapEntryManagerInstance.get();
		final String configurationDn = getConfigurationDn();
		try {
			final LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn,
					returnAttributes);

			return conf;
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP", ex);
		}

		return null;
	}

	private void init(LdapOxTrustConfiguration conf) {
		this.appConfiguration = conf.getApplication();
		this.cacheRefreshConfiguration = conf.getCacheRefresh();
		this.importPersonConfig = conf.getImportPersonConfig();
		this.attributeResolverConfiguration = conf.getAttributeResolverConfig();
		this.loadedRevision = conf.getRevision();
	}

    private PersistenceConfiguration loadLdapConfiguration(String ldapFileName) {
        try {
            FileConfiguration ldapConfiguration = new FileConfiguration(ldapFileName);

            // Allow to override value via environment variables
            replaceWithSystemValues(ldapConfiguration);

            long ldapFileLastModifiedTime = -1;
            File ldapFile = new File(ldapFileName);
            if (ldapFile.exists()) {
                ldapFileLastModifiedTime = ldapFile.lastModified();
            }

            PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration(ldapFileName, ldapConfiguration, org.gluu.persist.couchbase.impl.CouchbaseEntryManagerFactory.class, ldapFileLastModifiedTime);

            return persistenceConfiguration;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private PersistenceConfiguration loadPersistenceConfiguration(String gluuFileName) {
        try {
            // Determine persistence type
            FileConfiguration gluuFileConf = new FileConfiguration(gluuFileName);
            String persistenceType = gluuFileConf.getString("persistence.type");
            
            // Determine configuration file name and factory class type
            String persistenceFileName = null; 
            Class<? extends PersistenceEntryManagerFactory> persistenceEntryManagerFactoryType = null;
            
            for (PersistenceEntryManagerFactory persistenceEntryManagerFactory : persistenceEntryManagerFactoryInstance) {
                log.debug("Found Persistence Entry Manager Factory with type '{}'", persistenceEntryManagerFactory);
                if (StringHelper.equalsIgnoreCase(persistenceEntryManagerFactory.getPersistenceType(), persistenceType)) {
                    persistenceFileName = persistenceEntryManagerFactory.getDefaultConfigurationFileName();
                    persistenceEntryManagerFactoryType = (Class<? extends PersistenceEntryManagerFactory>) persistenceEntryManagerFactory.getClass().getSuperclass();
                    break;
                }
            }
            
            if (persistenceFileName == null) {
                log.error("Unable to get Persistence Entry Manager Factory by type '{}'", persistenceType);
                return null;
            }

            String persistenceFileNamePath = DIR + persistenceFileName;

            FileConfiguration persistenceFileConf = new FileConfiguration(persistenceFileNamePath);
            if (!persistenceFileConf.isLoaded()) {
                log.error("Unable to load configuration file '{}'", persistenceFileNamePath);
                return null;
            }

            // Allow to override value via environment variables
            replaceWithSystemValues(persistenceFileConf);

            long persistenceFileLastModifiedTime = -1;
            File persistenceFile = new File(persistenceFileNamePath);
            if (persistenceFile.exists()) {
                persistenceFileLastModifiedTime = persistenceFile.lastModified();
            }

            PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration(persistenceFileName, persistenceFileConf, persistenceEntryManagerFactoryType, persistenceFileLastModifiedTime);

            return persistenceConfiguration;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private void replaceWithSystemValues(FileConfiguration fileConfiguration) {
        Set<Map.Entry<Object, Object>> ldapProperties = fileConfiguration.getProperties().entrySet();
        for (Map.Entry<Object, Object> ldapPropertyEntry : ldapProperties) {
            String ldapPropertyKey = (String) ldapPropertyEntry.getKey();
            if (System.getenv(ldapPropertyKey) != null) {
                ldapPropertyEntry.setValue(System.getenv(ldapPropertyKey));
            }
        }
    }

    private String determineGluuConfigurationFileName() {
        File ldapFile = new File(GLUU_FILE_PATH);
        if (ldapFile.exists()) {
            return GLUU_FILE_PATH;
        }

        return null;
    }

	private String determineLdapConfigurationFileName() {
		File ldapFile = new File(LDAP_PROPERTIES_FILE);
		if (ldapFile.exists()) {
			return LDAP_PROPERTIES_FILE;
		}

		return LDAP_DEFAULT_PROPERTIES_FILE;
	}

	public String getIDPTemplatesLocation() {
		String jetyBase = System.getProperty("jetty.base");

		if (StringHelper.isEmpty(jetyBase)) {
			return ConfigurationFactory.DIR;
		}

		return jetyBase + File.separator + "conf" + File.separator;
	}
    
    public class PersistenceConfiguration {
        private final String fileName;
        private final FileConfiguration configuration;
        private final Class<? extends PersistenceEntryManagerFactory> entryManagerFactoryType;
        private final long lastModifiedTime;

        public PersistenceConfiguration(String fileName, FileConfiguration configuration, Class<? extends PersistenceEntryManagerFactory> entryManagerFactoryType, long lastModifiedTime) {
            this.fileName = fileName;
            this.configuration = configuration;
            this.entryManagerFactoryType = entryManagerFactoryType;
            this.lastModifiedTime = lastModifiedTime;
        }

        public final String getFileName() {
            return fileName;
        }

        public final FileConfiguration getConfiguration() {
            return configuration;
        }

        public final Class<? extends PersistenceEntryManagerFactory> getEntryManagerFactoryType() {
            return entryManagerFactoryType;
        }

        public final long getLastModifiedTime() {
            return lastModifiedTime;
        }

    }

}
