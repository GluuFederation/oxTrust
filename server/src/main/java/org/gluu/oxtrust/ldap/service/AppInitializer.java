/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.gluu.exception.OxIntializationException;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxauth.model.util.SecurityProviderUtility;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshTimer;
import org.gluu.oxtrust.service.CleanerTimer;
import org.gluu.oxtrust.service.MetricService;
import org.gluu.oxtrust.service.cdi.event.CentralLdap;
import org.gluu.oxtrust.service.custom.LdapCentralConfigurationReload;
import org.gluu.oxtrust.service.logger.LoggerService;
import org.gluu.oxtrust.service.status.ldap.PersistanceStatusTimer;
import org.gluu.oxtrust.util.BuildVersionService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.model.PersistenceConfiguration;
import org.gluu.service.PythonService;
import org.gluu.service.cdi.event.LdapConfigurationReload;
import org.gluu.service.cdi.util.CdiUtil;
import org.gluu.service.custom.lib.CustomLibrariesLoader;
import org.gluu.service.custom.script.CustomScriptManager;
import org.gluu.service.metric.inject.ReportMetric;
import org.gluu.service.timer.QuartzSchedulerManager;
import org.gluu.util.StringHelper;
import org.gluu.util.properties.FileConfiguration;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.gluu.oxauth.client.OpenIdConfigurationClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.client.OpenIdConnectDiscoveryClient;
import org.gluu.oxauth.client.OpenIdConnectDiscoveryResponse;
import org.gluu.oxauth.client.uma.UmaClientFactory;
import org.gluu.oxauth.client.uma.UmaMetadataService;

/**
 * Perform startup time initialization
 *
 * @author Yuriy Movchan
 */
@ApplicationScoped
@Named
public class AppInitializer {

	@Inject
	private Logger log;

	@Inject
	private BeanManager beanManager;

	@Inject
	@Named(ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME)
	private Instance<PersistenceEntryManager> persistenceEntryManagerInstance;

	@Inject
	@Named(ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME)
	@ReportMetric
	private Instance<PersistenceEntryManager> persistenceMetricEntryManagerInstance;

	@Inject
	@Named(ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME)
	@CentralLdap
	private Instance<PersistenceEntryManager> persistenceCentralEntryManagerInstance;

	@Inject
	private Instance<EncryptionService> encryptionServiceInstance;

	@Inject
	private ApplicationFactory applicationFactory;

	@Inject
	private MetadataValidationTimer metadataValidationTimer;

	@Inject
	private EntityIDMonitoringService entityIDMonitoringService;

	@Inject
	private LogFileSizeChecker logFileSizeChecker;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private CacheRefreshTimer cacheRefreshTimer;

	@Inject
	private StatusCheckerDaily statusCheckerDaily;

	@Inject
	private StatusCheckerTimer statusCheckerTimer;
	
	@Inject
	private UpdateChecker updateChecker;

	@Inject
	private PythonService pythonService;

	@Inject
	private MetricService metricService;

	@Inject
	private CustomScriptManager customScriptManager;

	@Inject
	private PersistanceStatusTimer ldapStatusTimer;

	@Inject
	private ShibbolethInitializer shibbolethInitializer;

	@Inject
	private TemplateService templateService;

	@Inject
	private CustomLibrariesLoader customLibrariesLoader;

	@Inject
	private QuartzSchedulerManager quartzSchedulerManager;

	@Inject
	private LdifArchiver ldifArchiver;

	@Inject
	private BuildVersionService buildVersionService;

	@Inject
	private LoggerService loggerService;

	@Inject
	private CleanerTimer cleanerTimer;

	@PostConstruct
	public void createApplicationComponents() {
		SecurityProviderUtility.installBCProvider();

		// Remove JUL console logger
		SLF4JBridgeHandler.removeHandlersForRootLogger();

		// Add SLF4JBridgeHandler to JUL root logger
		SLF4JBridgeHandler.install();
	}

