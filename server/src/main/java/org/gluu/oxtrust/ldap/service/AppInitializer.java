/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.config.ConfigurationFactory.PersistenceConfiguration;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshTimer;
import org.gluu.oxtrust.service.CleanerTimer;
import org.gluu.oxtrust.service.MetricService;
import org.gluu.oxtrust.service.cdi.event.CentralLdap;
import org.gluu.oxtrust.service.custom.LdapCentralConfigurationReload;
import org.gluu.oxtrust.service.logger.LoggerService;
import org.gluu.oxtrust.service.status.ldap.LdapStatusTimer;
import org.gluu.oxtrust.util.BuildVersionService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.exception.OxIntializationException;
import org.xdi.model.custom.script.CustomScriptType;
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
import org.xdi.service.cdi.event.ConfigurationUpdate;
import org.xdi.service.cdi.event.LdapConfigurationReload;
import org.xdi.service.cdi.event.LoggerUpdateEvent;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.service.custom.lib.CustomLibrariesLoader;
import org.xdi.service.custom.script.CustomScriptManager;
import org.xdi.service.timer.QuartzSchedulerManager;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

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

    @Inject @Named(ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME)
    private Instance<PersistenceEntryManager> persistenceEntryManagerInstance;

    @Inject @Named(ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME) @CentralLdap
    private Instance<PersistenceEntryManager> persistenceCentralEntryManagerInstance;

    @Inject
    private Instance<EncryptionService> encryptionServiceInstance;
    
    @Inject
    private ApplicationFactory applicationFactory;

    @Inject
    private SvnSyncTimer svnSyncTimer;

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
    private BuildVersionService buildVersionService;

    @Inject
    private LoggerService loggerService;
    
    @Inject
    private CleanerTimer cleanerTimer;

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
        log.debug("Initializing application services");
        showBuildInfo();

        customLibrariesLoader.init();

        configurationFactory.create();

        PersistenceEntryManager localLdapEntryManager = persistenceEntryManagerInstance.get();
        initializeLdifArchiver(localLdapEntryManager);

        // Initialize template engine
        templateService.initTemplateEngine();

        // Initialize SubversionService
        subversionService.initSubversionService();

        // Initialize python interpreter
        pythonService.initPythonInterpreter(configurationFactory.getPersistenceConfiguration().getConfiguration().getString("pythonModulesDir", null));

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

    private void showBuildInfo() {
        log.info("Build date {}. Code revision {} on {}. Build {}", getGluuBuildDate(),
                getGluuRevisionVersion(), getGluuRevisionDate(), getGluuBuildNumber());
    }

    @Produces @ApplicationScoped @Named(ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME)
    public PersistenceEntryManager createPersistenceEntryManager() {
        PersistenceConfiguration persistenceConfiguration = this.configurationFactory.getPersistenceConfiguration();
        FileConfiguration persistenceConfig = persistenceConfiguration.getConfiguration();
        Properties connectionProperties = (Properties) persistenceConfig.getProperties();

        EncryptionService securityService = encryptionServiceInstance.get();
        Properties decryptedConnectionProperties = securityService.decryptAllProperties(connectionProperties);

        PersistenceEntryManager persistenceEntryManager = applicationFactory.getPersistenceEntryManagerFactory().createEntryManager(decryptedConnectionProperties);
        log.info("Created {}: {}", new Object[] { ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME, persistenceEntryManager });

        return persistenceEntryManager;
    }

    @Produces @ApplicationScoped @Named(ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME) @CentralLdap
    public PersistenceEntryManager createCentralLdapEntryManager() {
        if (!((configurationFactory.getLdapCentralConfiguration() != null) && configurationFactory.getAppConfiguration().isUpdateApplianceStatus())) {
            return new LdapEntryManager();
        }

    	FileConfiguration ldapCentralConfig = configurationFactory.getLdapCentralConfiguration();
        Properties centralConnectionProperties = (Properties) ldapCentralConfig.getProperties();

        EncryptionService securityService = encryptionServiceInstance.get();
        Properties decryptedCentralConnectionProperties = securityService.decryptProperties(centralConnectionProperties);

        // TODO: Review if it works well with couchbase
        PersistenceEntryManager centralLdapEntryManager = applicationFactory.getPersistenceEntryManagerFactory().createEntryManager(decryptedCentralConnectionProperties);
        log.info("Created {}: {}", new Object[] { ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME, centralLdapEntryManager.getOperationService() });

        return centralLdapEntryManager;
    }

    public void recreateLdapEntryManager(@Observes @LdapConfigurationReload String event) {
        // Get existing application scoped instance
        PersistenceEntryManager oldLdapEntryManager = CdiUtil.getContextBean(beanManager, PersistenceEntryManager.class, ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME);

        // Close existing connections
        closeLdapEntryManager(oldLdapEntryManager);

        // Force to create new bean
        PersistenceEntryManager ldapEntryManager = persistenceEntryManagerInstance.get();
        persistenceEntryManagerInstance.destroy(ldapEntryManager);
        log.info("Recreated instance {}: {}", ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME, ldapEntryManager);
    }

    public void recreateCentralLdapEntryManager(@Observes @LdapCentralConfigurationReload String event) {
        // Get existing application scoped instance
        PersistenceEntryManager oldCentralLdapEntryManager = CdiUtil.getContextBean(beanManager, PersistenceEntryManager.class, ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME);

        // Close existing connections
        closeLdapEntryManager(oldCentralLdapEntryManager);

        // Force to create new bean
        PersistenceEntryManager ldapCentralEntryManager = persistenceCentralEntryManagerInstance.get();
        persistenceEntryManagerInstance.destroy(ldapCentralEntryManager);
        log.info("Recreated instance {}: {}", ApplicationFactory.PERSISTENCE_CENTRAL_ENTRY_MANAGER_NAME, ldapCentralEntryManager);
    }

    private void closeLdapEntryManager(PersistenceEntryManager oldLdapEntryManager) {
        // Close existing connections
        log.debug("Attempting to destroy {}: {}", ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME, oldLdapEntryManager);
        oldLdapEntryManager.destroy();
        log.debug("Destroyed {}: {}", ApplicationFactory.PERSISTENCE_ENTRY_MANAGER_NAME, oldLdapEntryManager);
    }

    private void initializeLdifArchiver(PersistenceEntryManager ldapEntryManager) {
        ldifArchiver.init();
        ldapEntryManager.addDeleteSubscriber(ldifArchiver);
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

    public void destroy(@Observes @BeforeDestroyed(ApplicationScoped.class) ServletContext init) {
        log.info("Closing LDAP connection at server shutdown...");
        PersistenceEntryManager ldapEntryManager = persistenceEntryManagerInstance.get();
        closeLdapEntryManager(ldapEntryManager);


        PersistenceEntryManager ldapCentralEntryManager = persistenceCentralEntryManagerInstance.get();
        if (ldapCentralEntryManager != null) {
            closeLdapEntryManager(ldapCentralEntryManager);
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