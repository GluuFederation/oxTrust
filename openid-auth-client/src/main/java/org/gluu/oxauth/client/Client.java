/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import java.util.Map;

import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.xdi.context.WebContext;

/**
 * This interface represents a client
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public interface Client<U extends UserProfile> {

	/**
	 * Get the name of the client
	 * 
	 * @return the name of the client
	 */
	public String getName();

	/**
	 * Get url for oxAuth authentication
	 * 
	 * @param context
	 * @return url with authorization request
	 */
	public String getRedirectionUrl(final WebContext context);

    /**
     * Get url for oxAuth authentication
     * 
     * @param context
     * @param customStateParameters
     * @param customParameters
     * @return url with authorization request
     */
	public String getRedirectionUrl(final WebContext context, Map<String, String> customStateParameters, final Map<String, String> customParameters);

    /**
     * Get logout url for oxAuth authentication
     * 
     * @param context
     * @return logout url with authorization request
     */
    public String getLogoutRedirectionUrl(final WebContext context);

	/**
	 * Check if web context provides authentication credentials
	 * 
	 * @param context
	 * @return result of check
	 */
	public boolean isAuthorizationResponse(final WebContext context);

    /**
     * Check if web context provides contain id_token
     * 
     * @param context
     * @return result of check
     */
    boolean isAuthorized(WebContext context);

    /**
     * Remove id_token from web context
     * 
     * @param context
     * @return result of check
     */
    void clearAuthorized(WebContext context);

	/**
	 * Check if request state is valid
	 * 
	 * @param context
	 * @return result of check
	 */
	public boolean isValidRequestState(final WebContext context);

	/**
     * Get state parameter from request
     * 
     * @param context
     * @return result of check
     */
    public String getRequestState(final WebContext context);

	/**
	 * Get the credentials from the web context
	 * 
	 * @param context
	 * @return the credentials
	 */
	public OpenIdCredentials getCredentials(final WebContext context);

	/**
	 * Get the user profile from the credentials and web context
	 * 
	 * @param credentials
	 * @param context
	 * @return the user profile
	 */
	public U getUserProfile(final OpenIdCredentials credentials, final WebContext context);

    void setAttribute(WebContext context, String attributeName, Object attributeValue);

    Object getAttribute(WebContext context, String attributeName);

}