	/**
	 * Initialize components and schedule DS connection time checker
	 */
	public void applicationInitialized(@Observes @Initialized(ApplicationScoped.class) Object init) {
		log.debug("Initializing application services");
		showBuildInfo();

		configurationFactory.create();

		PersistenceEntryManager localLdapEntryManager = persistenceEntryManagerInstance.get();
		initializeLdifArchiver(localLdapEntryManager);

		// Initialize template engine
		templateService.initTemplateEngine();

		// Initialize python interpreter
		pythonService.initPythonInterpreter(configurationFactory.getBaseConfiguration()
				.getString("pythonModulesDir", null));

		// Initialize Shibboleth
		shibbolethInitializer.createShibbolethConfiguration();

		// Initialize script manager
		List<CustomScriptType> supportedCustomScriptTypes = Arrays.asList(CustomScriptType.CACHE_REFRESH,
				CustomScriptType.UPDATE_USER, CustomScriptType.USER_REGISTRATION, CustomScriptType.ID_GENERATOR,
				CustomScriptType.SCIM);

		// Start timer
		initSchedulerService();

		// Schedule timer tasks
		metricService.initTimer();
		configurationFactory.initTimer();
		loggerService.initTimer();
		ldapStatusTimer.initTimer();
		metadataValidationTimer.initTimer();
		entityIDMonitoringService.initTimer();
		cacheRefreshTimer.initTimer();
		customScriptManager.initTimer(supportedCustomScriptTypes);
		cleanerTimer.initTimer();
		statusCheckerDaily.initTimer();
		statusCheckerTimer.initTimer();
		logFileSizeChecker.initTimer();
		updateChecker.initTimer();
	}

	protected void initSchedulerService() {
		quartzSchedulerManager.start();

		String disableScheduler = System.getProperties().getProperty("gluu.disable.scheduler");
		if ((disableScheduler != null) && Boolean.valueOf(disableScheduler)) {
			this.log.warn("Suspending Quartz Scheduler Service...");
			quartzSchedulerManager.standby();
			return;
		}
	}

	@Produces
	@ApplicationScoped
	public StringEncrypter getStringEncrypter() throws OxIntializationException {
		String encodeSalt = configurationFactory.getCryptoConfigurationSalt();

		if (StringHelper.isEmpty(encodeSalt)) {
			throw new OxIntializationException("Encode salt isn't defined");
		}

		try {
			StringEncrypter stringEncrypter = StringEncrypter.instance(encodeSalt);

			return stringEncrypter;
		} catch (EncryptionException ex) {
			throw new OxIntializationException("Failed to create StringEncrypter instance");
		}
	}

	private void showBuildInfo() {
		log.info("Build date {}. Code revision {} on {}. Build {}", getGluuBuildDate(), getGluuRevisionVersion(),
				getGluuRevisionDate(), getGluuBuildNumber());
	}

	protected Properties preparePersistanceProperties() {
		PersistenceConfiguration persistenceConfiguration = this.configurationFactory.getPersistenceConfiguration();
		FileConfiguration persistenceConfig = persistenceConfiguration.getConfiguration();
		Properties connectionProperties = (Properties) persistenceConfig.getProperties();

		EncryptionService securityService = encryptionServiceInstance.get();
		Properties decryptedConnectionProperties = securityService.decryptAllProperties(connectionProperties);
		return decryptedConnectionProperties;
	}

	protected Properties prepareCustomPersistanceProperties(String configId) {
		Properties connectionProperties = preparePersistanceProperties();
		if (StringHelper.isNotEmpty(configId)) {
			// Replace properties names 'configId.xyz' to 'configId.xyz' in order to
			// override default values
			connectionProperties = (Properties) connectionProperties.clone();

			String baseGroup = configId + ".";
			for (Object key : connectionProperties.keySet()) {
				String propertyName = (String) key;
				if (propertyName.startsWith(baseGroup)) {
					propertyName = propertyName.substring(baseGroup.length());

					Object value = connectionProperties.get(key);
					connectionProperties.put(propertyName, value);
				}
			}
		}

		return connectionProperties;
	}

