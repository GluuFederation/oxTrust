/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import java.util.Map;

import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.gluu.context.WebContext;

/**
 * This interface represents a client
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public interface Client<U extends UserProfile> {

	public String getName();

	public String getRedirectionUrl(final WebContext context);

    /**
     * Get url for oxAuth authentication
     * 
     * @param context
     * @param customStateParameters
     * @param customParameters
     * @return url with authorization request
     */
	public String getRedirectionUrl(final WebContext context, Map<String, String> customStateParameters, final Map<String, String> customParameters, final boolean force);

    public String getLogoutRedirectionUrl(final WebContext context);

	public boolean isAuthorizationResponse(final WebContext context);

    boolean isAuthorized(WebContext context);

    void clearAuthorized(WebContext context);

	public boolean isValidRequestState(final WebContext context);

    public String getRequestState(final WebContext context);

	public OpenIdCredentials getCredentials(final WebContext context);

	public U getUserProfile(final OpenIdCredentials credentials, final WebContext context);

    void setAttribute(WebContext context, String attributeName, Object attributeValue);

    Object getAttribute(WebContext context, String attributeName);

}
