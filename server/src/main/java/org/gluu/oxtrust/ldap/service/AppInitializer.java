/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.service.MetricService;
import org.gluu.oxtrust.service.status.ldap.LdapStatusTimer;
import org.gluu.oxtrust.util.BuildVersion;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.testng.annotations.Factory;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.exception.OxIntializationException;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryClient;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryResponse;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaConfigurationService;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.util.SecurityProviderUtility;
import org.xdi.oxauth.service.cdi.event.AuthConfigurationEvent;
import org.xdi.service.JsonService;
import org.xdi.service.PythonService;
import org.xdi.service.cdi.event.ConfigurationUpdate;
import org.xdi.service.cdi.event.Scheduled;
import org.xdi.service.custom.script.CustomScriptManager;
import org.xdi.service.ldap.LdapConnectionService;
import org.xdi.service.timer.QuartzSchedulerManager;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Perform startup time initialization. Provides factory methods for non Seam
 * components.
 * 
 * @author Yuriy Movchan
 */
@ApplicationScoped
@Named
public class AppInitializer {
	private final static String EVENT_TYPE = "AppInitializerTimerEvent";
    private final static int DEFAULT_INTERVAL = 30; // 30 seconds

    public static final String LDAP_ENTRY_MANAGER_NAME = "ldapEntryManager";
    public static final String LDAP_CENTRAL_ENTRY_MANAGER_NAME = "centralLdapEntryManager";

	// We are going to start connection checker every 120 seconds
	public static final long CONNECTION_CHECKER_INTERVAL = (long) (1000L * 60 * 2);

	private static final long LOG_MONITOR_INTERVAL = (long) (1000L * 60 * 64 * 24);

	@Inject
	private Logger log;

	@Inject
	private BeanManager beanManager;

	@Inject
	private Event<String> event;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject @Named(LDAP_ENTRY_MANAGER_NAME)
	private Instance<LdapEntryManager> ldapEntryManagerInstance;

	@Inject
	private SvnSyncTimer svnSyncTimer;
	
	@Inject
	private ApplianceService applianceService;

	@Inject
	private MetadataValidationTimer metadataValidationTimer;

	@Inject
	private LogFileSizeChecker logFileSizeChecker;
	
	@Inject
	private ConfigurationFactory configurationFactory;

    @Inject
    private PythonService pythonService;
    
    @Inject
    private MetricService metricService;

    @Inject
    private CustomScriptManager customScriptManager;

	@Inject
	private LdapStatusTimer ldapStatusTimer;

	@Inject
	private ShibbolethInitializer shibbolethInitializer;

	@Inject
	private JsonService jsonService;
	
	@Inject
	private QuartzSchedulerManager quartzSchedulerManager;

	@Inject
	private BuildVersion buildVersion;

    private AtomicBoolean isActive;
	private long lastFinishedTime;

	@PostConstruct
    public void createApplicationComponents() {
    	SecurityProviderUtility.installBCProvider();
    }