	@Produces
	@ApplicationScoped
	@Named(ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME)
	public PersistenceEntryManager createPersistenceEntryManager() {
		Properties connectionProperties = preparePersistanceProperties();

		PersistenceEntryManager persistenceEntryManager = applicationFactory.getPersistenceEntryManagerFactory()
				.createEntryManager(connectionProperties);
		log.info("Created {}: {} with operation service: {}",
				new Object[] { ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME, persistenceEntryManager,
						persistenceEntryManager.getOperationService() });

		return persistenceEntryManager;
	}

	@Produces
	@ApplicationScoped
	@Named(ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME)
	@ReportMetric
	public PersistenceEntryManager createMetricPersistenceEntryManager() {
		Properties connectionProperties = prepareCustomPersistanceProperties(
				ApplicationFactory.PERSISTENCE_METRIC_CONFIG_GROUP_NAME);

		PersistenceEntryManager persistenceEntryManager = applicationFactory.getPersistenceEntryManagerFactory()
				.createEntryManager(connectionProperties);
		log.info("Created {}: {} with operation service: {}",
				new Object[] { ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME, persistenceEntryManager,
						persistenceEntryManager.getOperationService() });

		return persistenceEntryManager;
	}

	@Produces
	@ApplicationScoped
	@Named(ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME)
	@CentralLdap
	public PersistenceEntryManager createCentralLdapEntryManager() {
		if (!((configurationFactory.getLdapCentralConfiguration() != null)
				&& configurationFactory.getAppConfiguration().isUpdateStatus())) {
			return new LdapEntryManager();
		}

		FileConfiguration ldapCentralConfig = configurationFactory.getLdapCentralConfiguration();
		Properties centralConnectionProperties = (Properties) ldapCentralConfig.getProperties();

		EncryptionService securityService = encryptionServiceInstance.get();
		Properties decryptedCentralConnectionProperties = securityService
				.decryptProperties(centralConnectionProperties);

		// TODO: Review if it works well with couchbase
		PersistenceEntryManager centralLdapEntryManager = applicationFactory.getPersistenceEntryManagerFactory()
				.createEntryManager(decryptedCentralConnectionProperties);
		log.info("Created {}: {}", new Object[] { ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME,
				centralLdapEntryManager.getOperationService() });

		return centralLdapEntryManager;
	}

	public void recreatePersistanceEntryManager(@Observes @LdapConfigurationReload String event) {
		recreatePersistanceEntryManagerImpl(persistenceEntryManagerInstance,
				ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME);

		recreatePersistanceEntryManagerImpl(persistenceEntryManagerInstance,
				ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME, ReportMetric.Literal.INSTANCE);
	}

	protected void recreatePersistanceEntryManagerImpl(Instance<PersistenceEntryManager> instance,
			String persistenceEntryManagerName, Annotation... qualifiers) {
		// Get existing application scoped instance
		PersistenceEntryManager oldLdapEntryManager = CdiUtil.getContextBean(beanManager, PersistenceEntryManager.class,
				persistenceEntryManagerName, qualifiers);

		// Close existing connections
		closePersistenceEntryManager(oldLdapEntryManager, persistenceEntryManagerName);

		// Force to create new bean
		PersistenceEntryManager ldapEntryManager = instance.get();
		instance.destroy(ldapEntryManager);
		log.info("Recreated instance {}: {} with operation service: {}", persistenceEntryManagerName, ldapEntryManager,
				ldapEntryManager.getOperationService());
	}

