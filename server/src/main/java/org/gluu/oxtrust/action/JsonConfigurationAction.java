/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.DbApplicationConfiguration;
import org.gluu.config.oxtrust.ImportPersonConfig;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxauth.model.configuration.CIBAEndUserNotificationConfig;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.EmailUniquenessService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.DataSourceTypeService;
import org.gluu.service.JsonService;
import org.gluu.service.cache.*;
import org.gluu.service.cdi.util.CdiUtil;
import org.gluu.service.document.store.conf.DocumentStoreConfiguration;
import org.gluu.service.document.store.conf.DocumentStoreType;
import org.gluu.service.document.store.conf.JcaDocumentStoreConfiguration;
import org.gluu.service.document.store.provider.JcaDocumentStoreProvider;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Action class for json configuring This class loads the JSON configurations
 * e.g. oxTrustConfig from OpenDJ and serves to front end
 * (configuration/update.xhtml) front end uses this JSON String to render JSON
 * editor When edited JSON is submitted back This action class will take care of
 * saving the edited JSON back to OpenDJ
 * 
 * @author Rahat Ali Date: 12/04/2015
 * @author Yuriy Movchan Date: 10/23/2015
 */
@Named("jsonConfigAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class JsonConfigurationAction implements Serializable {

	private String HIDDEN_PASSWORD_TEXT = "hidden";

	private static final long serialVersionUID = -4470460481895022468L;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private Logger log;

	@Inject
	private JsonService jsonService;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private EncryptionService encryptionService;

	@Inject
	private ConversationService conversationService;

	@Inject
	private EmailUniquenessService emailUniquenessService;

	@Inject
	private DataSourceTypeService dataSourceTypeService;

    @Inject
    private StringEncrypter stringEncrypter;

    private AppConfiguration oxTrustappConfiguration;
	private ImportPersonConfig oxTrustImportPersonConfiguration;

	private String oxTrustConfigJson;
	private String oxTrustImportPersonConfigJson;

	private String oxAuthDynamicConfigJson;

	private CacheConfiguration cacheConfiguration;
	private DocumentStoreConfiguration storeConfiguration;

	private String cacheConfigurationJson;
	private String storeConfigurationJson;

	private String fido2ConfigJson;

	public String init() {
		try {
			log.debug("Loading oxauth-config.json and oxtrust-config.json");
			this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			this.oxTrustImportPersonConfiguration = jsonConfigurationService.getOxTrustImportPersonConfiguration();
			this.cacheConfiguration = jsonConfigurationService.getOxMemCacheConfiguration();
			if (this.cacheConfiguration.getRedisConfiguration().getPassword() != null) {
				decryptPassword(this.cacheConfiguration.getRedisConfiguration());
			}
			this.storeConfiguration = jsonConfigurationService.getDocumentStoreConfiguration();
			if (this.storeConfiguration.getJcaConfiguration().getPassword() != null) {
				decryptPassword(this.storeConfiguration.getJcaConfiguration());
			}
			this.oxTrustConfigJson = getProtectedOxTrustappConfiguration(this.oxTrustappConfiguration);
			this.oxTrustImportPersonConfigJson = getOxTrustImportPersonConfiguration(
					this.oxTrustImportPersonConfiguration);
			this.oxAuthDynamicConfigJson = getProtectedOxAuthAppConfiguration(
					this.jsonConfigurationService.getOxAuthDynamicConfigJson());
			this.cacheConfigurationJson = getCacheConfiguration(cacheConfiguration);
			this.storeConfigurationJson = getStoreConfiguration(storeConfiguration);
			DbApplicationConfiguration loadFido2Configuration = jsonConfigurationService.loadFido2Configuration();
			if (loadFido2Configuration != null) {
				this.fido2ConfigJson = loadFido2Configuration.getDynamicConf();
			}
			if ((this.oxTrustConfigJson != null) && (this.oxAuthDynamicConfigJson != null)) {
				return OxTrustConstants.RESULT_SUCCESS;
			}
		} catch (Exception ex) {
			log.error("Failed to load configuration from LDAP", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load configuration from LDAP");
		}
		conversationService.endConversation();
		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveOxAuthDynamicConfigJson() {
		// Update JSON configurations
		try {
			log.debug("Saving oxauth-config.json:" + this.oxAuthDynamicConfigJson);
			if (this.oxAuthDynamicConfigJson != null) {
				String configurationJson = convertToOxAuthAppConfiguration(oxAuthDynamicConfigJson);
				if (configurationJson == null) {
					log.error("Failed to prepare update oxauth-config.json");
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to prepare oxAuth configuration for update in DB");
					return OxTrustConstants.RESULT_FAILURE;
				} else {
					jsonConfigurationService.saveOxAuthDynamicConfigJson(configurationJson);
				}
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxAuthDynamic Configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxauth-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxAuth configuration in DB");
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveOxTrustConfigJson() {
		try {
			log.debug("Saving oxtrust-config.json:" + this.oxTrustConfigJson);
			if (this.oxTrustConfigJson != null) {
				this.oxTrustappConfiguration = convertToOxTrustappConfiguration(this.oxTrustConfigJson);
				trimUriProperties();
				if (dataSourceTypeService.isLDAP(attributeService.getDnForAttribute(null))) {
					emailUniquenessService.setEmailUniqueness(this.oxTrustappConfiguration.getEnforceEmailUniqueness());
				}
				jsonConfigurationService.saveOxTrustappConfiguration(this.oxTrustappConfiguration);
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrust Configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxtrust-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxTrust configuration in LDAP");
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveFido2ConfigJson() {
		try {
			log.debug("Saving fido2-config.json:" + this.fido2ConfigJson);
			if (this.fido2ConfigJson != null) {
				jsonConfigurationService.saveFido2Configuration(this.fido2ConfigJson);
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Fido2 Configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update fido2-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Fido2 configuration in DB");
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveOxMemCacheConfigJson() {
		// Update JSON configurations
		try {
			log.debug("Saving memcache-config.json:" + this.cacheConfigurationJson);

			if (this.cacheConfigurationJson != null) {
				this.cacheConfiguration = convertToCacheConfiguration(this.cacheConfigurationJson);
				CacheProviderType type = this.cacheConfiguration.getCacheProviderType();
				if (type.equals(CacheProviderType.REDIS) && !canConnectToRedis()) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"Error connecting to redis with provided configuration");
					return OxTrustConstants.RESULT_FAILURE;
				}
				if (type.equals(CacheProviderType.MEMCACHED) && !canConnectToMemCached()) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"Error connecting to memcached server with provided configuration");
					return OxTrustConstants.RESULT_FAILURE;
				}
				jsonConfigurationService.saveOxMemCacheConfiguration(this.cacheConfiguration);
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Сache Configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxMemcache-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxTrust configuration in LDAP");
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveStoreConfigJson() {
		// Update JSON configurations
		try {
			log.debug("Saving store-config.json:" + this.storeConfigurationJson);
			if (this.storeConfigurationJson != null) {
				this.storeConfiguration = convertToStoreConfiguration(this.storeConfigurationJson);
				DocumentStoreType type = this.storeConfiguration.getDocumentStoreType();
				if (type.equals(DocumentStoreType.JCA) && !canConnectToJca()) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"Error connecting to JCA with provided configuration");
					return OxTrustConstants.RESULT_FAILURE;
				}
				jsonConfigurationService.saveDocumentStoreConfiguration(this.storeConfiguration);
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Document store configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update store-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update document store configuration in DB");
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	private boolean canConnectToRedis() {
		try {
            decryptPassword(this.cacheConfiguration.getRedisConfiguration());
            AbstractRedisProvider provider = RedisProviderFactory.create(this.cacheConfiguration.getRedisConfiguration());
			provider.create();
			if (provider.isConnected()) {
				provider.destroy();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

    private void decryptPassword(RedisConfiguration redisConfiguration) {
        try {
            String encryptedPassword = redisConfiguration.getPassword();
            if (StringUtils.isNotBlank(encryptedPassword)) {
                redisConfiguration.setPassword(stringEncrypter.decrypt(encryptedPassword));
                log.trace("Decrypted redis password successfully.");
            }
        } catch (StringEncrypter.EncryptionException e) {
            log.error("Error during redis password decryption", e);
        }
    }

    private void decryptPassword(JcaDocumentStoreConfiguration documentStoreConfiguration) {
        try {
            String encryptedPassword = documentStoreConfiguration.getPassword();
            if (StringUtils.isNotBlank(encryptedPassword)) {
            	documentStoreConfiguration.setPassword(stringEncrypter.decrypt(encryptedPassword));
                log.trace("Decrypted JCA store password successfully.");
            }
        } catch (StringEncrypter.EncryptionException e) {
            log.error("Error during JCA store password decryption", e);
        }
    }

    private boolean canConnectToMemCached() {
		try {
			MemcachedProvider provider = CdiUtil.bean(MemcachedProvider.class);
			provider.setCacheConfiguration(cacheConfiguration);
			provider.init();
			provider.create();
			if (provider.isConnected()) {
				provider.destroy();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}

	}

	private boolean canConnectToJca() {
		try {
			JcaDocumentStoreProvider provider = CdiUtil.bean(JcaDocumentStoreProvider.class);
			provider.setJcaDocumentStoreConfiguration(storeConfiguration.getJcaConfiguration());
			provider.create();
			if (provider.isConnected()) {
				provider.destroy();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private void trimUriProperties() {
		this.oxTrustappConfiguration
				.setLogoutRedirectUrl(StringHelper.trimAll(this.oxTrustappConfiguration.getLogoutRedirectUrl()));
		this.oxTrustappConfiguration
				.setLoginRedirectUrl(StringHelper.trimAll(this.oxTrustappConfiguration.getLoginRedirectUrl()));
		this.oxTrustappConfiguration.setOxAuthSectorIdentifierUrl(
				StringHelper.trimAll(this.oxTrustappConfiguration.getOxAuthSectorIdentifierUrl()));
	}

	public String saveOxTrustImportPersonConfigJson() {
		// Update JSON configurations
		try {
			log.debug("Saving oxtrust-import-person.json:" + this.oxTrustImportPersonConfigJson);
			this.oxTrustImportPersonConfiguration = convertToOxTrustImportPersonConfiguration(
					this.oxTrustImportPersonConfigJson);
			jsonConfigurationService.saveOxTrustImportPersonConfiguration(this.oxTrustImportPersonConfiguration);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrust Import Person Configuration is updated.");

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to oxtrust-import-person.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to update oxTrust Import Person configuration in LDAP");
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	private String getProtectedOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
		if (oxTrustappConfiguration != null) {
			try {
				AppConfiguration resultOxTrustappConfiguration = (AppConfiguration) BeanUtils
						.cloneBean(oxTrustappConfiguration);
				resultOxTrustappConfiguration.setKeystorePassword(HIDDEN_PASSWORD_TEXT);
				resultOxTrustappConfiguration.setIdpSecurityKeyPassword(HIDDEN_PASSWORD_TEXT);
				resultOxTrustappConfiguration.setIdpBindPassword(HIDDEN_PASSWORD_TEXT);
				resultOxTrustappConfiguration.setOxAuthClientPassword(HIDDEN_PASSWORD_TEXT);

				return jsonService.objectToJson(resultOxTrustappConfiguration);
			} catch (Exception ex) {
				log.error("Failed to prepare JSON from appConfiguration: '{}'", oxTrustappConfiguration, ex);
			}

			return null;
		}
		return null;

	}

	private String getProtectedOxAuthAppConfiguration(String oxAuthAppConfiguration) {

		if (oxAuthAppConfiguration != null) {
			try {
				org.gluu.oxauth.model.configuration.AppConfiguration appConfiguration = jsonService.jsonToObject(
						oxAuthAppConfiguration, org.gluu.oxauth.model.configuration.AppConfiguration.class);

				// Add missing config if needed
				if (appConfiguration.getCibaEndUserNotificationConfig() == null) {
					appConfiguration.setCibaEndUserNotificationConfig(new CIBAEndUserNotificationConfig());
					appConfiguration.getCibaEndUserNotificationConfig().setNotificationKey("");
				}

				try {
					String decryptedKey = encryptionService
							.decrypt(appConfiguration.getCibaEndUserNotificationConfig().getNotificationKey());
					appConfiguration.getCibaEndUserNotificationConfig().setNotificationKey(decryptedKey);
				} catch (EncryptionException ex) {
					log.error("Failed to decrypt values in the oxAuth json configuration: '{}'", oxAuthAppConfiguration,
							ex);
					appConfiguration.getCibaEndUserNotificationConfig().setNotificationKey("");
				}
				return jsonService.objectToJson(appConfiguration);
			} catch (Exception e) {
				log.error("Problems processing oxAuth App configuration file: {}", oxAuthAppConfiguration, e);
				return null;
			}
		}
		return null;

	}

	private String getOxTrustImportPersonConfiguration(ImportPersonConfig oxTrustImportPersonConfiguration) {
		try {
			if (oxTrustImportPersonConfiguration != null) {
				return jsonService.objectToJson(oxTrustImportPersonConfiguration);
			}
			return null;
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from ImportPersonConfig: '{}'", oxTrustImportPersonConfiguration, ex);
		}

		return null;
	}

	private String getCacheConfiguration(CacheConfiguration cachedConfig) {
		try {
			if (cachedConfig != null) {
				return jsonService.objectToJson(cachedConfig);
			}
			return null;
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from CacheConfiguration: '{}'", cachedConfig, ex);
		}

		return null;
	}

	private String getStoreConfiguration(DocumentStoreConfiguration documentStoreConfiguration) {
		try {
			return jsonService.objectToJson(documentStoreConfiguration);
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from documentStoreConfiguration: '{}'", documentStoreConfiguration, ex);
		}

		return null;
	}

	private AppConfiguration convertToOxTrustappConfiguration(String oxTrustappConfigurationJson) {
		try {
			AppConfiguration resultOxTrustappConfiguration = jsonService.jsonToObject(oxTrustappConfigurationJson,
					AppConfiguration.class);
			processPasswordProperty(this.oxTrustappConfiguration, resultOxTrustappConfiguration, "keystorePassword");
			processPasswordProperty(this.oxTrustappConfiguration, resultOxTrustappConfiguration,
					"idpSecurityKeyPassword");
			processPasswordProperty(this.oxTrustappConfiguration, resultOxTrustappConfiguration, "idpBindPassword");
			processPasswordProperty(this.oxTrustappConfiguration, resultOxTrustappConfiguration,
					"oxAuthClientPassword");

			// jsonConfigurationService.processScimTestModeIsTrue(this.oxTrustappConfiguration,
			// resultOxTrustappConfiguration);
			return resultOxTrustappConfiguration;
		} catch (Exception ex) {
			log.error("Failed to prepare appConfiguration from JSON: '{}'", oxTrustappConfigurationJson, ex);
		}

		return null;
	}

	private String convertToOxAuthAppConfiguration(String oxAuthAppConfigurationJson) {
		try {
			org.gluu.oxauth.model.configuration.AppConfiguration appConfiguration = jsonService.jsonToObject(
					oxAuthAppConfigurationJson, org.gluu.oxauth.model.configuration.AppConfiguration.class);
			String encryptedKey = encryptionService
					.encrypt(appConfiguration.getCibaEndUserNotificationConfig().getNotificationKey());
			appConfiguration.getCibaEndUserNotificationConfig().setNotificationKey(encryptedKey);
			return jsonService.objectToJson(appConfiguration);
		} catch (Exception ex) {
			log.error("Failed to prepare oxAuth AppConfiguration from JSON: '{}'", oxAuthAppConfigurationJson, ex);
		}
		return null;
	}

	private ImportPersonConfig convertToOxTrustImportPersonConfiguration(String oxTrustImportPersonConfigJson) {
		try {
			ImportPersonConfig resultOxTrustImportPersonConfiguration = jsonService
					.jsonToObject(oxTrustImportPersonConfigJson, ImportPersonConfig.class);

			return resultOxTrustImportPersonConfiguration;
		} catch (Exception ex) {
			log.error("Failed to prepare ImportPersonConfig from JSON: '{}'", oxTrustImportPersonConfigJson, ex);
		}

		return null;
	}

	private CacheConfiguration convertToCacheConfiguration(String oxCacheConfigurationJson) {
		try {
			CacheConfiguration cacheConfiguration = jsonService.jsonToObject(oxCacheConfigurationJson,
					CacheConfiguration.class);
			RedisConfiguration redisConfiguration = cacheConfiguration.getRedisConfiguration();
			processPasswordProperty(redisConfiguration, "password");
			cacheConfiguration.setRedisConfiguration(redisConfiguration);
			return cacheConfiguration;
		} catch (Exception ex) {
			log.error("Failed to prepare CacheConfiguration from JSON: '{}'", oxCacheConfigurationJson, ex);
		}

		return null;
	}

	private DocumentStoreConfiguration convertToStoreConfiguration(String storeConfigurationJson) {
		try {
			DocumentStoreConfiguration storeConfiguration = jsonService.jsonToObject(storeConfigurationJson,
					DocumentStoreConfiguration.class);
			processPasswordProperty(storeConfiguration.getJcaConfiguration(), "password");
			return storeConfiguration;
		} catch (Exception ex) {
			log.error("Failed to prepare DocumentStoreConfiguration from JSON: '{}'", storeConfigurationJson, ex);
		}

		return null;
	}

	private void processPasswordProperty(Object current, String property)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, EncryptionException {
		String currentValue = BeanUtils.getProperty(current, property);
		BeanUtils.setProperty(current, property, encryptionService.encrypt(currentValue));
	}

	private void processPasswordProperty(AppConfiguration source, AppConfiguration current, String property)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, EncryptionException {
		String currentValue = BeanUtils.getProperty(current, property);
		if (StringHelper.equals(currentValue, HIDDEN_PASSWORD_TEXT)) {
			BeanUtils.setProperty(current, property, BeanUtils.getSimpleProperty(source, property));
		} else {
			BeanUtils.setProperty(current, property, encryptionService.encrypt(currentValue));
		}
	}

	public String getOxTrustConfigJson() {
		return oxTrustConfigJson;
	}

	public void setOxTrustConfigJson(String oxTrustConfigJson) {
		this.oxTrustConfigJson = oxTrustConfigJson;
	}

	public String getOxTrustImportPersonConfigJson() {
		return oxTrustImportPersonConfigJson;
	}

	public void setOxTrustImportPersonConfigJson(String oxTrustImportPersonConfigJson) {
		this.oxTrustImportPersonConfigJson = oxTrustImportPersonConfigJson;
	}

	public String getOxAuthDynamicConfigJson() {
		return oxAuthDynamicConfigJson;
	}

	public void setOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) {
		this.oxAuthDynamicConfigJson = oxAuthDynamicConfigJson;
	}

	public String getFido2ConfigJson() {
		return fido2ConfigJson;
	}

	public void setFido2ConfigJson(String fido2ConfigJson) {
		this.fido2ConfigJson = fido2ConfigJson;
	}

	public String getCacheConfigurationJson() {
		return cacheConfigurationJson;
	}

	public void setCacheConfigurationJson(String cacheConfigurationJson) {
		this.cacheConfigurationJson = cacheConfigurationJson;
	}

	public String getStoreConfigurationJson() {
		return storeConfigurationJson;
	}

	public void setStoreConfigurationJson(String storeConfigurationJson) {
		this.storeConfigurationJson = storeConfigurationJson;
	}

}
