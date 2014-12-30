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
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
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
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.service.custom.script.AbstractCustomScriptService;
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
public class ManageCustomAuthenticationAction implements SimplePropertiesListModel, LdapConfigurationModel, Serializable {

	private static final long serialVersionUID = -4470460481895022468L;

	@Logger
	private Log log;

	@In
	private StatusMessages statusMessages;

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

	private List<CustomScript> customScripts;

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

			this.customScripts = customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.CUSTOM_AUTHENTICATION), "name", "oxLevel");

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

	public List<String> getCustomAuthenticationConfigurationNames() {
		if (this.customAuthenticationConfigNames == null) {
			this.customAuthenticationConfigNames = new ArrayList<String>();
			for (CustomScript customScript : this.customScripts) {
				String name = customScript.getName();
				if (StringHelper.isEmpty(name)) {
					continue;
				}

				this.customAuthenticationConfigNames.add(customScript.getName());
			}
		}

		return this.customAuthenticationConfigNames;
	}

	public List<String> getCustomAuthenticationConfigurationLevels() {
		if (this.customAuthenticationConfigLevels == null) {
			this.customAuthenticationConfigLevels = new ArrayList<String>();
			for (CustomScript customScript : this.customScripts) {
				String level = Integer.toString(customScript.getLevel());

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
