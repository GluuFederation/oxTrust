/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.ImportPersonConfig;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.oxauth.client.*;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.service.JsonService;
import org.xdi.util.security.StringEncrypter;

import javax.ws.rs.core.Response;

/**
 * Provides operations with JSON oxAuth/oxTrust configuration
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Scope(ScopeType.STATELESS)
@Name("jsonConfigurationService")
@AutoCreate
public class JsonConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	@Logger
	private Log log;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private JsonService jsonService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	@In(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;

	public ApplicationConfiguration getOxTrustApplicationConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getApplication();
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
		String configurationDn = oxTrustConfiguration.getConfigurationDn();

		LdapOxTrustConfiguration ldapOxTrustConfiguration = loadOxTrustConfig(configurationDn);
		return ldapOxTrustConfiguration;
	}

	public String getOxAuthDynamicConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		String configurationDn = oxTrustConfiguration.getConfigurationDn();

		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(configurationDn);
		return ldapOxAuthConfiguration.getOxAuthConfigDynamic();
	}

	public boolean saveOxTrustApplicationConfiguration(ApplicationConfiguration oxTrustApplicationConfiguration) {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setApplication(oxTrustApplicationConfiguration);
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
		String configurationDn = oxTrustConfiguration.getConfigurationDn();

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

	public static JsonConfigurationService instance() {
		return (JsonConfigurationService) Component.getInstance(JsonConfigurationService.class);
	}

	public void processScimTestModeIsTrue(ApplicationConfiguration source, ApplicationConfiguration current) throws Exception {

		ApplicationConfiguration applicationConfiguration = getOxTrustApplicationConfiguration();

		if (current.isScimTestMode()) {

			String clientPassword = StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getOxAuthClientPassword(), cryptoConfigurationSalt);

			if (source.getScimTestModeAccessToken() != null && !source.getScimTestModeAccessToken().isEmpty()) {

				// Check if current token is still valid

				String validateTokenEndpoint = applicationConfiguration.getOxAuthTokenValidationUrl();

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
					longLivedTokenRequest.setAuthUsername(applicationConfiguration.getOxAuthClientId());
					longLivedTokenRequest.setAuthPassword(clientPassword);
					longLivedTokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

					TokenClient longLivedTokenClient = new TokenClient(current.getOxAuthTokenUrl());
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
				tokenRequest.setScope(applicationConfiguration.getOxAuthClientScope());
				tokenRequest.setAuthUsername(applicationConfiguration.getOxAuthClientId());
				tokenRequest.setAuthPassword(clientPassword);
				tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

				TokenClient tokenClient = new TokenClient(applicationConfiguration.getOxAuthTokenUrl());
				tokenClient.setRequest(tokenRequest);
				TokenResponse tokenResponse = tokenClient.exec();

				String accessToken = tokenResponse.getAccessToken();
				log.info(" accessToken = " + accessToken);

				// 2. Exchange for long-lived access token
				TokenRequest longLivedTokenRequest = new TokenRequest(GrantType.OXAUTH_EXCHANGE_TOKEN);
				longLivedTokenRequest.setOxAuthExchangeToken(accessToken);
				longLivedTokenRequest.setAuthUsername(applicationConfiguration.getOxAuthClientId());
				longLivedTokenRequest.setAuthPassword(clientPassword);
				longLivedTokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

				TokenClient longLivedTokenClient = new TokenClient(current.getOxAuthTokenUrl());
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
