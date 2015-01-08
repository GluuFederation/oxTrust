/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.net.URISyntaxException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshConfiguration;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.gluu.oxtrust.model.scim.ScimCustomAttributes;
import org.gluu.oxtrust.service.custom.ExtendedCustomScriptManager;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Version;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.exception.OxIntializationException;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryClient;
import org.xdi.oxauth.client.OpenIdConnectDiscoveryResponse;
import org.xdi.oxauth.client.uma.MetaDataConfigurationService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.model.uma.MetadataConfiguration;
import org.xdi.service.PythonService;
import org.xdi.service.ldap.LdapConnectionService;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

/**
 * Perform startup time initialization. Provides factory methods for non Seam
 * components.
 * 
 * @author Yuriy Movchan
 */
@Startup(depends = "oxTrustConfiguration")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("appInitializer")
public class AppInitializer {

	// We are going to start connection checker every 120 seconds
	public static final long CONNECTION_CHECKER_INTERVAL = (long) (1000L * 60 * 2);
	// We are going to start status checker every 60 seconds
	public static final long STATUS_CHECKER_INTERVAL = (long) (1000L * 61 * 1);
	// We are going to start svn synchronization every 5 minutes
	public static final long SVN_SYNC_INTERVAL = (long) (1000L * 62 * 5);

	private static final long VALIDATION_INTERVAL = (long) (1000L * 63 * 1);
	private static final long LOG_MONITOR_INTERVAL = (long) (1000L * 60 * 64 * 24);

	// Group count and person count will now be checked daily
	public static final long STATUS_CHECKER_DAILY = (long) (1000L * 60 * 65 * 24);

	@Logger
	private Log log;

	@In
	private SvnSyncTimer svnSyncTimer;

	@In
	private StatusCheckerTimer statusCheckerTimer;

	@In
	private StatusCheckerDaily statusCheckerDaily;

	@In
	private MetadataValidationTimer metadataValidationTimer;

	@In
	private LogFileSizeChecker logFileSizeChecker;
	
	@In
	private OxTrustConfiguration oxTrustConfiguration;

	private GluuLdapConfiguration ldapConfig;

	/**
	 * Initialize components and schedule DS connection time checker
	 */
	@Create
	public void createApplicationComponents() throws ConfigurationException {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		log.debug("Creating application components");
		showBuildInfo();

		// Initialize local LDAP connection provider
		createConnectionProvider(oxTrustConfiguration.getLdapConfiguration(), "localLdapConfiguration", "connectionProvider");

		Events.instance().raiseEvent(OxTrustConfiguration.EVENT_INIT_CONFIGURATION);

		// Initialize central LDAP connection provider
		
		if(oxTrustConfiguration.getApplicationConfiguration().isUpdateApplianceStatus()){
			createConnectionProvider(oxTrustConfiguration.getLdapCentralConfiguration(), "centralLdapConfiguration", "centralConnectionProvider");
		}
		initializeLdifArchiver();
		initiateLDAPAuthConf();

		// Initialize template engine
		TemplateService.instance().initTemplateEngine();

		// Initialize SubversionService
		SubversionService.instance().initSubversionService();

		// Initialize python interpreter
		PythonService.instance().initPythonInterpreter();

//		checkAndUpdateLdapbaseConfiguration(); // We do not need to create ldapbase configuration any more because we 
											   //supply working ldap data with either dashboard or python setup sript.
		
		startInviteCodesExpirationService();

		
		startStatusChecker();
		startDailyStatusChecker();
		startSvnSync();
		// Asynchronous metadata validation service
		startMetadataValidator();

		createShibbolethConfiguration();

		prepareConfigurations();

		logSizeChecker();

		List<CustomScriptType> supportedCustomScriptTypes = Arrays.asList( CustomScriptType.CACHE_REFRESH, CustomScriptType.USER_REGISTRATION );
        ExtendedCustomScriptManager.instance().init(supportedCustomScriptTypes);
	}