	public void recreateCentralPersistanceEntryManager(@Observes @LdapCentralConfigurationReload String event) {
		// Get existing application scoped instance
		PersistenceEntryManager oldCentralLdapEntryManager = CdiUtil.getContextBean(beanManager,
				PersistenceEntryManager.class, ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME);

		// Close existing connections
		closePersistenceEntryManager(oldCentralLdapEntryManager,
				ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME);

		// Force to create new bean
		PersistenceEntryManager ldapCentralEntryManager = persistenceCentralEntryManagerInstance.get();
		persistenceEntryManagerInstance.destroy(ldapCentralEntryManager);
		log.info("Recreated instance {}: {}", ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME,
				ldapCentralEntryManager);
	}

	private void closePersistenceEntryManager(PersistenceEntryManager oldPersistenceEntryManager,
			String persistenceEntryManagerName) {
		// Close existing connections
		if ((oldPersistenceEntryManager != null) && (oldPersistenceEntryManager.getOperationService() != null)) {
			log.debug("Attempting to destroy {}:{} with operation service: {}", persistenceEntryManagerName,
					oldPersistenceEntryManager, oldPersistenceEntryManager.getOperationService());
			oldPersistenceEntryManager.destroy();
			log.debug("Destroyed {}:{} with operation service: {}", persistenceEntryManagerName,
					oldPersistenceEntryManager, oldPersistenceEntryManager.getOperationService());
		}
	}

	private void initializeLdifArchiver(PersistenceEntryManager ldapEntryManager) {
		ldifArchiver.init();
		ldapEntryManager.addDeleteSubscriber(ldifArchiver);
	}

	@Produces
	@ApplicationScoped
	@Named("openIdConfiguration")
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
		if ((openIdConnectDiscoveryResponse.getStatus() != 200) || (openIdConnectDiscoveryResponse.getSubject() == null)
				|| (openIdConnectDiscoveryResponse.getLinks().size() == 0)) {
			throw new OxIntializationException("OpenId discovery response is invalid!");
		}

		log.debug("Attempting to load OpenID configuration");
		String configurationEndpoint = openIdConnectDiscoveryResponse.getLinks().get(0).getHref()
				+ "/.well-known/openid-configuration";

		OpenIdConfigurationClient client = new OpenIdConfigurationClient(configurationEndpoint);

		OpenIdConfigurationResponse openIdConfiguration;
		try {
			openIdConfiguration = client.execOpenIdConfiguration();
		} catch (Exception e) {
			log.error("Failed to load OpenId configuration!", e);
			throw new OxIntializationException("Failed to load OpenId configuration!");
		}

		if (openIdConfiguration.getStatus() != 200) {
			throw new OxIntializationException("OpenId configuration response is invalid!");
		}

		return openIdConfiguration;
	}

	public void destroy(@Observes @BeforeDestroyed(ApplicationScoped.class) ServletContext init) {
		log.info("Stopping services and closing DB connections at server shutdown...");
		log.debug("Checking who intiated destory", new Throwable());

		metricService.close();

		PersistenceEntryManager persistanceEntryManager = persistenceEntryManagerInstance.get();
		closePersistenceEntryManager(persistanceEntryManager, ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME);

		PersistenceEntryManager persistanceMetricEntryManager = persistenceMetricEntryManagerInstance.get();
		closePersistenceEntryManager(persistanceMetricEntryManager,
				ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME);

		PersistenceEntryManager persistanceCentralEntryManager = persistenceCentralEntryManagerInstance.get();
		if (persistanceCentralEntryManager != null) {
			closePersistenceEntryManager(persistanceCentralEntryManager,
					ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME);
		}
	}

	public String getGluuRevisionVersion() {
		return buildVersionService.getRevisionVersion();
	}

	public String getGluuRevisionDate() {
		return buildVersionService.getRevisionDate();
	}

	public String getGluuBuildDate() {
		return buildVersionService.getBuildDate();
	}

	public String getGluuBuildNumber() {
		return buildVersionService.getBuildNumber();
	}

}