	/**
	 * Initialize components and schedule DS connection time checker
	 */
	public void applicationInitialized(@Observes @Initialized(ApplicationScoped.class) Object init) {
		log.debug("Creating application components");
		showBuildInfo();

		// Initialize local LDAP connection provider
		createConnectionProvider(configurationFactory.getLdapConfiguration(), "localLdapConfiguration", "connectionProvider");

		configurationFactory.create();
        LdapEntryManager localLdapEntryManager = ldapEntryManagerInstance.get();

		// Initialize central LDAP connection provider
		if ((configurationFactory.getLdapCentralConfiguration() != null) && configurationFactory.getAppConfiguration().isUpdateApplianceStatus()) {
			createConnectionProvider(configurationFactory.getLdapCentralConfiguration(), "centralLdapConfiguration", "centralConnectionProvider");
		}

		initializeLdifArchiver();

		// Initialize template engine
		TemplateService.instance().initTemplateEngine();

		// Initialize SubversionService
		SubversionService.instance().initSubversionService();

		// Initialize python interpreter
		pythonService.initPythonInterpreter(configurationFactory.getLdapConfiguration().getString("pythonModulesDir", null));

		// Initialize script manager
		List<CustomScriptType> supportedCustomScriptTypes = Arrays.asList( CustomScriptType.CACHE_REFRESH, CustomScriptType.UPDATE_USER, CustomScriptType.USER_REGISTRATION, CustomScriptType.ID_GENERATOR, CustomScriptType.SCIM );
        customScriptManager.init(supportedCustomScriptTypes);

        metricService.init();

        // Initialize Shibboleth
        shibbolethInitializer.createShibbolethConfiguration();

        // Start timer
        quartzSchedulerManager.start();

        // Schedule timer tasks
        ldapStatusTimer.initTimer();
        configurationFactory.initTimer();
        svnSyncTimer.initTimer();
		metadataValidationTimer.initTimer();
		logSizeChecker();
	}

    @Produces @ApplicationScoped
	public StringEncrypter getStringEncrypter() {
		String encodeSalt = configurationFactory.getCryptoConfigurationSalt();
    	
    	if (StringHelper.isEmpty(encodeSalt)) {
    		throw new ConfigurationException("Encode salt isn't defined");
    	}
    	
    	try {
    		StringEncrypter stringEncrypter = StringEncrypter.instance(encodeSalt);
    		
    		return stringEncrypter;
		} catch (EncryptionException ex) {
    		throw new ConfigurationException("Failed to create StringEncrypter instance");
		}
	}

    public void destroy(@Observes @BeforeDestroyed(ApplicationScoped.class) ServletContext init) {
    	log.info("Closing LDAP connection at server shutdown...");
        LdapEntryManager ldapEntryManager = ldapEntryManagerInstance.get();
        closeLdapEntryManager(ldapEntryManager);
    }

	private void showBuildInfo() {
		log.info("Build date {}. Code revision {} on {}. Build {}", getGluuBuildDate(),
				getGluuRevisionVersion(), getGluuRevisionDate(), getGluuBuildNumber());
	}

	private void createConnectionProvider(FileConfiguration configuration, String configurationComponentName, String connectionProviderComponentName) {
		Contexts.getApplicationContext().set(configurationComponentName, configuration);

		LdapConnectionService connectionProvider = null;
		if (configuration != null) {
			connectionProvider = new LdapConnectionService(PropertiesDecrypter.decryptProperties(configuration
				.getProperties(), configurationFactory.getCryptoConfigurationSalt()));
		}
		Contexts.getApplicationContext().set(connectionProviderComponentName, connectionProvider);
	}

	/**
	 * When application undeploy we need to close LDAP Connections
	 * 
	 * @throws org.apache.commons.configuration.ConfigurationException
	 */
//	@Destroy
	public void destroyApplicationComponents() throws ConfigurationException {
		log.debug("Destroying application components");
		LdapEntryManager ldapEntryManager = (LdapEntryManager) Contexts.getApplicationContext().get(LDAP_ENTRY_MANAGER_NAME);
		ldapEntryManager.destroy();

		LdapEntryManager ldapCentralEntryManager = (LdapEntryManager) Contexts.getApplicationContext().get(LDAP_CENTRAL_ENTRY_MANAGER_NAME);
		if (ldapCentralEntryManager != null) {
			ldapCentralEntryManager.destroy();
		}
	}

