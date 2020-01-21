/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.util;

import org.gluu.conf.model.AppConfiguration;
import org.gluu.conf.model.AppConfigurationEntry;
import org.gluu.oxauth.client.OpenIdClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.model.util.Util;
import org.gluu.util.StringHelper;

/**
 * oAuth properties and constants
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/20/2013
 */
public final class Configuration {

    /**T
     * Represents the constant for where the OAuth data will be located in memory
     */
	public static final String SESSION_OAUTH_DATA = "_oauth_data_";
    public static final String SESSION_AUTH_STATE = "_auth_state_";
    public static final String SESSION_AUTH_NONCE = "_auth_nonce_";

	/**
	 * OAuth constants
	 */
	public static final String OAUTH_CLIENT_ID = "client_id";
	public static final String OAUTH_CLIENT_PASSWORD = "client_password";
	public static final String OAUTH_CLIENT_CREDENTIALS = "client_credentials";
	public static final String OAUTH_REDIRECT_URI = "redirect_uri";
	public static final String OAUTH_RESPONSE_TYPE = "response_type";
	public static final String OAUTH_SCOPE = "scope";
	public static final String OAUTH_STATE = "state";
	public static final String OAUTH_CODE = "code";
	public static final String OAUTH_ID_TOKEN = "id_token";
	public static final String OAUTH_ERROR = "error";
	public static final String OAUTH_NONCE = "nonce";
	public static final String OAUTH_ERROR_DESCRIPTION = "error_description";
	public static final String OAUTH_ACCESS_TOKEN = "access_token";
	public static final String OXAUTH_ACR_VALUES = "acr_values";
    public static final String OAUTH_ID_TOKEN_HINT = "id_token_hint";
    public static final String OAUTH_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

	/**
	 * OAuth properties
	 */
	public static final String OAUTH_PROPERTY_AUTHORIZE_URL = "oxauth.authorize.url";
	public static final String OAUTH_PROPERTY_TOKEN_URL = "oxauth.token.url";
	public static final String OAUTH_PROPERTY_USERINFO_URL = "oxauth.userinfo.url";
	public static final String OAUTH_PROPERTY_LOGOUT_URL = "oxauth.logout.url";
	public static final String OAUTH_PROPERTY_LOGOUT_REDIRECT_URL = "oxauth.logout.redirect_url";
	public static final String OAUTH_PROPERTY_CLIENT_ID = "oxauth.client.id";
	public static final String OAUTH_PROPERTY_CLIENT_PASSWORD = "oxauth.client.password";
	public static final String OAUTH_PROPERTY_CLIENT_SCOPE = "oxauth.client.scope";

	

	private static class ConfigurationSingleton {
		static Configuration INSTANCE = new Configuration();
	}

	private AppConfiguration appConfiguration;

	private OpenIdConfigurationResponse openIdConfiguration;

	private Configuration() {
    	SamlConfiguration samlConfiguration = SamlConfiguration.instance();
    	this.appConfiguration = samlConfiguration.getAppConfiguration();

    	OpenIdClient<AppConfiguration, AppConfigurationEntry> openIdClient = new OpenIdClient<AppConfiguration, AppConfigurationEntry>(samlConfiguration);
    	openIdClient.init();

    	this.openIdConfiguration = openIdClient.getOpenIdConfiguration();
	}

	public static Configuration instance() {
		return ConfigurationSingleton.INSTANCE;
	}

	public String getPropertyValue(String propertyName) {
    	if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_AUTHORIZE_URL, propertyName)) {
    		return openIdConfiguration.getAuthorizationEndpoint();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_TOKEN_URL, propertyName)) {
    		return openIdConfiguration.getTokenEndpoint();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_USERINFO_URL, propertyName)) {
    		return openIdConfiguration.getUserInfoEndpoint();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_LOGOUT_URL, propertyName)) {
    		return openIdConfiguration.getEndSessionEndpoint();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_LOGOUT_REDIRECT_URL, propertyName)) {
    		return appConfiguration.getOpenIdPostLogoutRedirectUri();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_CLIENT_ID, propertyName)) {
    		return appConfiguration.getOpenIdClientId();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_CLIENT_PASSWORD, propertyName)) {
    		return appConfiguration.getOpenIdClientPassword();
    	} else if (StringHelper.equalsIgnoreCase(Configuration.OAUTH_PROPERTY_CLIENT_SCOPE, propertyName)) {
    		return Util.listAsString(appConfiguration.getOpenIdScopes());
    	}

    	return null;
	}

	public String getCryptoPropertyValue() {
		return SamlConfiguration.instance().getCryptoConfigurationSalt();
	}

}