	private void startInviteCodesExpirationService() {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 60);
		GluuOrganization org = OrganizationService.instance().getOrganization();
		RegistrationConfiguration config = org.getOxRegistrationConfiguration();
		boolean accountsTimeLimited;
		long linksExpirationFrequency;
		long accountsExpirationServiceFrequency;
		if(config != null){
			log.debug("OxRegistrationConfiguration found. Trying to get the custom expiration configuration");
			try{
				linksExpirationFrequency = Long.parseLong(config.getLinksExpirationFrequency());
				accountsExpirationServiceFrequency = Long.parseLong(config.getAccountsExpirationServiceFrequency());
				accountsTimeLimited = config.isAccountsTimeLimited();
			}catch (NumberFormatException e) {
				log.debug("OxRegistrationConfiguration malformed. Resorting to the default values");

				linksExpirationFrequency = RegistrationsExpirationService.instance().getDefaultLinksExpirationFrequency();
				accountsExpirationServiceFrequency = RegistrationsExpirationService.instance().getDefaultAccountsExpirationServiceFrequency();
				accountsTimeLimited = false;
			}
		}else{
			log.debug("OxRegistrationConfiguration missing. Resorting to the default values");
			linksExpirationFrequency = RegistrationsExpirationService.instance().getDefaultLinksExpirationFrequency();
			accountsExpirationServiceFrequency = RegistrationsExpirationService.instance().getDefaultAccountsExpirationServiceFrequency();
			accountsTimeLimited = false;
		}
		log.debug("OxRegistrationConfiguration parsed: linksExpirationFrequency: " + linksExpirationFrequency + ", accountsExpirationServiceFrequency: " + accountsExpirationServiceFrequency + ", accountsTimeLimited: " + accountsTimeLimited);

