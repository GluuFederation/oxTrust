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

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.SimpleExtendedCustomProperty;
import org.gluu.model.SimpleProperty;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.model.ldap.GluuLdapConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.LdapConfigurationModel;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.OxConstants;
import org.gluu.util.StringHelper;
import org.gluu.util.security.PropertiesDecrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

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
	private static final String SIMPLE_PASSWORD_AUTH = "simple_password_auth";

	private static final long serialVersionUID = -4470460481895022468L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private AbstractCustomScriptService customScriptService;

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

	private String recaptchaSiteKey;
	private String recaptchaSecretKey;

	private List<String> customAuthenticationConfigNames;

	private boolean initialized;

	private Boolean passportEnable = Boolean.FALSE;
	private boolean authenticationRecaptchaEnabled = false;

	public boolean isAuthenticationRecaptchaEnabled() {
		return authenticationRecaptchaEnabled;
	}

	public void setAuthenticationRecaptchaEnabled(boolean authenticationRecaptchaEnabled) {
		this.authenticationRecaptchaEnabled = authenticationRecaptchaEnabled;
	}

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private AppConfiguration oxTrustappConfiguration;

	public String modify() {
		String outcome = modifyImpl();

		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, facesMessages
					.evalResourceAsString("#{msgs['configuration.manageAuthentication.failToPrepareUpdate']}"));
			conversationService.endConversation();
		}

		return outcome;
	}

	public String modifyImpl() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			GluuConfiguration configuration = configurationService.getConfiguration();
			if (configuration == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}
			this.passportEnable = configuration.isPassportEnabled();
			this.customScripts = customScriptService.findCustomScripts(
					Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION), "displayName", "oxLevel", "oxEnabled");
			List<OxIDPAuthConf> list = getIDPAuthConfOrNull(configuration);
			this.sourceConfigs = new ArrayList<GluuLdapConfiguration>();
			if (list != null) {
				for (OxIDPAuthConf oxIDPAuthConf : list) {
					GluuLdapConfiguration oxldapConfig = oxIDPAuthConf.getConfig();
					this.sourceConfigs.add(oxldapConfig);
				}
			}
			getAuthenticationRecaptcha();
			this.authenticationMode = configuration.getAuthenticationMode();
			this.oxTrustAuthenticationMode = configuration.getOxTrustAuthenticationMode();
		} catch (Exception ex) {
			log.error("Failed to load configuration configuration", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws IOException {
		try {
			// Reload entry to include latest changes
			GluuConfiguration configuration = configurationService.getConfiguration();
			boolean updateAuthenticationMode = false;
			boolean updateOxTrustAuthenticationMode = false;

			String oldAuthName = getFirstConfigName(configuration.getOxIDPAuthentication());
			if (oldAuthName != null) {
				if (oldAuthName.equals(this.authenticationMode)) {
					updateAuthenticationMode = true;
				}
				if (oldAuthName.equals(this.oxTrustAuthenticationMode)) {
					updateOxTrustAuthenticationMode = true;
				}
			}

			updateAuthConf(configuration);
			String newAuthName = getFirstConfigName(configuration.getOxIDPAuthentication());
			String updatedAuthMode = updateAuthenticationMode ? newAuthName : this.authenticationMode;
			String updatedOxTrustAuthMode = updateOxTrustAuthenticationMode ? newAuthName
					: this.oxTrustAuthenticationMode;
			configuration.setAuthenticationMode(updatedAuthMode);
			configuration.setOxTrustAuthenticationMode(updatedOxTrustAuthMode);
			setAuthenticationRecaptcha();
			configuration.setPassportEnabled(passportEnable);
			configurationService.updateConfiguration(configuration);
		} catch (BasePersistenceException ex) {
			log.error("Failed to update configuration configuration", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update configuration");
			return OxTrustConstants.RESULT_FAILURE;
		}

		reset();
		facesMessages.add(FacesMessage.SEVERITY_INFO,
				facesMessages.evalResourceAsString("#{msgs['configuration.manageAuthentication.updateSucceed']}"));
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
		facesMessages.add(FacesMessage.SEVERITY_INFO,
				facesMessages.evalResourceAsString("#{msgs['configuration.manageAuthentication.updateFailed']}"));
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public boolean updateAuthConf(GluuConfiguration configuration) {
		try {
			List<OxIDPAuthConf> idpConf = new ArrayList<OxIDPAuthConf>();
			for (GluuLdapConfiguration ldapConfig : this.sourceConfigs) {
				if (ldapConfig.isUseAnonymousBind()) {
					ldapConfig.setBindDN(null);
				}
				OxIDPAuthConf ldapConfigIdpAuthConf = new OxIDPAuthConf();
				ldapConfig.updateStringsLists();
				ldapConfigIdpAuthConf.setType("auth");
				ldapConfigIdpAuthConf.setVersion(ldapConfigIdpAuthConf.getVersion() + 1);
				ldapConfigIdpAuthConf.setName(ldapConfig.getConfigId());
				ldapConfigIdpAuthConf.setEnabled(ldapConfig.isEnabled());
				ldapConfigIdpAuthConf.setConfig(ldapConfig);
				idpConf.add(ldapConfigIdpAuthConf);
			}
			configuration.setOxIDPAuthentication(idpConf);
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
				if ((ldapConfig != null) && StringHelper.isNotEmpty(ldapConfig.getConfigId())
						&& ldapConfig.isEnabled()) {
					this.customAuthenticationConfigNames.add(ldapConfig.getConfigId());
					internalServerName = false;
				}
			}

			if (internalServerName) {
				this.customAuthenticationConfigNames.add(OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME);
			}
			if (shouldEnableSimplePasswordAuth()
					&& !this.customAuthenticationConfigNames.contains(SIMPLE_PASSWORD_AUTH)) {
				this.customAuthenticationConfigNames.add(SIMPLE_PASSWORD_AUTH);
			}
		}
		return this.customAuthenticationConfigNames;
	}

	public String testLdapConnection(GluuLdapConfiguration ldapConfig) {
		try {
			Properties properties = new Properties();
			properties.setProperty("bindDN", ldapConfig.getBindDN());
			properties.setProperty("bindPassword", ldapConfig.getBindPassword());
			properties.setProperty("servers", buildServersString(ldapConfig.getServers()));
			properties.setProperty("useSSL", Boolean.toString(ldapConfig.isUseSSL()));
			Properties ldapDecryptedProperties = PropertiesDecrypter
					.decryptProperties(properties, configurationFactory.getCryptoConfigurationSalt());
			log.trace("Attempting to create LDAP connection with properties: {}", ldapDecryptedProperties);

			LdapConnectionProvider connectionProvider = new LdapConnectionProvider(ldapDecryptedProperties);
			connectionProvider.create();
			if (connectionProvider.isConnected()) {
				connectionProvider.closeConnectionPool();

				facesMessages.add(FacesMessage.SEVERITY_INFO, facesMessages
						.evalResourceAsString("#{msgs['configuration.manageAuthentication.ldap.testSucceed']}"));

				return OxTrustConstants.RESULT_SUCCESS;

			}
			if (connectionProvider.getConnectionPool() != null) {
				connectionProvider.closeConnectionPool();
			}
		} catch (Exception ex) {
			log.error("Could not connect to LDAP", ex);
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR,
				facesMessages.evalResourceAsString("#{msgs['configuration.manageAuthentication.ldap.testFailed']}"));

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

	public String getId(Object obj) {
		return "c" + System.identityHashCode(obj) + "Id";
	}

	public void addStrategy() {

	}

	public void addField(PassportConfiguration removePassportConfiguration) {

	}

	public Boolean getPassportEnable() {
		return passportEnable;
	}

	public void setPassportEnable(Boolean passportEnable) {
		this.passportEnable = passportEnable;
	}

	private List<OxIDPAuthConf> getIDPAuthConfOrNull(GluuConfiguration configuration) {
		List<OxIDPAuthConf> idpConfs = configuration.getOxIDPAuthentication();
		List<OxIDPAuthConf> authIdpConfs = new ArrayList<OxIDPAuthConf>();
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

	}

	public String getRecaptchaSiteKey() {
		return recaptchaSiteKey;
	}

	public void setRecaptchaSiteKey(String recaptchaSiteKey) {
		this.recaptchaSiteKey = recaptchaSiteKey;
	}

	public String getRecaptchaSecretKey() {
		return recaptchaSecretKey;
	}

	public void setRecaptchaSecretKey(String recaptchaSecretKey) {
		this.recaptchaSecretKey = recaptchaSecretKey;
	}

	private void setAuthenticationRecaptcha() {
		this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
		this.oxTrustappConfiguration.setRecaptchaSecretKey(this.recaptchaSecretKey);
		this.oxTrustappConfiguration.setRecaptchaSiteKey(this.recaptchaSiteKey);
		this.oxTrustappConfiguration.setAuthenticationRecaptchaEnabled(!authenticationRecaptchaEnabled);
		this.jsonConfigurationService.saveOxTrustappConfiguration(this.oxTrustappConfiguration);

	}

	private void getAuthenticationRecaptcha() {
		this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
		this.recaptchaSecretKey = oxTrustappConfiguration.getRecaptchaSecretKey();
		this.recaptchaSiteKey = oxTrustappConfiguration.getRecaptchaSiteKey();
		this.authenticationRecaptchaEnabled = !oxTrustappConfiguration.isAuthenticationRecaptchaEnabled();

	}

	private boolean shouldEnableSimplePasswordAuth() {
		return !this.sourceConfigs.stream().anyMatch(e -> e.isEnabled());

	}

	public AppConfiguration getOxTrustappConfiguration() {
		return oxTrustappConfiguration;
	}

	public void setOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
		this.oxTrustappConfiguration = oxTrustappConfiguration;
	}

}