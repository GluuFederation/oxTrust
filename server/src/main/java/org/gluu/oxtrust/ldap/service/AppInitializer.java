/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
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

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshTimer;
import org.gluu.oxtrust.service.CleanerTimer;
import org.gluu.oxtrust.service.MetricService;
import org.gluu.oxtrust.service.cdi.event.CentralLdap;
import org.gluu.oxtrust.service.custom.LdapCentralConfigurationReload;
import org.gluu.oxtrust.service.logger.LoggerService;
import org.gluu.oxtrust.service.status.ldap.LdapStatusTimer;
import org.gluu.oxtrust.util.BuildVersion;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xdi.exception.ConfigurationException;
import org.xdi.exception.OxIntializationException;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryClient;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryResponse;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaMetadataService;
import org.xdi.oxauth.model.uma.UmaMetadata;
import org.xdi.oxauth.model.util.SecurityProviderUtility;
import org.xdi.service.JsonService;
import org.xdi.service.PythonService;
import org.xdi.service.cdi.event.LdapConfigurationReload;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.service.custom.lib.CustomLibrariesLoader;
import org.xdi.service.custom.script.CustomScriptManager;
import org.xdi.service.ldap.LdapConnectionProviders;
import org.xdi.service.ldap.LdapConnectionService;
import org.xdi.service.metric.inject.ReportMetric;
import org.xdi.service.timer.QuartzSchedulerManager;
import org.xdi.service.timer.event.TimerEvent;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import com.unboundid.ldap.sdk.ResultCode;

/**
 * Perform startup time initialization
 *
 * @author Yuriy Movchan
 */
@ApplicationScoped
@Named
public class AppInitializer {

    public static final String LDAP_METRIC_CONFIG_GROUP_NAME = "metric";

    public static final String LDAP_ENTRY_MANAGER_NAME = "ldapEntryManager";
    public static final String LDAP_METRIC_ENTRY_MANAGER_NAME = "ldapMetricEntryManager";
    public static final String LDAP_CENTRAL_ENTRY_MANAGER_NAME = "centralLdapEntryManager";

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

    @Inject @Named(LDAP_METRIC_ENTRY_MANAGER_NAME) @ReportMetric
    private Instance<LdapEntryManager> ldapMetricEntryManagerInstance;

    @Inject @Named(LDAP_CENTRAL_ENTRY_MANAGER_NAME) @CentralLdap
    private Instance<LdapEntryManager> ldapCentralEntryManagerInstance;

    @Inject
    private Instance<EncryptionService> encryptionServiceInstance;

    @Inject
    private SvnSyncTimer svnSyncTimer;

    @Inject
    private ApplianceService applianceService;

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
    private TemplateService templateService;

    @Inject
    private SubversionService subversionService;

    @Inject
    private CustomLibrariesLoader customLibrariesLoader;

    @Inject
    private QuartzSchedulerManager quartzSchedulerManager;

    @Inject
    private LdifArchiver ldifArchiver;

    @Inject
    private BuildVersion buildVersion;

    @Inject
    private LoggerService loggerService;
    
    @Inject
    private CleanerTimer cleanerTimer;

    private FileConfiguration ldapConfig;
    private FileConfiguration ldapCentralConfig;

    private LdapConnectionProviders connectionProviders;
    private LdapConnectionProviders metricConnectionProviders;
    private LdapConnectionProviders centralConnectionProviders;

    private AtomicBoolean isActive;
    private long lastFinishedTime;

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
        log.debug("Creating application components");
        showBuildInfo();

        customLibrariesLoader.init();

        // Initialize local LDAP connection provider
        this.connectionProviders = createConnectionProvider((String) null, false);
        this.metricConnectionProviders = createConnectionProvider(LDAP_METRIC_CONFIG_GROUP_NAME, false);
        
        configurationFactory.create();

        LdapEntryManager localLdapEntryManager = ldapEntryManagerInstance.get();

        // Initialize central LDAP connection provider
        this.centralConnectionProviders = createCentralConnectionProvider();

