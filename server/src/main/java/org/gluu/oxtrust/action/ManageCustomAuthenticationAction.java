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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.model.scim.ScimCustomAttributes;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleProperty;
import org.xdi.model.config.CustomAuthenticationConfiguration;
import org.xdi.model.cusom.script.conf.CustomScript;
import org.xdi.model.cusom.script.type.CustomScriptType;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.service.custom.script.AbstractCustomScriptService;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Action class for configuring custom configuration
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 */
@Name("manageCustomAuthenticationAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class ManageCustomAuthenticationAction implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, LdapConfigurationModel, Serializable {

	private static final long serialVersionUID = -4470460481895022468L;

//	private static final String CUSTOM_AUTHENTICATION_SCRIPT_PROPERTY_NAME = "script.__$__customAuthenticationScript__$__";
//	private static final String CUSTOM_AUTHENTICATION_PROPERTY_PREFIX = "property.";
//	private static final String CUSTOM_AUTHENTICATION_SCRIPT_USAGE_TYPE = "usage.";

	@Logger
	private Log log;

	@In
	private StatusMessages statusMessages;

	@In
	private GluuCustomPerson currentPerson;

	@In
	private ImageService imageService;

	@In
	private OrganizationService organizationService;

	@In
	private ApplianceService applianceService;

	@In(value = "customScriptService")
	private AbstractCustomScriptService customScriptService;

	@In
	private FacesMessages facesMessages;

	private GluuLdapConfiguration ldapConfig;
	private boolean existLdapConfigIdpAuthConf;

	private List<CustomAuthenticationConfiguration> customAuthenticationConfigurations;

	private String authenticationLevel, authenticationMode;

	private List<String> customAuthenticationConfigNames;
	private List<String> customAuthenticationConfigLevels;

	private boolean initialized;
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String modify() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			GluuAppliance appliance = applianceService.getAppliance();

			if (appliance == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.customAuthenticationConfigurations = new ArrayList<CustomAuthenticationConfiguration>();
			List<CustomScript> customScripts = customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.CUSTOM_AUTHENTICATION), null);
			for (CustomScript customScript : customScripts) {
				CustomAuthenticationConfiguration customAuthenticationConfig = toCustomAuthenticationConfiguration(customScript);
				this.customAuthenticationConfigurations.add(customAuthenticationConfig);
			}

			List<OxIDPAuthConf> idpConfs = appliance.getOxIDPAuthentication();
			if (idpConfs != null) {
				for (OxIDPAuthConf idpConf : idpConfs) {
					if (idpConf.getType().equalsIgnoreCase("auth")) {
						this.ldapConfig = mapLdapConfig(idpConf.getConfig());
						break;
					}
				}
			}

			this.existLdapConfigIdpAuthConf = this.ldapConfig != null;

			if (this.ldapConfig == null) {
				this.ldapConfig = new GluuLdapConfiguration();
			}
			
			this.authenticationLevel = appliance.getAuthenticationLevel();
			this.authenticationMode = appliance.getAuthenticationMode();
		} catch (Exception ex) {
			log.error("Failed to load appliance configuration", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	// TODO: Don't use this conversion in next version
	private CustomAuthenticationConfiguration toCustomAuthenticationConfiguration(CustomScript customScript) {
		CustomAuthenticationConfiguration result = new CustomAuthenticationConfiguration();

		result.setDn(customScript.getDn());
		result.setInum(customScript.getInum());

		result.setName(customScript.getName());
		result.setLevel(customScript.getLevel());
		result.setEnabled(customScript.isEnabled());
		result.setVersion(customScript.getRevision());
		result.setCustomAuthenticationScript(customScript.getScript());
		result.setCustomAuthenticationAttributes(customScript.getConfigurationProperties());

		AuthenticationScriptUsageType tmpUsageType = null;
		for (SimpleCustomProperty moduleProperty : customScript.getModuleProperties()) {
			if (StringHelper.equalsIgnoreCase(moduleProperty.getValue1(), "usage_type")) {
				tmpUsageType = AuthenticationScriptUsageType.getByValue(moduleProperty.getValue2());
				break;
			}
		}
		result.setUsageType(tmpUsageType);

		return result;
	}

	// TODO: Don't use this conversion in next version
	private CustomScript toCustomScript(CustomAuthenticationConfiguration customAuthenticationConfiguration) {
		CustomScript result = new CustomScript();

		result.setProgrammingLanguage(ProgrammingLanguage.PYTHON);
		result.setScriptType(CustomScriptType.CUSTOM_AUTHENTICATION);
		
		result.setName(customAuthenticationConfiguration.getName());
		result.setLevel(customAuthenticationConfiguration.getLevel());
		result.setEnabled(customAuthenticationConfiguration.isEnabled());
		result.setRevision(customAuthenticationConfiguration.getVersion());
		result.setScript(customAuthenticationConfiguration.getCustomAuthenticationScript());

		List<SimpleCustomProperty> moduleProperties = Arrays.asList(new SimpleCustomProperty("usage_type", customAuthenticationConfiguration.getUsageType().toString()));
		result.setModuleProperties(moduleProperties);

		List<SimpleCustomProperty> configurationProperties = customAuthenticationConfiguration.getCustomAuthenticationAttributes();
		if (configurationProperties.size() > 0) {
			result.setConfigurationProperties(configurationProperties);
		} else {
			result.setConfigurationProperties(null);
		}

		return result;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String save() {
		try {
			// Reload entry to include latest changes
			GluuAppliance appliance = applianceService.getAppliance();

			updateAuthConf(appliance);
			
			appliance.setAuthenticationLevel(this.authenticationLevel);
			appliance.setAuthenticationMode(this.authenticationMode);
			
			applianceService.updateAppliance(appliance);
		} catch (LdapMappingException ex) {
			log.error("Failed to update appliance configuration", ex);
			facesMessages.add(Severity.ERROR, "Failed to update appliance");
			return OxTrustConstants.RESULT_FAILURE;
		}

		reset();

		return modify();
	}

	private void reset() {
		this.customAuthenticationConfigNames = null;
		this.customAuthenticationConfigLevels = null;
	}


	private GluuLdapConfiguration mapLdapConfig(String config) throws JsonParseException, JsonMappingException, IOException {
		return (GluuLdapConfiguration) jsonToObject(config, GluuLdapConfiguration.class);
	}

//	private CustomAuthenticationConfiguration mapCustomAuthentication(OxIDPAuthConf oneConf) {
//		CustomAuthenticationConfiguration customAuthenticationConfig = new CustomAuthenticationConfiguration();
//		customAuthenticationConfig.setName(oneConf.getName());
//		customAuthenticationConfig.setLevel(oneConf.getLevel());
//		customAuthenticationConfig.setPriority(oneConf.getPriority());
//		customAuthenticationConfig.setEnabled(oneConf.getEnabled());
//		customAuthenticationConfig.setVersion(oneConf.getVersion());
//
//		for (ScimCustomAttributes scimCustomAttributes : oneConf.getFields()) {
//			if ((scimCustomAttributes.getValues() == null) || (scimCustomAttributes.getValues().size() == 0)) {
//				continue;
//			}
//
//			String attrName = StringHelper.toLowerCase(scimCustomAttributes.getName());
//
//			if (StringHelper.isEmpty(attrName)) {
//				continue;
//			}
//
//			String value = scimCustomAttributes.getValues().get(0);
//
//			if (attrName.startsWith(CUSTOM_AUTHENTICATION_PROPERTY_PREFIX)) {
//				String key = scimCustomAttributes.getName().substring(CUSTOM_AUTHENTICATION_PROPERTY_PREFIX.length());
//				SimpleCustomProperty property = new SimpleCustomProperty(key, value);
//				customAuthenticationConfig.getCustomAuthenticationAttributes().add(property);
//			} else if (StringHelper.equalsIgnoreCase(attrName, CUSTOM_AUTHENTICATION_SCRIPT_PROPERTY_NAME)) {
//				customAuthenticationConfig.setCustomAuthenticationScript(value);
//			} else if (StringHelper.equalsIgnoreCase(attrName, CUSTOM_AUTHENTICATION_SCRIPT_USAGE_TYPE)) {
//				if (StringHelper.isNotEmpty(value)) {
//					AuthenticationScriptUsageType authenticationScriptUsageType =  AuthenticationScriptUsageType.getByValue(value);
//					customAuthenticationConfig.setUsageType(authenticationScriptUsageType);
//				}
//			}
//		}
//
//		return customAuthenticationConfig;
//	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() throws Exception {
	}

	private Object jsonToObject(String json, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException {
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
			List<OxIDPAuthConf> idpConf = new ArrayList<OxIDPAuthConf>();
			
			List<CustomScript> oldCustomScripts = customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.CUSTOM_AUTHENTICATION), "dn", "inum");

			List<String> updatedInums = new ArrayList<String>();
			for (CustomAuthenticationConfiguration customAuthenticationConfig : this.customAuthenticationConfigurations) {
				customAuthenticationConfig.setVersion(customAuthenticationConfig.getVersion() + 1);
//				OxIDPAuthConf oxIDPAuthConf = customAuthenticationToIdp(customAuthenticationConfig);
//				oxIDPAuthConf.setVersion(oxIDPAuthConf.getVersion());
				
				CustomScript customScript = toCustomScript(customAuthenticationConfig);

				boolean update = true;
				String dn = customAuthenticationConfig.getDn();
				String customScriptId = customAuthenticationConfig.getInum();
				if (StringHelper.isEmpty(dn)) {
					String basedInum = OrganizationService.instance().getOrganizationInum();
					customScriptId = basedInum + "!" + INumGenerator.generate(2);
					dn = customScriptService.buildDn(customScriptId);

					customAuthenticationConfig.setDn(dn);
					customAuthenticationConfig.setInum(customScriptId);
					update = false;
				};


				customScript.setDn(dn);
				customScript.setInum(customScriptId);
				
				updatedInums.add(customScriptId);

				if (update) {
					customScriptService.update(customScript);
				} else {
					customScriptService.add(customScript);
				}
//				idpConf.add(oxIDPAuthConf);
			}
			
			// Remove removed scripts
			for (CustomScript oldCustomScript : oldCustomScripts) {
				if (!updatedInums.contains(oldCustomScript.getInum())) {
					customScriptService.remove(oldCustomScript);
				}
			}

			if (this.existLdapConfigIdpAuthConf) {
				if (this.ldapConfig.isUseAnonymousBind()) {
					this.ldapConfig.setBindDN(null);
				}

				OxIDPAuthConf ldapConfigIdpAuthConf = new OxIDPAuthConf();
				ldapConfigIdpAuthConf.setType("auth");
				ldapConfigIdpAuthConf.setVersion(ldapConfigIdpAuthConf.getVersion() + 1);
				ldapConfigIdpAuthConf.setName(this.ldapConfig.getConfigId());
				ldapConfigIdpAuthConf.setEnabled(this.ldapConfig.isEnabled());
				ldapConfigIdpAuthConf.setConfig(objectToJson(this.ldapConfig));
	
				idpConf.add(ldapConfigIdpAuthConf);
			}

			appliance.setOxIDPAuthentication(idpConf);
		} catch (Exception ex) {
			log.error("An Error occured ", ex);

			return false;
		}
		
		return true;
	}

