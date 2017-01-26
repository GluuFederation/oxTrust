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
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PassportService;
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
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.model.passport.FieldSet;
import org.xdi.model.passport.PassportConfiguration;
import org.xdi.service.custom.script.AbstractCustomScriptService;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Action class for configuring person authentication
 * 
 * @author Yuriy Movchan Date: 16/11/2010
 */
@Name("managePersonAuthenticationAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class ManagePersonAuthenticationAction
		implements SimplePropertiesListModel, LdapConfigurationModel, Serializable {

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
	private PassportService passportService;

	@In
	private FacesMessages facesMessages;

	private GluuLdapConfiguration ldapConfig;
	private boolean existLdapConfigIdpAuthConf;

	private List<CustomScript> customScripts;

	private String authenticationMode, oxTrustAuthenticationMode;

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

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;

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
			passportEnable = appliance.getPassportEnabled();
			log.info("passport enabled value  : '{0}'", passportEnable);
			this.customScripts = customScriptService.findCustomScripts(
					Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION), "displayName", "oxLevel", "gluuStatus");

			OxIDPAuthConf idpConf = getIDPAuthConfOrNull(appliance);
			if (idpConf != null) {
				this.ldapConfig = mapLdapConfig(idpConf.getConfig());
			}

			this.existLdapConfigIdpAuthConf = this.ldapConfig != null;

			if (this.ldapConfig == null) {
				this.ldapConfig = new GluuLdapConfiguration();
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

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String save() {
		try {
			// Reload entry to include latest changes
			GluuAppliance appliance = applianceService.getAppliance();

			boolean updateAuthenticationMode = false;
			boolean updateOxTrustAuthenticationMode = false;

			OxIDPAuthConf idpConf = getIDPAuthConfOrNull(appliance);
			if (idpConf != null && idpConf.getName() != null) {
				if (idpConf.getName().equals(this.authenticationMode)) {
					updateAuthenticationMode = true;
				}
				if (idpConf.getName().equals(this.oxTrustAuthenticationMode)) {
					updateOxTrustAuthenticationMode = true;
				}
			}

			this.ldapConfig.updateStringsLists();

			updateAuthConf(appliance);

			String updatedAuthMode = updateAuthenticationMode ? this.ldapConfig.getConfigId() : this.authenticationMode;
			String updatedOxTrustAuthMode = updateOxTrustAuthenticationMode ? this.ldapConfig.getConfigId()
					: this.oxTrustAuthenticationMode;
			appliance.setAuthenticationMode(updatedAuthMode);
			appliance.setOxTrustAuthenticationMode(updatedOxTrustAuthMode);

			appliance.setPassportEnabled(passportEnable);

			applianceService.updateAppliance(appliance);

			ldapOxPassportConfiguration.setPassportConfigurations(ldapPassportConfigurations);

			passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
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
	}

	private GluuLdapConfiguration mapLdapConfig(String config)
			throws JsonParseException, JsonMappingException, IOException {
		return (GluuLdapConfiguration) jsonToObject(config, GluuLdapConfiguration.class);
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() throws Exception {
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

			if (ldapConfig != null) {
				this.customAuthenticationConfigNames.add(ldapConfig.getConfigId());
			} else {
				this.customAuthenticationConfigNames.add(OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME);
			}
		}

		return this.customAuthenticationConfigNames;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String testLdapConnection() {

		try {
			FileConfiguration configuration = new FileConfiguration(OxTrustConfiguration.LDAP_PROPERTIES_FILE);
			if (!configuration.isLoaded()) {
				configuration = new FileConfiguration(OxTrustConfiguration.LDAP_DEFAULT_PROPERTIES_FILE);
			}
			Properties properties = configuration.getProperties();
			properties.setProperty("bindDN", this.ldapConfig.getBindDN());
			properties.setProperty("bindPassword", this.ldapConfig.getBindPassword());
			properties.setProperty("servers", buildServersString(this.ldapConfig.getServers()));
			properties.setProperty("useSSL", Boolean.toString(this.ldapConfig.isUseSSL()));
			LDAPConnectionProvider connectionProvider = new LDAPConnectionProvider(
					PropertiesDecrypter.decryptProperties(properties, cryptoConfigurationSalt));
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
			encryptedLdapBindPassword = StringEncrypter.defaultInstance().encrypt(this.ldapConfig.getBindPassword(),
					cryptoConfigurationSalt);
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
		this.ldapConfig.setEnabled(true);
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

	private OxIDPAuthConf getIDPAuthConfOrNull(GluuAppliance appliance) {
		List<OxIDPAuthConf> idpConfs = appliance.getOxIDPAuthentication();
		if (idpConfs != null) {
			for (OxIDPAuthConf idpConf : idpConfs) {
				if (idpConf.getType().equalsIgnoreCase("auth")) {
					return idpConf;
				}
			}
		}

		return null;
	}
}
