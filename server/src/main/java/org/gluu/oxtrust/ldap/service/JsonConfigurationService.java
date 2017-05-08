/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.Serializable;

import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.Component;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;

import javax.faces.application.FacesMessage;import javax.enterprise.context.ConversationScoped;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.ImportPersonConfig;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.config.oxtrust.LdapconfigurationFactory;
import org.xdi.oxauth.client.*;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.service.JsonService;
import org.xdi.service.cache.CacheConfiguration;
import org.xdi.util.security.StringEncrypter;

import javax.ws.rs.core.Response;

/**
 * Provides operations with JSON oxAuth/oxTrust configuration
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Stateless
@Named("jsonConfigurationService")
public class JsonConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	@Inject
	private Logger log;

	@Inject
	private LdapEntryManager ldapEntryManager;

	@Inject
	private JsonService jsonService;

	@Inject
	private OpenIdService openIdService;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private StringEncrypter stringEncrypter;
	
	@Inject
	private ApplianceService applianceService;

	public AppConfiguration getOxTrustappConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getApplication();
	}
	
	public CacheConfiguration getOxMemCacheConfiguration() {
		CacheConfiguration cachedConfiguration = applianceService.getAppliance().getCacheConfiguration();
		return cachedConfiguration;
	}
	
	public boolean saveOxMemCacheConfiguration(CacheConfiguration cachedConfiguration) {
		GluuAppliance gluuAppliance = applianceService.getAppliance();
		gluuAppliance.setCacheConfiguration(cachedConfiguration);
		applianceService.updateAppliance(gluuAppliance);
		return true;
	}

	public ImportPersonConfig getOxTrustImportPersonConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getImportPersonConfig();
	}

	public CacheRefreshConfiguration getOxTrustCacheRefreshConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getCacheRefresh();
	}

	private LdapOxTrustConfiguration getOxTrustConfiguration() {
		String configurationDn = configurationFactory.getConfigurationDn();

		LdapOxTrustConfiguration ldapOxTrustConfiguration = loadOxTrustConfig(configurationDn);
		return ldapOxTrustConfiguration;
	}

	public String getOxAuthDynamicConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		String configurationDn = configurationFactory.getConfigurationDn();

		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(configurationDn);
		return ldapOxAuthConfiguration.getOxAuthConfigDynamic();
	}

	public boolean saveOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setApplication(oxTrustappConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustImportPersonConfiguration(ImportPersonConfig oxTrustImportPersonConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setImportPersonConfig(oxTrustImportPersonConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustCacheRefreshConfiguration(CacheRefreshConfiguration oxTrustCacheRefreshConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setCacheRefresh(oxTrustCacheRefreshConfiguration);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) throws JsonParseException, JsonMappingException, IOException {
		String configurationDn = configurationFactory.getConfigurationDn();

		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(configurationDn);
		ldapOxAuthConfiguration.setOxAuthConfigDynamic(oxAuthDynamicConfigJson);
		ldapOxAuthConfiguration.setRevision(ldapOxAuthConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxAuthConfiguration);
		return true;
	}

	private LdapOxTrustConfiguration loadOxTrustConfig(String configurationDn) {
		try {
			LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn);

			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	private LdapOxAuthConfiguration loadOxAuthConfig(String configurationDn) {
		try {
			configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
			LdapOxAuthConfiguration conf = ldapEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	public void processScimTestModeIsTrue(AppConfiguration source, AppConfiguration current) throws Exception {

		AppConfiguration appConfiguration = getOxTrustappConfiguration();

		if (current.isScimTestMode()) {
			OpenIdConfigurationResponse openIdConfiguration = openIdService.getOpenIdConfiguration();

			String clientPassword = StringEncrypter.defaultInstance().decrypt(appConfiguration.getOxAuthClientPassword(), cryptoConfigurationSalt);

			if (source.getScimTestModeAccessToken() != null && !source.getScimTestModeAccessToken().isEmpty()) {
				// Check if current token is still valid
				String validateTokenEndpoint = openIdConfiguration.getValidateTokenEndpoint();

				ValidateTokenClient validateTokenClient = new ValidateTokenClient(validateTokenEndpoint);
				ValidateTokenResponse validateTokenResponse = validateTokenClient.execValidateToken(source.getScimTestModeAccessToken());

				log.info(" (JsonConfigurationService) validateToken token = " + current.getScimTestModeAccessToken());
				log.info(" (JsonConfigurationService) validateToken status = " + validateTokenResponse.getStatus());
				log.info(" (JsonConfigurationService) validateToken entity = " + validateTokenResponse.getEntity());
				log.info(" (JsonConfigurationService) validateToken isValid = " + validateTokenResponse.isValid());
				log.info(" (JsonConfigurationService) validateToken expires = " + validateTokenResponse.getExpiresIn());

				if (!validateTokenResponse.isValid() ||
					(validateTokenResponse.getExpiresIn() == null || (validateTokenResponse.getExpiresIn() != null && validateTokenResponse.getExpiresIn() <= 0)) ||
					(validateTokenResponse.getStatus() != Response.Status.OK.getStatusCode())) {

					log.info(" (processScimTestModeIsTrue) Current long-lived token has expired, requesting a new one...");

					//  Request new long-lived access token
					TokenRequest longLivedTokenRequest = new TokenRequest(GrantType.OXAUTH_EXCHANGE_TOKEN);
					longLivedTokenRequest.setOxAuthExchangeToken(source.getScimTestModeAccessToken());
					longLivedTokenRequest.setAuthUsername(appConfiguration.getOxAuthClientId());
					longLivedTokenRequest.setAuthPassword(clientPassword);
					longLivedTokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

					TokenClient longLivedTokenClient = new TokenClient(openIdConfiguration.getTokenEndpoint());
					longLivedTokenClient.setRequest(longLivedTokenRequest);
					TokenResponse longLivedTokenResponse = longLivedTokenClient.exec();

					String longLivedAccessToken = longLivedTokenResponse.getAccessToken();
					log.info(" longLivedAccessToken = " + longLivedAccessToken);

					current.setScimTestModeAccessToken(longLivedAccessToken);
					source.setScimTestModeAccessToken(longLivedAccessToken);

				} else {
					log.info(" (processScimTestModeIsTrue) Current long-lived token still valid");
				}
			} else {
				log.info(" (processScimTestModeIsTrue) Requesting for a first time long-lived access token...");

				// 1. Request short-lived access token
				TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
				tokenRequest.setScope(appConfiguration.getOxAuthClientScope());
				tokenRequest.setAuthUsername(appConfiguration.getOxAuthClientId());
				tokenRequest.setAuthPassword(clientPassword);
				tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

				TokenClient tokenClient = new TokenClient(openIdConfiguration.getTokenEndpoint());
				tokenClient.setRequest(tokenRequest);
				TokenResponse tokenResponse = tokenClient.exec();

				String accessToken = tokenResponse.getAccessToken();
				log.info(" accessToken = " + accessToken);

				// 2. Exchange for long-lived access token
				TokenRequest longLivedTokenRequest = new TokenRequest(GrantType.OXAUTH_EXCHANGE_TOKEN);
				longLivedTokenRequest.setOxAuthExchangeToken(accessToken);
				longLivedTokenRequest.setAuthUsername(appConfiguration.getOxAuthClientId());
				longLivedTokenRequest.setAuthPassword(clientPassword);
				longLivedTokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

				TokenClient longLivedTokenClient = new TokenClient(openIdConfiguration.getTokenEndpoint());
				longLivedTokenClient.setRequest(longLivedTokenRequest);
				TokenResponse longLivedTokenResponse = longLivedTokenClient.exec();

				String longLivedAccessToken = longLivedTokenResponse.getAccessToken();
				log.info(" longLivedAccessToken = " + longLivedAccessToken);

				current.setScimTestModeAccessToken(longLivedAccessToken);
				source.setScimTestModeAccessToken(longLivedAccessToken);
			}
		}

		source.setScimTestMode(current.isScimTestMode());
	}
}