//	private OxIDPAuthConf customAuthenticationToIdp(CustomAuthenticationConfiguration customAuthenticationConfig) {
//		OxIDPAuthConf oxIDP = new OxIDPAuthConf();
//		oxIDP.setEnabled(customAuthenticationConfig.isEnabled());
//
//		oxIDP.setType("customAuthentication");
//		oxIDP.setName(customAuthenticationConfig.getName());
//		oxIDP.setLevel(customAuthenticationConfig.getLevel());
//		oxIDP.setPriority(customAuthenticationConfig.getPriority());
//		oxIDP.setVersion(customAuthenticationConfig.getVersion());
//
//		List<ScimCustomAttributes> properties = new ArrayList<ScimCustomAttributes>();
//
//		ScimCustomAttributes usageProperty = new ScimCustomAttributes();
//		usageProperty.setName(CUSTOM_AUTHENTICATION_SCRIPT_USAGE_TYPE);
//		usageProperty.getValues().add(customAuthenticationConfig.getUsageType().getValue());
//
//		properties.add(usageProperty);
//
//		for (SimpleCustomProperty customProperty : customAuthenticationConfig.getCustomAuthenticationAttributes()) {
//			ScimCustomAttributes property = new ScimCustomAttributes();
//
//			property.setName(CUSTOM_AUTHENTICATION_PROPERTY_PREFIX + customProperty.getValue1());
//			property.getValues().add(customProperty.getValue2());
//
//			properties.add(property);
//		}
//
//		ScimCustomAttributes property = new ScimCustomAttributes();
//		property.setName(CUSTOM_AUTHENTICATION_SCRIPT_PROPERTY_NAME);
//		property.getValues().add(customAuthenticationConfig.getCustomAuthenticationScript());
//
//		properties.add(property);
//
//		oxIDP.setFields(properties);
//
//		return oxIDP;
//	}

	public List<CustomAuthenticationConfiguration> getCustomAuthenticationConfigurations() {
		return customAuthenticationConfigurations;
	}

	public void addCustomAuthenticationConfiguration() {
		CustomAuthenticationConfiguration customAuthenticationConfiguration = new CustomAuthenticationConfiguration();
		customAuthenticationConfiguration.setUsageType(AuthenticationScriptUsageType.INTERACTIVE);

		this.customAuthenticationConfigurations.add(new CustomAuthenticationConfiguration());
	}

	public void removeCustomAuthenticationConfiguration(CustomAuthenticationConfiguration removeCustomAuthenticationConfiguration) {
		for (Iterator<CustomAuthenticationConfiguration> iterator = this.customAuthenticationConfigurations.iterator(); iterator.hasNext();) {
			CustomAuthenticationConfiguration customAuthenticationConfig = iterator.next();
			if (System.identityHashCode(removeCustomAuthenticationConfiguration) == System.identityHashCode(customAuthenticationConfig)) {
				iterator.remove();
				return;
			}
		}
	}

	public List<String> getCustomAuthenticationConfigurationNames() {
		if (this.customAuthenticationConfigNames == null) {
			this.customAuthenticationConfigNames = new ArrayList<String>();
			for (CustomAuthenticationConfiguration customAuthenticationConfig : this.customAuthenticationConfigurations) {
				String name = customAuthenticationConfig.getName();
				if (StringHelper.isEmpty(name)) {
					continue;
				}

				this.customAuthenticationConfigNames.add(customAuthenticationConfig.getName());
			}
		}

		return this.customAuthenticationConfigNames;
	}

	public List<String> getCustomAuthenticationConfigurationLevels() {
		if (this.customAuthenticationConfigLevels == null) {
			this.customAuthenticationConfigLevels = new ArrayList<String>();
			for (CustomAuthenticationConfiguration customAuthenticationConfig : this.customAuthenticationConfigurations) {
				String level = Integer.toString(customAuthenticationConfig.getLevel());

				if (!this.customAuthenticationConfigLevels.contains(level)) {
					this.customAuthenticationConfigLevels.add(level);
				}
			}
		}

		return this.customAuthenticationConfigLevels;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String testLdapConnection() {

		try {
			FileConfiguration configuration = new FileConfiguration(OxTrustConstants.CONFIGURATION_FILE_LOCAL_LDAP_PROPERTIES_FILE);
			Properties properties = configuration.getProperties();
			properties.setProperty("bindDN", this.ldapConfig.getBindDN());
			properties.setProperty("bindPassword", this.ldapConfig.getBindPassword());
			properties.setProperty("servers", buildServersString(this.ldapConfig.getServers()));
			properties.setProperty("useSSL", Boolean.toString(this.ldapConfig.isUseSSL()));
			LDAPConnectionProvider connectionProvider = new LDAPConnectionProvider(PropertiesDecrypter.decryptProperties(properties, cryptoConfiguration.getEncodeSalt()));
			if (connectionProvider.isConnected()) {
				connectionProvider.closeConnectionPool();
				return OxTrustConstants.RESULT_SUCCESS;

			}
			connectionProvider.closeConnectionPool();
			return OxTrustConstants.RESULT_FAILURE;

		} catch (Exception ex) {
			log.error("Could not connect to LDAP", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
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

	public void updateLdapBindPassword() {
		String encryptedLdapBindPassword = null;
		try {
			encryptedLdapBindPassword = StringEncrypter.defaultInstance().encrypt(this.ldapConfig.getBindPassword(), cryptoConfiguration.getEncodeSalt());
		} catch (EncryptionException ex) {
			log.error("Failed to encrypt LDAP bind password", ex);
		}

		this.ldapConfig.setBindPassword(encryptedLdapBindPassword);
	}

	public boolean isExistLdapConfigIdpAuthConf() {
		return existLdapConfigIdpAuthConf;
	}

	public void setExistLdapConfigIdpAuthConf(boolean existLdapConfigIdpAuthConf) {
		this.existLdapConfigIdpAuthConf = existLdapConfigIdpAuthConf;
	}

	public GluuLdapConfiguration getLdapConfig() {
		return ldapConfig;
	}

	@Override
	public void setActiveLdapConfig(GluuLdapConfiguration activeLdapConfig) {
	}

	@Override
	public void addLdapConfig(List<GluuLdapConfiguration> ldapConfigList) {
	}

	@Override
	public void removeLdapConfig(List<GluuLdapConfiguration> ldapConfigList, GluuLdapConfiguration removeLdapConfig) {
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

	@Override
	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.add(new SimpleCustomProperty("", ""));
		}
	}

	@Override
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.remove(simpleCustomProperty);
		}
	}

	public String getAuthenticationLevel() {
		return authenticationLevel;
	}

	public void setAuthenticationLevel(String authenticationLevel) {
		this.authenticationLevel = authenticationLevel;
	}

	public String getAuthenticationMode() {
		return authenticationMode;
	}

	public void setAuthenticationMode(String authenticationMode) {
		this.authenticationMode = authenticationMode;
	}

	public boolean isInitialized() {
		return initialized;
	}

}