        initializeLdifArchiver(localLdapEntryManager);

        // Initialize template engine
        templateService.initTemplateEngine();

        // Initialize SubversionService
        subversionService.initSubversionService();

        // Initialize python interpreter
        pythonService.initPythonInterpreter(configurationFactory.getLdapConfiguration().getString("pythonModulesDir", null));

        // Initialize Shibboleth
        shibbolethInitializer.createShibbolethConfiguration();

        // Initialize script manager
        List<CustomScriptType> supportedCustomScriptTypes = Arrays.asList( CustomScriptType.CACHE_REFRESH, CustomScriptType.UPDATE_USER, CustomScriptType.USER_REGISTRATION, CustomScriptType.ID_GENERATOR, CustomScriptType.SCIM );

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
        svnSyncTimer.initTimer();
        logFileSizeChecker.initTimer();
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

    @Produces @ApplicationScoped
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

    public void destroy(@Observes @BeforeDestroyed(ApplicationScoped.class) ServletContext init) {
        log.info("Closing LDAP connection at server shutdown...");
        LdapEntryManager ldapEntryManager = ldapEntryManagerInstance.get();
        closeLdapEntryManager(ldapEntryManager, LDAP_ENTRY_MANAGER_NAME);

        LdapEntryManager ldapMetricEntryManager = ldapMetricEntryManagerInstance.get();
        closeLdapEntryManager(ldapMetricEntryManager, LDAP_METRIC_ENTRY_MANAGER_NAME);

        LdapEntryManager ldapCentralEntryManager = ldapCentralEntryManagerInstance.get();
        if (ldapCentralEntryManager != null) {
            closeLdapEntryManager(ldapCentralEntryManager, LDAP_CENTRAL_ENTRY_MANAGER_NAME);
        }
    }

    private void showBuildInfo() {
        log.info("Build date {}. Code revision {} on {}. Build {}", getGluuBuildDate(),
                getGluuRevisionVersion(), getGluuRevisionDate(), getGluuBuildNumber());
    }

    private LdapConnectionProviders createCentralConnectionProvider() {
        if ((configurationFactory.getLdapCentralConfiguration() != null) && configurationFactory.getAppConfiguration().isUpdateApplianceStatus()) {
            this.ldapCentralConfig = configurationFactory.getLdapCentralConfiguration();

            Properties connectionProperties = (Properties) this.ldapConfig.getProperties();
            LdapConnectionService centralConnectionProvider = createConnectionProvider(connectionProperties);
            log.debug("Created centralConnectionProvider: {}", centralConnectionProvider);
            
            return new LdapConnectionProviders(centralConnectionProvider, null);
        }

        return null;
    }


    private Properties prepareBindConnectionProperties(Properties connectionProperties) {
        // TODO: Use own properties with prefix specified in variable 'bindConfigurationComponentName'
        Properties bindProperties = (Properties) connectionProperties.clone();
        bindProperties.remove("bindDN");
        bindProperties.remove("bindPassword");

        return bindProperties;
    }

    private LdapConnectionService createConnectionProvider(Properties connectionProperties) {
        EncryptionService securityService = encryptionServiceInstance.get();
        LdapConnectionService connectionProvider = new LdapConnectionService(securityService.decryptProperties(connectionProperties));

        return connectionProvider;
    }

    private LdapConnectionService createBindConnectionProvider(Properties bindConnectionProperties, Properties connectionProperties) {
        LdapConnectionService bindConnectionProvider = createConnectionProvider(bindConnectionProperties);
        if (ResultCode.INAPPROPRIATE_AUTHENTICATION.equals(bindConnectionProvider.getCreationResultCode())) {
            log.warn("It's not possible to create authentication LDAP connection pool using anonymous bind. Attempting to create it using binDN/bindPassword");
            bindConnectionProvider = createConnectionProvider(connectionProperties);
        }
        
        return bindConnectionProvider;
    }