		RegistrationsExpirationService.instance().expireLinks(calendar.getTime(), linksExpirationFrequency);
		if(accountsTimeLimited){
			RegistrationsExpirationService.instance().expireUsers(calendar.getTime(), accountsExpirationServiceFrequency);
		}

	}

	private void createConnectionAuthProvider(String configurationLdapConfigComponentName, String fileName, String configurationComponentName, String connectionProviderComponentName) {
		FileConfiguration configuration = new FileConfiguration(fileName);
		Contexts.getApplicationContext().set(configurationComponentName, configuration);
		
		//
		Properties properties = configuration.getProperties();
		if (this.ldapConfig != null) {
            Contexts.getApplicationContext().set(configurationLdapConfigComponentName, this.ldapConfig);

			properties.setProperty("servers", buildServersString(this.ldapConfig.getServers()));
			properties.setProperty("bindDN", this.ldapConfig.getBindDN());
			properties.setProperty("bindPassword", this.ldapConfig.getBindPassword());
			properties.setProperty("useSSL", Boolean.toString(this.ldapConfig.isUseSSL()));
		}
		//

		LdapConnectionService connectionProvider = new LdapConnectionService(PropertiesDecrypter.decryptProperties(properties, oxTrustConfiguration.getCryptoConfiguration().getEncodeSalt()));
		Contexts.getApplicationContext().set(connectionProviderComponentName, connectionProvider);
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

	private void prepareConfigurations() {
		CacheRefreshConfiguration cacheRefreshConfiguration = new CacheRefreshConfiguration();
		Contexts.getApplicationContext().set("cacheRefreshConfiguration", cacheRefreshConfiguration);
	}

	private boolean createShibbolethConfiguration() {
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		boolean createConfig = applicationConfiguration.isConfigGeneration();
		log.info("IDP config generation is set to " + createConfig);
		if (createConfig) {
			String gluuSPInum = null;
			GluuSAMLTrustRelationship gluuSP;
			try {
				gluuSPInum = ApplianceService.instance().getAppliance().getGluuSPTR();
				gluuSP = new GluuSAMLTrustRelationship();
				gluuSP.setDn(TrustService.instance().getDnForTrustRelationShip(gluuSPInum));
			} catch (EntryPersistenceException ex) {
				log.error("Failed to determine SP inum", ex);
				return false;
			}

			boolean servicesNeedRestarting = false;
			if (gluuSPInum == null || gluuSP == null || ! TrustService.instance().containsTrustRelationship(gluuSP)) {
				log.info("No trust relationships exist in LDAP. Adding gluuSP");
				GluuAppliance appliance = ApplianceService.instance().getAppliance();
				appliance.setGluuSPTR(null);
				ApplianceService.instance().updateAppliance(appliance);
				TrustService.instance().addGluuSP();
				servicesNeedRestarting = true;
			}
			gluuSP = TrustService.instance().getRelationshipByInum(
					ApplianceService.instance().getAppliance().getGluuSPTR());

			List<GluuSAMLTrustRelationship> trustRelationships = TrustService.instance().getAllActiveTrustRelationships();
			Shibboleth2ConfService.instance().generateIdpConfigurationFiles();
			Shibboleth2ConfService.instance().generateConfigurationFiles(trustRelationships);
			Shibboleth2ConfService.instance().generateMetadataFiles(gluuSP);

			Shibboleth2ConfService.instance().removeUnusedCredentials();
			Shibboleth2ConfService.instance().removeUnusedMetadata();
			if (servicesNeedRestarting) {
				ApplianceService.instance().restartServices();
			}

		}

		return true;
	}

	private void showBuildInfo() {
		log.info("Build date {0}. Code revision {1} on {2}. Build {3}", OxTrustConstants.getGluuBuildDate(),
				OxTrustConstants.getGluuSvnRevisionVersion(), OxTrustConstants.getGluuSvnRevisionDate(), Version.GLUU_HUDSON_BUILDNO);
	}

	private void createConnectionProvider(FileConfiguration configuration, String configurationComponentName, String connectionProviderComponentName)
			throws ConfigurationException {
		Contexts.getApplicationContext().set(configurationComponentName, configuration);

		LdapConnectionService connectionProvider = null;
		if (configuration != null) {
			connectionProvider = new LdapConnectionService(PropertiesDecrypter.decryptProperties(configuration
				.getProperties(), oxTrustConfiguration.getCryptoConfiguration().getEncodeSalt()));
		}
		Contexts.getApplicationContext().set(connectionProviderComponentName, connectionProvider);
	}

	/**
	 * Schedule status checker
	 */
	private void startStatusChecker() {
		// Schedule first check after 60 seconds
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 60);

		statusCheckerTimer.scheduleStatusChecking(calendar.getTime(), STATUS_CHECKER_INTERVAL);
	}

	private void startDailyStatusChecker() {
		// Schedule first check after 60 seconds
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 10*60);

		statusCheckerDaily.scheduleStatusChecking(calendar.getTime(), STATUS_CHECKER_DAILY);
	}

	private void startSvnSync() {
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
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
	@Destroy
	public void destroyApplicationComponents() throws ConfigurationException {
		log.debug("Destroying application components");
		LdapConnectionService connectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("connectionProvider");
		connectionProvider.closeConnectionPool();

		LdapConnectionService authConnectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("authConnectionProvider");
		authConnectionProvider.closeConnectionPool();

		LdapConnectionService centralConnectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get(
				"centralConnectionProvider");
		if (centralConnectionProvider != null) {
			centralConnectionProvider.closeConnectionPool();
		}
	}

	@Factory(value = "ldapEntryManager", scope = ScopeType.APPLICATION, autoCreate = true)
	public LdapEntryManager createLdapEntryManager() {
		LdapConnectionService connectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("connectionProvider");
		LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider));
		log.debug("Created site LdapEntryManager: " + ldapEntryManager);

		// Initialize local LDAP Authentication connection provider
		createConnectionAuthProvider("ldapAuthConfig", OxTrustConstants.CONFIGURATION_FILE_LOCAL_LDAP_PROPERTIES_FILE, "localLdapAuthConfiguration",
				"authConnectionProvider");

		return ldapEntryManager;
	}

	@Factory(value = "centralLdapEntryManager", scope = ScopeType.APPLICATION, autoCreate = true)
	public LdapEntryManager createCentralLdapEntryManager() {
		LdapConnectionService centralConnectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get(
				"centralConnectionProvider");
		if (centralConnectionProvider == null) {
			return null;
		}

		LdapEntryManager centralLdapEntryManager = new LdapEntryManager(new OperationsFacade(centralConnectionProvider));
		log.debug("Created central LdapEntryManager: " + centralLdapEntryManager);

		return centralLdapEntryManager;
	}

	@Factory(value = "ldapAuthEntryManager", scope = ScopeType.APPLICATION, autoCreate = true)
	public LdapEntryManager createLdapAuthEntryManager() {
		LdapConnectionService connectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("authConnectionProvider");
		LdapEntryManager ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider, null));
		// ldapEntryManager.addDeleteSubscriber(new
		// LdifArchiver(ldapEntryManager));
		log.debug("Created site LdapAuthEntryManager: " + ldapEntryManager);

		return ldapEntryManager;
	}

	private void initializeLdifArchiver() {
		LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");
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

	public void initiateLDAPAuthConf() {
		GluuAppliance appliance = null;
		
		appliance = ApplianceService.instance().getAppliance();

		if (appliance == null) {
			return;
		}

		List<OxIDPAuthConf> idpConfs = appliance.getOxIDPAuthentication();
		if (idpConfs == null) {
			log.warn("Appliance entry in database does not contain authentication configuration. Guessing local ldap.");
			idpConfs = new ArrayList<OxIDPAuthConf>();
			OxIDPAuthConf oxIDPAuthentication = new OxIDPAuthConf();
			oxIDPAuthentication.setType("ldap");
			oxIDPAuthentication.setName("Ldap authentication");
			oxIDPAuthentication.setLevel(0);
			oxIDPAuthentication.setPriority(1);
			oxIDPAuthentication.setEnabled(true);
			oxIDPAuthentication.setVersion(0);
			List<ScimCustomAttributes> fields = new ArrayList<ScimCustomAttributes>();
			ScimCustomAttributes attribute = null;
			List<String> values = null;

			LdapConnectionService connectionProvider = (LdapConnectionService) Contexts.getApplicationContext().get("connectionProvider");

			attribute = new ScimCustomAttributes();
			attribute.setName("ldapHost");
			values = Arrays.asList(connectionProvider.getServers());
			attribute.setValues(values);
			fields.add(attribute);

			attribute = new ScimCustomAttributes();
			attribute.setName("ldapPort");
			values = new ArrayList<String>();
			for(int i : connectionProvider.getPorts() ){
				values.add(Integer.toString(i));
			}
			attribute.setValues(values);
			fields.add(attribute);

			attribute = new ScimCustomAttributes();
			attribute.setName("ldapBindDn");
			values = new ArrayList<String>();
			values.add(connectionProvider.getBindDn());
			attribute.setValues(values);
			fields.add(attribute);

			attribute = new ScimCustomAttributes();
			attribute.setName("ldapBindPw");
			values = new ArrayList<String>();
			values.add(connectionProvider.getBindPassword());
			attribute.setValues(values);
			fields.add(attribute);

			attribute = new ScimCustomAttributes();
			attribute.setName("ldapUseSSL");
			values = new ArrayList<String>();
			values.add(Boolean.toString(connectionProvider.isUseSSL()));
			attribute.setValues(values);
			fields.add(attribute);
			
			oxIDPAuthentication.setFields(fields);
			idpConfs.add(oxIDPAuthentication);
		}

		for (OxIDPAuthConf oneConf : idpConfs) {
			if (oneConf.getType().equalsIgnoreCase("ldap")) {
				try {
					this.ldapConfig = mapLdapOldConfig(oneConf);
				} catch (Exception ex) {
					log.error("Failed to load LDAP authentication server connection details", ex);
				}
			} else if (oneConf.getType().equalsIgnoreCase("auth")) {
				try {
					this.ldapConfig = mapLdapConfig(oneConf.getConfig());
				} catch (Exception ex) {
					log.error("Failed to load LDAP authentication server connection details", ex);
				}
			}
		}
	}

	@Factory(value ="openIdConfiguration", scope=ScopeType.APPLICATION, autoCreate = true)
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

	@Factory(value ="umaMetadataConfiguration", scope=ScopeType.APPLICATION, autoCreate = true)
	public MetadataConfiguration initUmaMetadataConfiguration() throws OxIntializationException {
		String umaConfigurationEndpoint = getUmaConfigurationEndpoint();
		if (StringHelper.isEmpty(umaConfigurationEndpoint)) {
			return null;
		}

		MetaDataConfigurationService metaDataConfigurationService = UmaClientFactory.instance().createMetaDataConfigurationService(umaConfigurationEndpoint);
		MetadataConfiguration metadataConfiguration = metaDataConfigurationService.getMetadataConfiguration();

        if (metadataConfiguration == null) {
			throw new OxIntializationException("UMA meta data configuration is invalid!");
        }
        
        return metadataConfiguration;
	}

	public String getUmaConfigurationEndpoint() {
		String umaIssuer = this.oxTrustConfiguration.getApplicationConfiguration().getUmaIssuer();
		if (StringHelper.isEmpty(umaIssuer)) {
			log.info("oxAuth UMA issuer isn't specified");
			return null;
		}

		String umaConfigurationEndpoint = umaIssuer;
		if (!umaConfigurationEndpoint.endsWith("uma-configuration")) {
			umaConfigurationEndpoint += "/.well-known/uma-configuration";
		}

		return umaConfigurationEndpoint;
	}

}
