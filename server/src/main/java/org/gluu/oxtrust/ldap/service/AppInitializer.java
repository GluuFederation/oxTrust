/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import com.unboundid.ldap.sdk.ResultCode;
import java.io.IOException;
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

import com.unboundid.ldap.sdk.ResultCode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshTimer;
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
import org.xdi.config.oxtrust.AppConfiguration;
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
import org.xdi.service.cdi.event.ConfigurationUpdate;
import org.xdi.service.cdi.event.LdapConfigurationReload;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.service.custom.lib.CustomLibrariesLoader;
import org.xdi.service.custom.script.CustomScriptManager;
import org.xdi.service.ldap.LdapConnectionService;
import org.xdi.service.timer.QuartzSchedulerManager;
import org.xdi.service.timer.event.TimerEvent;
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

    public static final String LDAP_ENTRY_MANAGER_NAME = "ldapEntryManager";
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

    private FileConfiguration ldapConfig;
    private FileConfiguration ldapCentralConfig;
    private LdapConnectionService connectionProvider;
    private LdapConnectionService centralConnectionProvider;

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
        createConnectionProvider();

        configurationFactory.create();
        LdapEntryManager localLdapEntryManager = ldapEntryManagerInstance.get();

        // Initialize central LDAP connection provider
        createCentralConnectionProvider();

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
        quartzSchedulerManager.start();

        // Schedule timer tasks
        metricService.initTimer();
        configurationFactory.initTimer();
        ldapStatusTimer.initTimer();
        metadataValidationTimer.initTimer();
        entityIDMonitoringService.initTimer();
        cacheRefreshTimer.initTimer();
        customScriptManager.initTimer(supportedCustomScriptTypes);
        statusCheckerDaily.initTimer();
        statusCheckerTimer.initTimer();
        svnSyncTimer.initTimer();
        logFileSizeChecker.initTimer();

        loggerService.updateLoggerConfigLocation();
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
        closeLdapEntryManager(ldapEntryManager);


        LdapEntryManager ldapCentralEntryManager = ldapCentralEntryManagerInstance.get();
        if (ldapCentralEntryManager != null) {
            closeLdapEntryManager(ldapCentralEntryManager);
        }
    }

    private void showBuildInfo() {
        log.info("Build date {}. Code revision {} on {}. Build {}", getGluuBuildDate(),
                getGluuRevisionVersion(), getGluuRevisionDate(), getGluuBuildNumber());
    }

    private void createConnectionProvider() {
        this.ldapConfig = configurationFactory.getLdapConfiguration();

        Properties connectionProperties = (Properties) this.ldapConfig.getProperties();
        this.connectionProvider = createConnectionProvider(connectionProperties);
        if (!ResultCode.SUCCESS.equals(this.connectionProvider.getCreationResultCode())) {
            throw new ConfigurationException("Failed to create LDAP connection pool!");
        }
        log.debug("Created connectionProvider: {}", connectionProvider);
    }

    private void createCentralConnectionProvider() {
        if ((configurationFactory.getLdapCentralConfiguration() != null) && configurationFactory.getAppConfiguration().isUpdateApplianceStatus()) {
            this.ldapCentralConfig = configurationFactory.getLdapCentralConfiguration();

            Properties connectionProperties = (Properties) this.ldapConfig.getProperties();
            this.centralConnectionProvider = createConnectionProvider(connectionProperties);
            log.debug("Created centralConnectionProvider: {}", centralConnectionProvider);
        }
    }

    private LdapConnectionService createConnectionProvider(Properties connectionProperties) {
        EncryptionService securityService = encryptionServiceInstance.get();
        LdapConnectionService connectionProvider = new LdapConnectionService(securityService.decryptProperties(connectionProperties));

        return connectionProvider;
    }

    @Produces @ApplicationScoped @Named(LDAP_ENTRY_MANAGER_NAME)
    public LdapEntryManager getLdapEntryManager() {
        LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(this.connectionProvider));
        log.info("Created {}: {}", new Object[] { LDAP_ENTRY_MANAGER_NAME, ldapEntryManager.getLdapOperationService() });

        return ldapEntryManager;
    }

    @Produces @ApplicationScoped @Named(LDAP_CENTRAL_ENTRY_MANAGER_NAME) @CentralLdap
    public LdapEntryManager createCentralLdapEntryManager() {
        if (this.centralConnectionProvider == null) {
            return new LdapEntryManager();
        }

        LdapEntryManager centralLdapEntryManager = new LdapEntryManager(new OperationsFacade(this.centralConnectionProvider));
        log.info("Created {}: {}", new Object[] { LDAP_CENTRAL_ENTRY_MANAGER_NAME, centralLdapEntryManager.getLdapOperationService() });

        return centralLdapEntryManager;
    }

    public void recreateLdapEntryManager(@Observes @LdapConfigurationReload String event) {
        // Get existing application scoped instance
        LdapEntryManager oldLdapEntryManager = CdiUtil.getContextBean(beanManager, LdapEntryManager.class, LDAP_ENTRY_MANAGER_NAME);

        // Recreate components
        createConnectionProvider();

        // Close existing connections
        closeLdapEntryManager(oldLdapEntryManager);

        // Force to create new bean
        LdapEntryManager ldapEntryManager = ldapEntryManagerInstance.get();
        ldapEntryManagerInstance.destroy(ldapEntryManager);
        log.info("Recreated instance {}: {}", LDAP_ENTRY_MANAGER_NAME, ldapEntryManager);
    }

    public void recreateCentralLdapEntryManager(@Observes @LdapCentralConfigurationReload String event) {
        // Get existing application scoped instance
        LdapEntryManager oldCentralLdapEntryManager = CdiUtil.getContextBean(beanManager, LdapEntryManager.class, LDAP_CENTRAL_ENTRY_MANAGER_NAME);

        // Recreate components
        createCentralConnectionProvider();

        // Close existing connections
        closeLdapEntryManager(oldCentralLdapEntryManager);

        // Force to create new bean
        LdapEntryManager ldapCentralEntryManager = ldapCentralEntryManagerInstance.get();
        ldapEntryManagerInstance.destroy(ldapCentralEntryManager);
        log.info("Recreated instance {}: {}", LDAP_CENTRAL_ENTRY_MANAGER_NAME, ldapCentralEntryManager);
    }

    private void closeLdapEntryManager(LdapEntryManager oldLdapEntryManager) {
        // Close existing connections
        if ((oldLdapEntryManager != null) && (oldLdapEntryManager.getLdapOperationService() != null)) {
            log.debug("Attempting to destroy {}: {}", LDAP_ENTRY_MANAGER_NAME, oldLdapEntryManager);
            oldLdapEntryManager.destroy();
            log.debug("Destroyed {}: {}", LDAP_ENTRY_MANAGER_NAME, oldLdapEntryManager);
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

    public void updateLoggingSeverity(@Observes @ConfigurationUpdate AppConfiguration appConfiguration) {
        String loggingLevel = appConfiguration.getLoggingLevel();
        if (StringHelper.isEmpty(loggingLevel)) {
            return;
        }

        log.info("Setting loggers level to: '{}'", loggingLevel);

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