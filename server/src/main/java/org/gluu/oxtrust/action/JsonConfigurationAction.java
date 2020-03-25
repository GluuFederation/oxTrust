/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.ImportPersonConfig;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.EmailUniquenessService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.DataSourceTypeService;
import org.gluu.service.JsonService;
import org.gluu.service.cache.CacheConfiguration;
import org.gluu.service.cache.CacheProviderType;
import org.gluu.service.cache.MemcachedProvider;
import org.gluu.service.cache.RedisConfiguration;
import org.gluu.service.cache.RedisProvider;
import org.gluu.service.cdi.util.CdiUtil;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

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

	private static final String HIDDEN_PASSWORD_TEXT = "hidden";

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

	private AppConfiguration oxTrustappConfiguration;
	private ImportPersonConfig oxTrustImportPersonConfiguration;

	private String oxTrustConfigJson;
	private String oxTrustImportPersonConfigJson;

	private String oxAuthDynamicConfigJson;

	private CacheConfiguration cacheConfiguration;

	private String cacheConfigurationJson;

	public String init() {
		try {
			log.debug("Loading oxauth-config.json and oxtrust-config.json");
			this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			this.oxTrustImportPersonConfiguration = jsonConfigurationService.getOxTrustImportPersonConfiguration();
			this.cacheConfiguration = jsonConfigurationService.getOxMemCacheConfiguration();
			this.oxTrustConfigJson = getProtectedOxTrustappConfiguration(this.oxTrustappConfiguration);
			this.oxTrustImportPersonConfigJson = getOxTrustImportPersonConfiguration(
					this.oxTrustImportPersonConfiguration);
			this.oxAuthDynamicConfigJson = jsonConfigurationService.getOxAuthDynamicConfigJson();
			this.cacheConfigurationJson = getCacheConfiguration(cacheConfiguration);

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
			log.debug("Saving oxauth-config.json:" + oxAuthDynamicConfigJson);
			jsonConfigurationService.saveOxAuthDynamicConfigJson(oxAuthDynamicConfigJson);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxAuthDynamic Configuration is updated.");

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxauth-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxAuth configuration in LDAP");
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveOxTrustConfigJson() {
		try {
			log.debug("Saving oxtrust-config.json:" + this.oxTrustConfigJson);
			this.oxTrustappConfiguration = convertToOxTrustappConfiguration(this.oxTrustConfigJson);
			trimUriProperties();
			if (dataSourceTypeService.isLDAP(attributeService.getDnForAttribute(null))) {
				emailUniquenessService.setEmailUniqueness(this.oxTrustappConfiguration.getEnforceEmailUniqueness());
			}
			jsonConfigurationService.saveOxTrustappConfiguration(this.oxTrustappConfiguration);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrust Configuration is updated.");

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxtrust-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxTrust configuration in LDAP");
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String saveOxMemCacheConfigJson() {
		// Update JSON configurations
		try {
			log.debug("Saving memcache-config.json:" + this.cacheConfigurationJson);
			this.cacheConfiguration = convertToCacheConfiguration(this.cacheConfigurationJson);
			CacheProviderType type = this.cacheConfiguration.getCacheProviderType();
			if (type.equals(CacheProviderType.REDIS) && !canConnectToRedis()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error connecting to redis with provided configuration");
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (type.equals(CacheProviderType.MEMCACHED) && !canConnectToMemCached()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"Error connecting to memcached server with provided configuration");
				return OxTrustConstants.RESULT_FAILURE;
			}
			jsonConfigurationService.saveOxMemCacheConfiguration(this.cacheConfiguration);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Сache Configuration is updated.");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to update oxMemcache-config.json", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxTrust configuration in LDAP");
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	private boolean canConnectToRedis() {
		try {
			RedisProvider provider = CdiUtil.bean(RedisProvider.class);
			CacheConfiguration cConfiguration = this.cacheConfiguration;
			cConfiguration.setRedisConfiguration(this.cacheConfiguration.getRedisConfiguration());
			provider.setCacheConfiguration(cConfiguration);
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
		try {
			AppConfiguration resultOxTrustappConfiguration = (AppConfiguration) BeanUtils
					.cloneBean(oxTrustappConfiguration);
			resultOxTrustappConfiguration.setKeystorePassword(HIDDEN_PASSWORD_TEXT);
			resultOxTrustappConfiguration.setIdpSecurityKeyPassword(HIDDEN_PASSWORD_TEXT);
			resultOxTrustappConfiguration.setIdpBindPassword(HIDDEN_PASSWORD_TEXT);
			resultOxTrustappConfiguration.setCaCertsPassphrase(HIDDEN_PASSWORD_TEXT);
			resultOxTrustappConfiguration.setOxAuthClientPassword(HIDDEN_PASSWORD_TEXT);

			return jsonService.objectToJson(resultOxTrustappConfiguration);
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from appConfiguration: '{}'", oxTrustappConfiguration, ex);
		}

		return null;
	}

	private String getOxTrustImportPersonConfiguration(ImportPersonConfig oxTrustImportPersonConfiguration) {
		try {
			return jsonService.objectToJson(oxTrustImportPersonConfiguration);
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from ImportPersonConfig: '{}'", oxTrustImportPersonConfiguration, ex);
		}

		return null;
	}

	private String getCacheConfiguration(CacheConfiguration cachedConfig) {
		try {
			return jsonService.objectToJson(cachedConfig);
		} catch (Exception ex) {
			log.error("Failed to prepare JSON from ImportPersonConfig: '{}'", oxTrustImportPersonConfiguration, ex);
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
			processPasswordProperty(this.oxTrustappConfiguration, resultOxTrustappConfiguration, "caCertsPassphrase");
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
			processRedisPasswordProperty(redisConfiguration, "password");
			cacheConfiguration.setRedisConfiguration(redisConfiguration);
			return cacheConfiguration;
		} catch (Exception ex) {
			log.error("Failed to prepare CacheConfiguration from JSON: '{}'", oxCacheConfigurationJson, ex);
		}

		return null;
	}

	private void processRedisPasswordProperty(RedisConfiguration current, String property)
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

	public String getCacheConfigurationJson() {
		return cacheConfigurationJson;
	}

	public void setCacheConfigurationJson(String cacheConfigurationJson) {
		this.cacheConfigurationJson = cacheConfigurationJson;
	}
}
