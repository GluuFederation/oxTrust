/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gluu.oxauth.client.util.Configuration;

/**
 * Listener to detect when an HTTP session is destroyed and remove it from the map of
 * managed sessions.  Also allows for the programmatic removal of sessions.
 * <p>
 * Enables the SAML Single Sign out feature.
 *
 * @author Yuriy Movchan
 * @version 0.1, 03/20/2013
 */
public final class SignOutHandler {

	protected final Log log = LogFactory.getLog(getClass());

	private static class SignOutHandlerSingleton {
		static SignOutHandler INSTANCE = new SignOutHandler();
	}

	private SignOutHandler() {}

	public static SignOutHandler instance() {
		return SignOutHandlerSingleton.INSTANCE;
	}

    public String getOAuthLogoutUrl(final HttpServletRequest servletRequest) {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpSession session = request.getSession(false);

        if (session == null) {
        	log.trace("There is no exising session");
        	return null;
        }

        OAuthData oAuthData = (OAuthData) session.getAttribute(Configuration.SESSION_OAUTH_DATA);
        if (oAuthData == null) {
        	log.trace("There is no OAuthData in the session");
        	return null;
        }
        
        // TODO: Validate access token
        WebTarget webTarget = ClientBuilder.newClient().target(Configuration.instance().getPropertyValue(Configuration.OAUTH_PROPERTY_LOGOUT_URL));

        webTarget = webTarget.queryParam(Configuration.OAUTH_ID_TOKEN_HINT, oAuthData.getIdToken());
        webTarget = webTarget.queryParam(Configuration.OAUTH_POST_LOGOUT_REDIRECT_URI, constructRedirectUrl(request));

		// Remove OAuth data from session
        session.removeAttribute(Configuration.SESSION_OAUTH_DATA);

		try {
			return webTarget.getUri().toString();
		} catch (Exception ex) {
			log.error("Failed to prepare OAuth log out URL", ex);
		}

		return null;
    }

    public final String constructRedirectUrl(final HttpServletRequest request) {
    	log.trace("Starting constructRedirectUrl");
    	
    	String redirectUri = Configuration.instance().getPropertyValue(Configuration.OAUTH_PROPERTY_LOGOUT_REDIRECT_URL);
		if ((redirectUri != null) && !redirectUri.equals("")) {
	    	log.trace("redirectUri from configuration: " + redirectUri);
			return redirectUri;
		}
    	
    	String[] redirectUriParameters = (String[])request.getParameterMap().get(Configuration.OAUTH_POST_LOGOUT_REDIRECT_URI);
		if ((redirectUriParameters != null) && (redirectUriParameters.length > 0)) {
			redirectUri = redirectUriParameters[0];
		}

		if ((redirectUri != null) && !redirectUri.equals("")) {
	    	log.trace("redirectUri from request: " + redirectUri);
			return redirectUri;
		}
    	
    	int serverPort = request.getServerPort();
    	if ((serverPort == 80) || (serverPort == 443)) {
    		redirectUri = String.format("%s://%s%s", request.getScheme(), request.getServerName(), "/identity/authentication/finishlogout");
    	} else {
    		redirectUri = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), "/identity");
    	}
    
    	log.trace("Default redirectUri: " + redirectUri);

    	return redirectUri;
    }
}