	@Factory(value = LDAP_ENTRY_MANAGER_NAME, scope = ScopeType.APPLICATION, autoCreate = true)
	public LdapEntryManager createLdapEntryManager() {
		LdapConnectionService connectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("connectionProvider");
		LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider));
		log.debug("Created site LdapEntryManager: " + ldapEntryManager);

		return ldapEntryManager;
	}

	@Factory(value = LDAP_CENTRAL_ENTRY_MANAGER_NAME, scope = ScopeType.APPLICATION, autoCreate = true)
	public LdapEntryManager createCentralLdapEntryManager() {
		LdapConnectionService centralConnectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("centralConnectionProvider");
		if (centralConnectionProvider == null) {
			return null;
		}
		log.debug("Created central LdapEntryManager: " + centralConnectionProvider);

		LdapEntryManager centralLdapEntryManager = new LdapEntryManager(new OperationsFacade(centralConnectionProvider));
		log.debug("Created central LdapEntryManager: " + centralLdapEntryManager);

		return centralLdapEntryManager;
	}

    @Observer(ConfigurationFactory.LDAP_CONFIGUARION_RELOAD_EVENT_TYPE)
    public void recreateLdapEntryManager() {
    	// Backup current references to objects to allow shutdown properly
    	LdapEntryManager oldLdapEntryManager = (LdapEntryManager) Component.getInstance(LDAP_ENTRY_MANAGER_NAME);

    	// Recreate components
		createConnectionProvider(configurationFactory.getLdapConfiguration(), "localLdapConfiguration", "connectionProvider");

        // Destroy old components
    	Contexts.getApplicationContext().remove(LDAP_ENTRY_MANAGER_NAME);
    	oldLdapEntryManager.destroy();

    	log.debug("Destroyed {0}: {1}", LDAP_ENTRY_MANAGER_NAME, oldLdapEntryManager);
    }

    @Observer(ConfigurationFactory.LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE)
    public void recreateCentralLdapEntryManager() {
    	// Backup current references to objects to allow shutdown properly
    	LdapEntryManager oldCentralLdapEntryManager = (LdapEntryManager) Component.getInstance(LDAP_CENTRAL_ENTRY_MANAGER_NAME);

    	// Recreate components
		if ((configurationFactory.getLdapCentralConfiguration() != null) && configurationFactory.getApplicationConfiguration().isUpdateApplianceStatus()) {
			createConnectionProvider(configurationFactory.getLdapCentralConfiguration(), "centralLdapConfiguration", "centralConnectionProvider");
		} else {
	    	Contexts.getApplicationContext().remove("centralConnectionProvider");
		}

        // Destroy old components
    	Contexts.getApplicationContext().remove(LDAP_CENTRAL_ENTRY_MANAGER_NAME);
    	
    	if (oldCentralLdapEntryManager != null) {
    		oldCentralLdapEntryManager.destroy();

        	log.debug("Destroyed {0}: {1}", LDAP_CENTRAL_ENTRY_MANAGER_NAME, oldCentralLdapEntryManager);
    	}
    }

	private void initializeLdifArchiver(LdapEntryManager ldapEntryManager) {
		ldapEntryManager.addDeleteSubscriber(new LdifArchiver(ldapEntryManager));
	}

	private void logSizeChecker() {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 60);
		logFileSizeChecker.scheduleSizeChecking(calendar.getTime(), LOG_MONITOR_INTERVAL);
	}

	private GluuLdapConfiguration mapLdapConfig(String config) {
		try {
			return (GluuLdapConfiguration) jsonService.jsonToObject(config, GluuLdapConfiguration.class);
		} catch (IOException ex) {
			log.error("Failed to parse JSON", ex);
		}
	}

	@Produces @ApplicationScoped @Named("openIdConfiguration")
	public OpenIdConfigurationResponse initOpenIdConfiguration() throws OxIntializationException {
		String oxAuthIssuer = this.configurationFactory.getAppConfiguration().getOxAuthIssuer();
		if (StringHelper.isEmpty(oxAuthIssuer)) {
			log.info("oxAuth issuer isn't specified");
			return null;
		}

		log.debug("Attempting to determine configuration endpoint URL");
        OpenIdConnectDiscoveryClient openIdConnectDiscoveryClient;
		try {
			openIdConnectDiscoveryClient = new OpenIdConnectDiscoveryClient(oxAuthIssuer);
		} catch (URISyntaxException ex) {
			throw new OxIntializationException("OpenId discovery response is invalid!", ex);
		}

		OpenIdConnectDiscoveryResponse openIdConnectDiscoveryResponse = openIdConnectDiscoveryClient.exec();
        if ((openIdConnectDiscoveryResponse.getStatus() != 200) || (openIdConnectDiscoveryResponse.getSubject() == null) ||
        	(openIdConnectDiscoveryResponse.getLinks().size() == 0)) {
			throw new OxIntializationException("OpenId discovery response is invalid!");
        }

		log.debug("Attempting to load OpenID configuration");
        String configurationEndpoint = openIdConnectDiscoveryResponse.getLinks().get(0).getHref() + "/.well-known/openid-configuration";

        OpenIdConfigurationClient client = new OpenIdConfigurationClient(configurationEndpoint);
    	
    	OpenIdConfigurationResponse openIdConfiguration = client.execOpenIdConfiguration();

        if (openIdConfiguration.getStatus() != 200) {
			throw new OxIntializationException("OpenId configuration response is invalid!");
        }
        
        return openIdConfiguration;
	}

	@Produces @ApplicationScoped @Named("umaMetadataConfiguration")
	public UmaConfiguration initUmaMetadataConfiguration() throws OxIntializationException {
		String umaConfigurationEndpoint = getUmaConfigurationEndpoint();
		if (StringHelper.isEmpty(umaConfigurationEndpoint)) {
			return null;
		}

        UmaConfigurationService metaDataConfigurationService = UmaClientFactory.instance().createMetaDataConfigurationService(umaConfigurationEndpoint);
		UmaConfiguration metadataConfiguration = metaDataConfigurationService.getMetadataConfiguration();

        if (metadataConfiguration == null) {
			throw new OxIntializationException("UMA meta data configuration is invalid!");
        }
        
        return metadataConfiguration;
	}

	public String getUmaConfigurationEndpoint() {
		String umaIssuer = this.configurationFactory.getAppConfiguration().getUmaIssuer();
		if (StringHelper.isEmpty(umaIssuer)) {
			log.trace("oxAuth UMA issuer isn't specified");
			return null;
		}

		String umaConfigurationEndpoint = umaIssuer;
		if (!umaConfigurationEndpoint.endsWith("uma-configuration")) {
			umaConfigurationEndpoint += "/.well-known/uma-configuration";
		}

		return umaConfigurationEndpoint;
	}
	
	public void updateLoggingSeverity(@Observes @ConfigurationUpdate AppConfiguration appConfiguration) {
		String loggingLevel = appConfiguration.getLoggingLevel();
		if (StringHelper.isEmpty(loggingLevel)) {
			return;
		}

		log.info("Setting loggers level to: '{0}'", loggingLevel);
		
		LoggerContext loggerContext = LoggerContext.getContext(false);

		if (StringHelper.equalsIgnoreCase("DEFAULT", loggingLevel)) {
			log.info("Reloadming log4j configuration");
			loggerContext.reconfigure();
			return;
		}

		Level level = Level.toLevel(loggingLevel, Level.INFO);

		for (org.apache.logging.log4j.core.Logger logger : loggerContext.getLoggers()) {
			String loggerName = logger.getName();
			if (loggerName.startsWith("org.xdi.service") || loggerName.startsWith("org.xdi.oxauth") || loggerName.startsWith("org.gluu")) {
				logger.setLevel(level);
			}
		}
	}

    public String getGluuRevisionVersion() {
        return buildVersion.getRevisionVersion();
    }

    public String getGluuRevisionDate() {
        return buildVersion.getRevisionDate();
    }

    public String getGluuBuildDate() {
        return buildVersion.getBuildDate();
    }

    public String getGluuBuildNumber() {
        return buildVersion.getBuildNumber();
    }

}
