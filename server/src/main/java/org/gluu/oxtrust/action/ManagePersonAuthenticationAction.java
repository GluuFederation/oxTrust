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
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethod;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethodService;
import org.gluu.oxtrust.service.config.authentication.PassportAuthenticationMethod;
import org.gluu.oxtrust.service.config.ldap.ConnectionStatus;
import org.gluu.oxtrust.service.config.ldap.LdapConfigurationService;
import org.gluu.oxtrust.service.config.ldap.LdapConnectionData;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.gluu.persist.model.base.GluuBoolean;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.ldap.GluuLdapConfiguration;
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
		implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, LdapConfigurationModel, Serializable {

	private static final String CLIENT_SECRET = "clientSecret";

	private static final String CLIENT_ID = "clientID";

	private static final long serialVersionUID = -4470460481895022468L;

	private static final String DEFAULT_AUTHENTICATION_MODE = "auth_ldap_server";
	private static final String DEFAULT_OX_TRUST_AUTHENTICATION_MODE = null;

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

	@Inject
	transient private LdapConfigurationService ldapConfigurationService;

	@Inject
	transient private AuthenticationMethodService authenticationMethodService;

	@Inject
	transient private ConnectionStatus connectionStatus;

	private boolean existLdapConfigIdpAuthConf;

	private List<CustomScript> customScripts;

	private List<GluuLdapConfiguration> sourceConfigs;

	private GluuLdapConfiguration activeLdapConfig;

    private AuthenticationMethod authenticationMethod;

	private List<String> customAuthenticationConfigNames;

	private boolean initialized;

	private List<PassportConfiguration> ldapPassportConfigurations;

	public List<PassportConfiguration> getLdapPassportConfigurations() {
		for (PassportConfiguration configuration : ldapPassportConfigurations) {
			if (configuration.getFieldset() == null) {
				configuration.setFieldset(new ArrayList<SimpleExtendedCustomProperty>());
			}
		}
		return ldapPassportConfigurations;
	}

	public void setLdapPassportConfigurations(List<PassportConfiguration> ldapPassportConfigurations) {
		this.ldapPassportConfigurations = ldapPassportConfigurations;
	}

	public String modify() {
		String outcome = modifyImpl();

		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msg['configuration.manageAuthentication.failToPrepareUpdate']}"));
			conversationService.endConversation();
		}

		return outcome;
	}

	public String modifyImpl() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.customScripts = customScriptService.findCustomScripts(
					Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION), "displayName", "oxLevel", "gluuStatus");

			this.sourceConfigs = new ArrayList<GluuLdapConfiguration>(ldapConfigurationService.findLdapConfigurations());

            authenticationMethod = authenticationMethodService.findAuthenticationMode();

			this.ldapPassportConfigurations = authenticationMethod.getPassportAuthenticationMethod()
					.getLdapOxPassportConfiguration().getPassportConfigurations();

			if (ldapPassportConfigurations == null) {
				ldapPassportConfigurations = new ArrayList<PassportConfiguration>();
			}
		} catch (Exception ex) {
			log.error("Failed to load appliance configuration", ex);

			if (authenticationMethod == null) {
				authenticationMethod = new AuthenticationMethod(DEFAULT_AUTHENTICATION_MODE,
						DEFAULT_OX_TRUST_AUTHENTICATION_MODE, PassportAuthenticationMethod.disabled());
			}

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
				if (authenticationMethod.hasAuthenticationMode(oldAuthName)) {
					updateAuthenticationMode = true;
				}
				if (authenticationMethod.hasOxTrustAuthenticationMode(oldAuthName)) {
					updateOxTrustAuthenticationMode = true;
				}
			}

			updateAuthConf();
			appliance = applianceService.getAppliance();

			String newAuthName = getFirstConfigName(appliance.getOxIDPAuthentication());
			String updatedAuthMode = updateAuthenticationMode ? newAuthName : this.authenticationMethod.getAuthenticationMode();
			String updatedOxTrustAuthMode = updateOxTrustAuthenticationMode ? newAuthName
					: this.authenticationMethod.getOxTrustAuthenticationMode();

			authenticationMethod.getPassportAuthenticationMethod().getLdapOxPassportConfiguration().setPassportConfigurations(ldapPassportConfigurations);
			authenticationMethodService.save(new AuthenticationMethod(updatedAuthMode, updatedOxTrustAuthMode,
					authenticationMethod.getPassportAuthenticationMethod()));
		} catch (BasePersistenceException ex) {
			log.error("Failed to update appliance configuration", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update appliance");
			return OxTrustConstants.RESULT_FAILURE;
		}

		reset();

		facesMessages.add(FacesMessage.SEVERITY_INFO, facesMessages.evalResourceAsString("#{msg['configuration.manageAuthentication.updateSucceed']}"));
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

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, facesMessages.evalResourceAsString("#{msg['configuration.manageAuthentication.updateFailed']}"));
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String objectToJson(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);
	}

	public boolean updateAuthConf() {
		try {
			ldapConfigurationService.save(sourceConfigs);
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
            if (connectionStatus.isUp(LdapConnectionData.from(ldapConfig))) {
				facesMessages.add(FacesMessage.SEVERITY_INFO, "LDAP Connection Test succeeded!");
				return OxTrustConstants.RESULT_SUCCESS;
			}
		} catch (Exception ex) {
			log.error("Could not connect to LDAP", ex);
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, facesMessages.evalResourceAsString("#{msg['configuration.manageAuthentication.ldap.testFailed']}"));

		return OxTrustConstants.RESULT_FAILURE;
	}

	@Deprecated
	public void updateLdapBindPassword(GluuLdapConfiguration ldapConfig) {
		// This method does nothing.
		// Should be removed.
		// Is this used somewhere in the UI ?
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
		return this.authenticationMethod.getAuthenticationMode();
	}

	public void setAuthenticationMode(String authenticationMode) {
		this.authenticationMethod.setAuthenticationMode(authenticationMode);
    }

	public String getOxTrustAuthenticationMode() {
		return this.authenticationMethod.getOxTrustAuthenticationMode();
	}

	public void setOxTrustAuthenticationMode(String oxTrustAuthenticationMode) {
        this.authenticationMethod.setOxTrustAuthenticationMode(oxTrustAuthenticationMode);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public LdapOxPassportConfiguration getLdapOxPassportConfiguration() {
		return authenticationMethod.getPassportAuthenticationMethod().getLdapOxPassportConfiguration();
	}

	public void setLdapOxPassportConfiguration(LdapOxPassportConfiguration ldapOxPassportConfiguration) {
		this.authenticationMethod.getPassportAuthenticationMethod().setLdapOxPassportConfiguration(ldapOxPassportConfiguration);
	}

	public String getId(Object obj) {
		return "c" + System.identityHashCode(obj) + "Id";
	}

	public void addStrategy() {
		PassportConfiguration passportConfiguration = new PassportConfiguration();
		if (ldapPassportConfigurations == null) {
			ldapPassportConfigurations = new ArrayList<PassportConfiguration>();
		}
		SimpleExtendedCustomProperty clientIDField = new SimpleExtendedCustomProperty();
		clientIDField.setValue1(CLIENT_ID);
		clientIDField.setValue2(facesMessages
				.evalResourceAsString("#{msg['manageAuthentication.passport.strategy.clientIDFieldHint']}"));
		SimpleExtendedCustomProperty clientSecretField = new SimpleExtendedCustomProperty();
		clientSecretField.setValue1(CLIENT_SECRET);
		clientSecretField.setValue2(facesMessages
				.evalResourceAsString("#{msg['manageAuthentication.passport.strategy.clientSecretFieldHint']}"));
		passportConfiguration.setFieldset(new ArrayList<SimpleExtendedCustomProperty>());
		passportConfiguration.getFieldset().add(clientIDField);
		passportConfiguration.getFieldset().add(clientSecretField);
		this.ldapPassportConfigurations.add(passportConfiguration);
	}

	public void addField(PassportConfiguration removePassportConfiguration) {
		for (PassportConfiguration passportConfig : this.ldapPassportConfigurations) {
			if (System.identityHashCode(removePassportConfiguration) == System.identityHashCode(passportConfig)) {
				if (passportConfig.getFieldset() == null) {
					passportConfig.setFieldset(new ArrayList<SimpleExtendedCustomProperty>());
				}
				passportConfig.getFieldset().add(new SimpleExtendedCustomProperty());
			}
		}
	}

	public GluuBoolean getPassportEnable() {
		return authenticationMethod.getPassportAuthenticationMethod().asGluuBoolean();
	}

	public void setPassportEnable(GluuBoolean passportEnable) {
		authenticationMethodService.change(authenticationMethod, passportEnable);
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

	@Override
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

	@Override
	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties) {
		simpleCustomProperties.add(new SimpleExtendedCustomProperty("", ""));
	}

	@Override
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.remove(simpleCustomProperty);
		}
	}

	public void removeStrategy(PassportConfiguration removePassportConfiguration) {
		for (Iterator<PassportConfiguration> iterator = this.ldapPassportConfigurations.iterator(); iterator
				.hasNext();) {
			PassportConfiguration passportConfiguration = iterator.next();
			if (System.identityHashCode(removePassportConfiguration) == System
					.identityHashCode(passportConfiguration)) {
				iterator.remove();
				return;
			}
		}
	}

}
