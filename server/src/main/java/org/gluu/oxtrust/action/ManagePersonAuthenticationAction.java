/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.gluu.persist.model.base.GluuBoolean;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.model.passport.FieldSet;
import org.xdi.model.passport.PassportConfiguration;
import org.xdi.service.custom.script.AbstractCustomScriptService;
import org.xdi.service.security.Secure;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Action class for configuring person authentication
 * 
 * @author Yuriy Movchan Date: 16/11/2010
 */
@Named("managePersonAuthenticationAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ManagePersonAuthenticationAction
		implements SimplePropertiesListModel, LdapConfigurationModel, Serializable {

	private static final long serialVersionUID = -4470460481895022468L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private AbstractCustomScriptService customScriptService;

	@Inject
	private PassportService passportService;
	
	@Inject
	private ConfigurationFactory configurationFactory;
	
	@Inject
	private EncryptionService encryptionService;

	private boolean existLdapConfigIdpAuthConf;

	private List<CustomScript> customScripts;

	private List<GluuLdapConfiguration> sourceConfigs;

	private GluuLdapConfiguration activeLdapConfig;

	private String authenticationMode = "auth_ldap_server";
	private String oxTrustAuthenticationMode;

	private List<String> customAuthenticationConfigNames;

	private boolean initialized;

	private GluuBoolean passportEnable = GluuBoolean.DISABLED;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;

	private List<PassportConfiguration> ldapPassportConfigurations;

	public List<PassportConfiguration> getLdapPassportConfigurations() {
		return ldapPassportConfigurations;
	}

	public void setLdapPassportConfigurations(
			List<PassportConfiguration> ldapPassportConfigurations) {
		this.ldapPassportConfigurations = ldapPassportConfigurations;
	}

	public String modify() {
		String outcome = modifyImpl();
		
		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to prepare for person authentication configuration update");
			conversationService.endConversation();
		}
		
		return outcome;
	}

	public String modifyImpl() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			GluuAppliance appliance = applianceService.getAppliance();

			if (appliance == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}
			passportEnable = appliance.getPassportEnabled();
			log.info("passport enabled value  : '{}'", passportEnable);
			this.customScripts = customScriptService.findCustomScripts(
					Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION), "displayName", "oxLevel", "gluuStatus");

			List<OxIDPAuthConf> list = getIDPAuthConfOrNull(appliance);
			this.sourceConfigs = new ArrayList<GluuLdapConfiguration>();
			if (list != null) {
				for(OxIDPAuthConf oxIDPAuthConf : list){
					GluuLdapConfiguration oxldapConfig = mapLdapConfig(oxIDPAuthConf.getConfig());
					this.sourceConfigs.add(oxldapConfig);
				}
			}

			this.authenticationMode = appliance.getAuthenticationMode();
			this.oxTrustAuthenticationMode = appliance.getOxTrustAuthenticationMode();

			ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			if (ldapOxPassportConfiguration == null) {
				ldapOxPassportConfiguration = new LdapOxPassportConfiguration();
			}
			this.ldapPassportConfigurations = ldapOxPassportConfiguration.getPassportConfigurations();
		} catch (Exception ex) {
			log.error("Failed to load appliance configuration", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws JsonParseException, JsonMappingException, IOException {
		try {
			// Reload entry to include latest changes
			GluuAppliance appliance = applianceService.getAppliance();

 			boolean updateAuthenticationMode = false;
 			boolean updateOxTrustAuthenticationMode = false;
 			
 			String oldAuthName = getFirstConfigName(appliance.getOxIDPAuthentication());
			if (oldAuthName != null) {
				if (oldAuthName.equals(this.authenticationMode)) {
					updateAuthenticationMode = true;
				}
				if (oldAuthName.equals(this.oxTrustAuthenticationMode)) {
					updateOxTrustAuthenticationMode = true;
				}
			}

			updateAuthConf(appliance);

 			String newAuthName = getFirstConfigName(appliance.getOxIDPAuthentication());
			String updatedAuthMode = updateAuthenticationMode ? newAuthName : this.authenticationMode;
			String updatedOxTrustAuthMode = updateOxTrustAuthenticationMode ? newAuthName : this.oxTrustAuthenticationMode;
			appliance.setAuthenticationMode(updatedAuthMode);
			appliance.setOxTrustAuthenticationMode(updatedOxTrustAuthMode);

			appliance.setPassportEnabled(passportEnable);

			applianceService.updateAppliance(appliance);

			ldapOxPassportConfiguration.setPassportConfigurations(ldapPassportConfigurations);

			passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
		} catch (BaseMappingException ex) {
			log.error("Failed to update appliance configuration", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update appliance");
			return OxTrustConstants.RESULT_FAILURE;
		}

		reset();

		facesMessages.add(FacesMessage.SEVERITY_INFO, "Person authentication configuration updated successfully");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String getFirstConfigName(List<OxIDPAuthConf> idpConfs) {
		if ((idpConfs == null) || idpConfs.isEmpty()) {
			return null;
		}

		return idpConfs.get(0).getName();
	}

	private void reset() {
		this.customAuthenticationConfigNames = null;
	}

	private GluuLdapConfiguration mapLdapConfig(String config)
			throws JsonParseException, JsonMappingException, IOException {
		return (GluuLdapConfiguration) jsonToObject(config, GluuLdapConfiguration.class);
	}

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Person authentication configuration not updated");
		conversationService.endConversation();
		
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private Object jsonToObject(String json, Class<?> clazz)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object clazzObject = mapper.readValue(json, clazz);
		return clazzObject;
	}

	private String objectToJson(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);
	}

	public boolean updateAuthConf(GluuAppliance appliance) {
		try {
			String configId = null;
			List<OxIDPAuthConf> idpConf = new ArrayList<OxIDPAuthConf>();
			for (GluuLdapConfiguration ldapConfig : this.sourceConfigs) {
				if (idpConf.isEmpty()) {
					configId = ldapConfig.getConfigId();
				}
				if (ldapConfig.isUseAnonymousBind()) {
					ldapConfig.setBindDN(null);
				}

				OxIDPAuthConf ldapConfigIdpAuthConf = new OxIDPAuthConf();
				ldapConfig.setConfigId(configId);
				ldapConfig.updateStringsLists();
				ldapConfigIdpAuthConf.setType("auth");
				ldapConfigIdpAuthConf.setVersion(ldapConfigIdpAuthConf.getVersion() + 1);
				ldapConfigIdpAuthConf.setName(configId);
				ldapConfigIdpAuthConf.setEnabled(ldapConfig.isEnabled());
				ldapConfigIdpAuthConf.setConfig(objectToJson(ldapConfig));

				idpConf.add(ldapConfigIdpAuthConf);
			}

			appliance.setOxIDPAuthentication(idpConf);
		} catch (Exception ex) {
			log.error("An Error occured ", ex);

			return false;
		}

		return true;
	}

	public List<String> getPersonAuthenticationConfigurationNames() {
		if (this.customAuthenticationConfigNames == null) {
			this.customAuthenticationConfigNames = new ArrayList<String>();
			for (CustomScript customScript : this.customScripts) {
				if (customScript.isEnabled()) {
					String name = customScript.getName();
					if (StringHelper.isEmpty(name)) {
						continue;
					}

					this.customAuthenticationConfigNames.add(customScript.getName());
				}
			}
			
			boolean internalServerName = true;
			
			for (GluuLdapConfiguration ldapConfig : this.sourceConfigs) {
				if ((ldapConfig != null) && StringHelper.isNotEmpty(ldapConfig.getConfigId())) {
					this.customAuthenticationConfigNames.add(ldapConfig.getConfigId());
					internalServerName = false;
					break;
				}
			}

			if (internalServerName) {
				this.customAuthenticationConfigNames.add(OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME);
			}
		}

		return this.customAuthenticationConfigNames;
	}

	public String testLdapConnection(GluuLdapConfiguration ldapConfig) {
		try {
			FileConfiguration configuration = new FileConfiguration(ConfigurationFactory.LDAP_PROPERTIES_FILE);
			if (!configuration.isLoaded()) {
				configuration = new FileConfiguration(ConfigurationFactory.LDAP_DEFAULT_PROPERTIES_FILE);
			}
			Properties properties = configuration.getProperties();
			properties.setProperty("bindDN", ldapConfig.getBindDN());
			properties.setProperty("bindPassword", ldapConfig.getBindPassword());
			properties.setProperty("servers", buildServersString(ldapConfig.getServers()));
			properties.setProperty("useSSL", Boolean.toString(ldapConfig.isUseSSL()));
			LdapConnectionProvider connectionProvider = new LdapConnectionProvider(
					PropertiesDecrypter.decryptProperties(properties, configurationFactory.getCryptoConfigurationSalt()));
			if (connectionProvider.isConnected()) {
				connectionProvider.closeConnectionPool();

				facesMessages.add(FacesMessage.SEVERITY_INFO, "LDAP Connection Test succeeded!");

				return OxTrustConstants.RESULT_SUCCESS;

			}
			if (connectionProvider.getConnectionPool() != null) {
				connectionProvider.closeConnectionPool();
			}
		} catch (Exception ex) {
			log.error("Could not connect to LDAP", ex);
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to connect to LDAP server");

		return OxTrustConstants.RESULT_FAILURE;
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

	public void updateLdapBindPassword(GluuLdapConfiguration ldapConfig) {
		log.info("hello setting passoword" + ldapConfig.getPrimaryKey());
		for (Iterator<GluuLdapConfiguration> iterator = sourceConfigs.iterator(); iterator.hasNext();) {
			GluuLdapConfiguration ldapConfig1 = iterator.next();

		}
	}
	
	public String updateLdapBindPassword(String bindPassword) {
		String encryptedLdapBindPassword = null;
		try {
			encryptedLdapBindPassword = encryptionService.encrypt(bindPassword);
			return encryptedLdapBindPassword;
		} catch (EncryptionException ex) {
			log.error("Failed to encrypt LDAP bind password", ex);
		}

		return null;
	}

	public boolean isExistLdapConfigIdpAuthConf() {
		return existLdapConfigIdpAuthConf;
	}


	@Override
	public void addItemToSimpleProperties(List<SimpleProperty> simpleProperties) {
		if (simpleProperties != null) {
			simpleProperties.add(new SimpleProperty(""));
		}
	}

	@Override
	public void removeItemFromSimpleProperties(List<SimpleProperty> simpleProperties, SimpleProperty simpleProperty) {
		if (simpleProperties != null) {
			simpleProperties.remove(simpleProperty);
		}
	}

	public String getAuthenticationMode() {
		return authenticationMode;
	}

	public void setAuthenticationMode(String authenticationMode) {
		this.authenticationMode = authenticationMode;
	}

	public String getOxTrustAuthenticationMode() {
		return oxTrustAuthenticationMode;
	}

	public void setOxTrustAuthenticationMode(String oxTrustAuthenticationMode) {
		this.oxTrustAuthenticationMode = oxTrustAuthenticationMode;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public LdapOxPassportConfiguration getLdapOxPassportConfiguration() {
		return ldapOxPassportConfiguration;
	}

	public void setLdapOxPassportConfiguration(LdapOxPassportConfiguration ldapOxPassportConfiguration) {
		this.ldapOxPassportConfiguration = ldapOxPassportConfiguration;
	}

	public String getId(Object obj) {
		return "c" + System.identityHashCode(obj) + "Id";
	}

	public void addStrategy() {
		PassportConfiguration passportConfiguration = new PassportConfiguration();
		if (ldapPassportConfigurations == null) {
			ldapPassportConfigurations = new ArrayList<PassportConfiguration>();
		}
		this.ldapPassportConfigurations.add(passportConfiguration);
	}

	public void addField(PassportConfiguration passportConfiguration) {
		String id = getId(passportConfiguration);
		for (PassportConfiguration passportConfig : this.ldapPassportConfigurations) {
			String passportid = getId(passportConfig);
			if (id.equals(passportid)) {
				passportConfig.getFieldset().add(new FieldSet());
			}
		}
	}

	public GluuBoolean getPassportEnable() {
		return passportEnable;
	}

	public void setPassportEnable(GluuBoolean passportEnable) {
		this.passportEnable = passportEnable;
	}

	private List<OxIDPAuthConf> getIDPAuthConfOrNull(GluuAppliance appliance) {
		List<OxIDPAuthConf> idpConfs = appliance.getOxIDPAuthentication();
		List<OxIDPAuthConf> authIdpConfs = new ArrayList<OxIDPAuthConf> ();
		if (idpConfs != null) {
			for (OxIDPAuthConf idpConf : idpConfs) {
				if (idpConf.getType().equalsIgnoreCase("auth")) {
					authIdpConfs.add(idpConf);
				}
			}
		}
		return authIdpConfs;

	}
	
	public List<GluuLdapConfiguration> getSourceConfigs() {
		return sourceConfigs;
	}

	public void setSourceConfigs(List<GluuLdapConfiguration> sourceConfigs) {
		this.sourceConfigs = sourceConfigs;
	}
	
	public void addSourceConfig() {
		addLdapConfig(this.getSourceConfigs());
	}
	
	@Override
	public void addLdapConfig(List<GluuLdapConfiguration> ldapConfigList) {
		GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
		ldapConfiguration.setBindPassword("");
		ldapConfigList.add(ldapConfiguration);
	}
	
	public void removeLdapConfig(List<GluuLdapConfiguration> ldapConfigList, GluuLdapConfiguration removeLdapConfig) {
		for (Iterator<GluuLdapConfiguration> iterator = ldapConfigList.iterator(); iterator.hasNext();) {
			GluuLdapConfiguration ldapConfig = iterator.next();
			if (System.identityHashCode(removeLdapConfig) == System.identityHashCode(ldapConfig)) {
				iterator.remove();
				return;
			}
		}
	}
	
	public GluuLdapConfiguration getActiveLdapConfig() {
		return activeLdapConfig;
	}
	
	public void updateBindPassword() {
		if (this.activeLdapConfig == null) {
			return;
		}

		try {
        	this.activeLdapConfig.setBindPassword(encryptionService.encrypt(this.activeLdapConfig.getBindPassword()));
        } catch (EncryptionException ex) {
            log.error("Failed to encrypt password", ex);
        }
	}

	@Override
	public void setActiveLdapConfig(GluuLdapConfiguration activeLdapConfig) {
		this.activeLdapConfig = activeLdapConfig;
	}

}