    @Produces @ApplicationScoped @Named(LDAP_ENTRY_MANAGER_NAME)
    public LdapEntryManager createLdapEntryManager() {
        LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(this.connectionProviders.getConnectionProvider()));
        log.info("Created {}:{} with provider {}", LDAP_ENTRY_MANAGER_NAME, ldapEntryManager, ldapEntryManager.getLdapOperationService().getConnectionProvider() );
 
        return ldapEntryManager;
     }

    @Produces @Named(LDAP_METRIC_ENTRY_MANAGER_NAME) @ReportMetric @ApplicationScoped
    public LdapEntryManager createLdapMetricEntryManager() {
        LdapEntryManager ldapMetricEntryManager = new LdapEntryManager(new OperationsFacade(this.metricConnectionProviders.getConnectionProvider()));
        log.info("Created {}:{} with provider {}", LDAP_METRIC_ENTRY_MANAGER_NAME, ldapMetricEntryManager, ldapMetricEntryManager.getLdapOperationService().getConnectionProvider());

        return ldapMetricEntryManager;
    }

    @Produces @ApplicationScoped @Named(LDAP_CENTRAL_ENTRY_MANAGER_NAME) @CentralLdap
    public LdapEntryManager createCentralLdapEntryManager() {
        if (this.centralConnectionProviders == null) {
            return new LdapEntryManager();
        }

        LdapEntryManager centralLdapEntryManager = new LdapEntryManager(new OperationsFacade(this.centralConnectionProviders.getConnectionProvider()));
        log.info("Created {}:{} with provider {}", LDAP_CENTRAL_ENTRY_MANAGER_NAME, centralLdapEntryManager, centralLdapEntryManager.getLdapOperationService().getConnectionProvider());

        return centralLdapEntryManager;
    }


    public void recreateLdapEntryManager(@Observes @LdapConfigurationReload String event) {
        this.connectionProviders = recreateLdapEntryManagerImpl(LDAP_ENTRY_MANAGER_NAME, null, false); 
        forceCreateNewEntryManager(ldapEntryManagerInstance, LDAP_ENTRY_MANAGER_NAME);

        this.metricConnectionProviders = recreateLdapEntryManagerImpl(LDAP_METRIC_ENTRY_MANAGER_NAME, LDAP_METRIC_CONFIG_GROUP_NAME, false, ReportMetric.Literal.INSTANCE);
        forceCreateNewEntryManager(ldapMetricEntryManagerInstance, LDAP_METRIC_ENTRY_MANAGER_NAME);
    }

    protected <T> LdapConnectionProviders recreateLdapEntryManagerImpl(String entryManagerName, String configId, boolean createBind, Annotation... qualifiers) {
        // Get existing application scoped instance
        LdapEntryManager oldLdapEntryManager = CdiUtil.getContextBean(beanManager, LdapEntryManager.class, entryManagerName, qualifiers);

        // Recreate components
        LdapConnectionProviders createConnectionProviders = createConnectionProvider(configId, createBind);

        // Close existing connections
        closeLdapEntryManager(oldLdapEntryManager, entryManagerName);
        
        return createConnectionProviders;
    }

    protected <T> void forceCreateNewEntryManager(Instance<T> instance, String entryManagerName) {
        // Force to create new bean
        T ldapEntryManager = instance.get();
        instance.destroy(ldapEntryManager);
        log.info("Recreated instance {}: {}", entryManagerName, ldapEntryManager);
    }

    private LdapConnectionProviders createConnectionProvider(String configId, boolean createBind) {
        Properties connectionProperties = getLdapConfigProperties(configId);
        String logConfigId = StringHelper.isEmpty(configId) ? "" : configId + "-";

        LdapConnectionService connectionProvider = createConnectionProvider(connectionProperties);
        if (!ResultCode.SUCCESS.equals(connectionProvider.getCreationResultCode())) {
            throw new ConfigurationException("Failed to create LDAP connection pool!");
        }
        log.debug("Created {}connectionProvider: {}", logConfigId, connectionProvider);

        LdapConnectionService bindConnectionProvider = null;
        if (createBind) {
            Properties bindConnectionProperties = prepareBindConnectionProperties(connectionProperties);
            bindConnectionProvider = createBindConnectionProvider(bindConnectionProperties, connectionProperties);
            if (!ResultCode.SUCCESS.equals(bindConnectionProvider.getCreationResultCode())) {
                throw new ConfigurationException("Failed to create LDAP connection pool!");
            }
            log.debug("Created {}bindConnectionProvider: {}", logConfigId, bindConnectionProvider);
        }
        
        return new LdapConnectionProviders(connectionProvider, bindConnectionProvider);
    }

    protected Properties getLdapConfigProperties(String configId) {
        Properties connectionProperties = (Properties) configurationFactory.getLdapConfiguration().getProperties();
        if (StringHelper.isNotEmpty(configId)) {
            // Replace properties names 'configId.xyz' to 'configId.xyz' in order to override default values
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

    public void recreateCentralLdapEntryManager(@Observes @LdapCentralConfigurationReload String event) {
        // Get existing application scoped instance
        LdapEntryManager oldCentralLdapEntryManager = CdiUtil.getContextBean(beanManager, LdapEntryManager.class, LDAP_CENTRAL_ENTRY_MANAGER_NAME);

        // Recreate components
        this.centralConnectionProviders = createCentralConnectionProvider();

        // Close existing connections
        closeLdapEntryManager(oldCentralLdapEntryManager, LDAP_CENTRAL_ENTRY_MANAGER_NAME);

        // Force to create new bean
        LdapEntryManager ldapCentralEntryManager = ldapCentralEntryManagerInstance.get();
        ldapEntryManagerInstance.destroy(ldapCentralEntryManager);
        log.info("Recreated instance {}: {}", LDAP_CENTRAL_ENTRY_MANAGER_NAME, ldapCentralEntryManager);
    }

    private void closeLdapEntryManager(LdapEntryManager oldLdapEntryManager, String entryManagerName) {
        // Close existing connections
        if ((oldLdapEntryManager != null) && (oldLdapEntryManager.getLdapOperationService() != null)) {
            log.debug("Attempting to destroy {}:{} with provider {}", entryManagerName, oldLdapEntryManager, oldLdapEntryManager.getLdapOperationService().getConnectionProvider());
            oldLdapEntryManager.destroy();
            log.debug("Destroyed {}:{} with provider {}", entryManagerName, oldLdapEntryManager, oldLdapEntryManager.getLdapOperationService().getConnectionProvider());
        }
    }

    private void initializeLdifArchiver(LdapEntryManager ldapEntryManager) {
        ldifArchiver.init();
        ldapEntryManager.addDeleteSubscriber(ldifArchiver);
    }

    private GluuLdapConfiguration mapLdapConfig(String config) throws IOException {
        try {
            return (GluuLdapConfiguration) jsonService.jsonToObject(config, GluuLdapConfiguration.class);
        } catch (IOException ex) {
            log.error("Failed to parse JSON", ex);
            throw ex;
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

        OpenIdConfigurationResponse openIdConfiguration;
        try {
            openIdConfiguration = client.execOpenIdConfiguration();
        } catch (IOException e) {
            throw new OxIntializationException("Failed to load OpenId configuration!");
        }

        if (openIdConfiguration.getStatus() != 200) {
            throw new OxIntializationException("OpenId configuration response is invalid!");
        }

        return openIdConfiguration;
    }

    @Produces @ApplicationScoped @Named("umaMetadataConfiguration")
    public UmaMetadata initUmaMetadataConfiguration() throws OxIntializationException {
        String umaConfigurationEndpoint = getUmaConfigurationEndpoint();
        if (StringHelper.isEmpty(umaConfigurationEndpoint)) {
            return null;
        }

        UmaMetadataService metaDataConfigurationService = UmaClientFactory.instance().createMetadataService(umaConfigurationEndpoint);
        UmaMetadata metadataConfiguration = metaDataConfigurationService.getMetadata();

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
        if (!umaConfigurationEndpoint.endsWith("uma2-configuration")) {
            umaConfigurationEndpoint += "/.well-known/uma2-configuration";
        }

        return umaConfigurationEndpoint;
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