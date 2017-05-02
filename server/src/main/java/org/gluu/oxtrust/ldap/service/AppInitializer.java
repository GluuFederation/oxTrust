/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.util.BuildVersion;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
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
import org.xdi.service.PythonService;
import org.xdi.service.cdi.event.ConfigurationUpdate;
import org.xdi.service.custom.script.CustomScriptManager;
import org.xdi.service.ldap.LdapConnectionService;
import org.xdi.service.timer.schedule.TimerSchedule;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

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
	// We are going to start svn synchronization every 5 minutes
	public static final long SVN_SYNC_INTERVAL = (long) (1000L * 62 * 5);

	private static final long VALIDATION_INTERVAL = (long) (1000L * 63 * 1);
	private static final long LOG_MONITOR_INTERVAL = (long) (1000L * 60 * 64 * 24);

	@Inject
	private Logger log;

	@Inject
	private SvnSyncTimer svnSyncTimer;
	
	@Inject
	private ApplianceService applianceService;

	@Inject
	private MetadataValidationTimer metadataValidationTimer;

	@Inject
	private LogFileSizeChecker logFileSizeChecker;
	
	@Inject
	private OxTrustConfiguration oxTrustConfiguration;
	
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
		createConnectionProvider(oxTrustConfiguration.getLdapConfiguration(), "localLdapConfiguration", "connectionProvider");
		oxTrustConfiguration.create();

		initializeLdifArchiver();

		// Initialize central LDAP connection provider
		if ((oxTrustConfiguration.getLdapCentralConfiguration() != null) && oxTrustConfiguration.getAppsConfiguration().isUpdateApplianceStatus()) {
			createConnectionProvider(oxTrustConfiguration.getLdapCentralConfiguration(), "centralLdapConfiguration", "centralConnectionProvider");
		}

		// Initialize template engine
		TemplateService.instance().initTemplateEngine();

		// Initialize SubversionService
		SubversionService.instance().initSubversionService();

		// Initialize python interpreter
		PythonService.instance().initPythonInterpreter(oxTrustConfiguration.getLdapConfiguration().getString("pythonModulesDir", null));

		// Initialize script manager
		List<CustomScriptType> supportedCustomScriptTypes = Arrays.asList( CustomScriptType.CACHE_REFRESH, CustomScriptType.UPDATE_USER, CustomScriptType.USER_REGISTRATION, CustomScriptType.ID_GENERATOR, CustomScriptType.SCIM );
        CustomScriptManager.instance().init(supportedCustomScriptTypes);

		startSvnSync();
		// Asynchronous metadata validation service
		startMetadataValidator();

		createShibbolethConfiguration();

		logSizeChecker();
	}

	@Observer("org.jboss.seam.postInitialization")
	@Asynchronous
    public void postInitialization() {
	}

    @Observer("org.jboss.seam.postInitialization")
    public void initReloadTimer() {
		this.isActive = new AtomicBoolean(false);
		this.lastFinishedTime = System.currentTimeMillis();

		Events.instance().raiseTimedEvent(EVENT_TYPE, new TimerSchedule(1 * 60 * 1000L, DEFAULT_INTERVAL * 1000L));
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
			this.lastFinishedTime = System.currentTimeMillis();
		}
	}

	private void reloadConfiguration() {
	}

	private String buildServersString(List<SimpleProperty> servers) {
		StringBuilder sb = new StringBuilder();

		if (servers == null) {
			return sb.toString();
		}

		boolean first = true;
		for (SimpleProperty server : servers) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}

			sb.append(server.getValue());
		}

		return sb.toString();
	}

	private void startMetadataValidator() {
		// Schedule first check after 60 seconds
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 60);
		metadataValidationTimer.scheduleValidation(calendar.getTime(), VALIDATION_INTERVAL);
	}
	
	private boolean createShibbolethConfiguration() {

		AppConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		boolean createConfig = applicationConfiguration.isConfigGeneration();
		log.info("IDP config generation is set to " + createConfig);
		
		if (createConfig) {

			String gluuSPInum;
			GluuSAMLTrustRelationship gluuSP;

			try {

				gluuSPInum = ApplianceService.instance().getAppliance().getGluuSPTR();

				// log.info("########## gluuSPInum = " + gluuSPInum);

				gluuSP = new GluuSAMLTrustRelationship();
				gluuSP.setDn(TrustService.instance().getDnForTrustRelationShip(gluuSPInum));

			} catch (EntryPersistenceException ex) {
				log.error("Failed to determine SP inum", ex);
				return false;
			}

			// log.info("########## gluuSP.getDn() = " + gluuSP.getDn());

			boolean servicesNeedRestarting = false;
			if (gluuSPInum == null || ! TrustService.instance().containsTrustRelationship(gluuSP)) {

				log.info("No trust relationships exist in LDAP. Adding gluuSP");
//				GluuAppliance appliance = ApplianceService.instance().getAppliance();
//				appliance.setGluuSPTR(null);
//				ApplianceService.instance().updateAppliance(appliance);
				TrustService.instance().addGluuSP();
				servicesNeedRestarting = true;
			}

			gluuSP = TrustService.instance().getRelationshipByInum(ApplianceService.instance().getAppliance().getGluuSPTR());

			List<GluuSAMLTrustRelationship> trustRelationships = TrustService.instance().getAllActiveTrustRelationships();

			/*
			if (trustRelationships != null && !trustRelationships.isEmpty()) {
				for (GluuSAMLTrustRelationship gluuSAMLTrustRelationship : trustRelationships) {
					log.info("########## gluuSAMLTrustRelationship.getDn() = " + gluuSAMLTrustRelationship.getDn());
				}
			}
			*/

			String shibbolethVersion = applicationConfiguration.getShibbolethVersion();
			log.info("########## shibbolethVersion = " + shibbolethVersion);

			Shibboleth3ConfService.instance().generateMetadataFiles(gluuSP);
			Shibboleth3ConfService.instance().generateConfigurationFiles(trustRelationships);

			Shibboleth3ConfService.instance().removeUnusedCredentials();
			Shibboleth3ConfService.instance().removeUnusedMetadata();

			if (servicesNeedRestarting) {
				applianceService.restartServices();
			}
		}

		return true;
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
				.getProperties(), oxTrustConfiguration.getCryptoConfigurationSalt()));
		}
		Contexts.getApplicationContext().set(connectionProviderComponentName, connectionProvider);
	}

	private void startSvnSync() {
		AppConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		if (applicationConfiguration.isPersistSVN()) {
			// Schedule first check after 60 seconds
			final Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, 60);

			svnSyncTimer.scheduleSvnSync(calendar.getTime(), SVN_SYNC_INTERVAL);
		}
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

    @Observer(OxTrustConfiguration.LDAP_CONFIGUARION_RELOAD_EVENT_TYPE)
    public void recreateLdapEntryManager() {
    	// Backup current references to objects to allow shutdown properly
    	LdapEntryManager oldLdapEntryManager = (LdapEntryManager) Component.getInstance(LDAP_ENTRY_MANAGER_NAME);

    	// Recreate components
		createConnectionProvider(oxTrustConfiguration.getLdapConfiguration(), "localLdapConfiguration", "connectionProvider");

        // Destroy old components
    	Contexts.getApplicationContext().remove(LDAP_ENTRY_MANAGER_NAME);
    	oldLdapEntryManager.destroy();

    	log.debug("Destroyed {0}: {1}", LDAP_ENTRY_MANAGER_NAME, oldLdapEntryManager);
    }

    @Observer(OxTrustConfiguration.LDAP_CENTRAL_CONFIGUARION_RELOAD_EVENT_TYPE)
    public void recreateCentralLdapEntryManager() {
    	// Backup current references to objects to allow shutdown properly
    	LdapEntryManager oldCentralLdapEntryManager = (LdapEntryManager) Component.getInstance(LDAP_CENTRAL_ENTRY_MANAGER_NAME);

    	// Recreate components
		if ((oxTrustConfiguration.getLdapCentralConfiguration() != null) && oxTrustConfiguration.getApplicationConfiguration().isUpdateApplianceStatus()) {
			createConnectionProvider(oxTrustConfiguration.getLdapCentralConfiguration(), "centralLdapConfiguration", "centralConnectionProvider");
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

	private void initializeLdifArchiver() {
		LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance(LDAP_ENTRY_MANAGER_NAME);
		ldapEntryManager.addDeleteSubscriber(new LdifArchiver(ldapEntryManager));
	}

	private void logSizeChecker() {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 60);
		logFileSizeChecker.scheduleSizeChecking(calendar.getTime(), LOG_MONITOR_INTERVAL);
	}

	private Object jsonToObject(String json, Class<?> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Object clazzObject = mapper.readValue(json, clazz);
		return clazzObject;
	}

	@Deprecated
	// Remove it after 2013/10/01
	private GluuLdapConfiguration mapLdapOldConfig(OxIDPAuthConf oneConf) {
		GluuLdapConfiguration ldapConfig = new GluuLdapConfiguration();
		ldapConfig.setServers(Arrays.asList(
				new SimpleProperty(oneConf.getFields().get(0).getValues().get(0) + ":" + oneConf.getFields().get(1).getValues().get(0))));
		ldapConfig.setBindDN(oneConf.getFields().get(2).getValues().get(0));
		ldapConfig.setBindPassword(oneConf.getFields().get(3).getValues().get(0));
		ldapConfig.setUseSSL(Boolean.valueOf(oneConf.getFields().get(4).getValues().get(0)));
		ldapConfig.setMaxConnections(3);
		ldapConfig.setConfigId("auth_ldap_server");
		ldapConfig.setEnabled(oneConf.getEnabled());
		return ldapConfig;
	}

	private GluuLdapConfiguration mapLdapConfig(String config) throws Exception {
		return (GluuLdapConfiguration) jsonToObject(config, GluuLdapConfiguration.class);
	}

	@Produces @ApplicationScoped @Named("openIdConfiguration")
	public OpenIdConfigurationResponse initOpenIdConfiguration() throws OxIntializationException {
		String oxAuthIssuer = this.oxTrustConfiguration.getApplicationConfiguration().getOxAuthIssuer();
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
		String umaIssuer = this.oxTrustConfiguration.getApplicationConfiguration().getUmaIssuer();
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
        return buildVersion.class.getBuildNumber();
    }

}
