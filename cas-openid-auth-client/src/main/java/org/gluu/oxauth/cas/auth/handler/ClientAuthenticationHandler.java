/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.handler;

import java.security.GeneralSecurityException;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.gluu.oxauth.cas.auth.principal.ClientCredential;
import org.gluu.oxauth.client.Client;
import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.gluu.context.J2EContext;
import org.gluu.context.WebContext;
import org.gluu.util.StringHelper;

/**
 * This handler authenticates the client credentials
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public final class ClientAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

	@NotNull
	private final Client<UserProfile> client;

	public ClientAuthenticationHandler(final Client<UserProfile> client) {
		this.client = client;
	}

	/**
	 * {@InheritDoc}
	 */
	@Override
	public boolean supports(final Credential credential) {
		return (credential != null) && ClientCredential.class.isAssignableFrom(credential.getClass());
	}

	/**
	 * {@InheritDoc}
	 */
	@Override
	protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
		final ClientCredential clientCredentials = (ClientCredential) credential;
		final OpenIdCredentials openIdCredentials = clientCredentials.getOpenIdCredentials();
		logger.debug("Client credentials : '{}'", clientCredentials);

		final String clientName = openIdCredentials.getClientName();
		logger.debug("Client name : '{}'", clientName);

		// Web context
		final ServletExternalContext servletExternalContext = (ServletExternalContext) ExternalContextHolder.getExternalContext();
		final HttpServletRequest request = (HttpServletRequest) servletExternalContext.getNativeRequest();
		final HttpServletResponse response = (HttpServletResponse) servletExternalContext.getNativeResponse();
		final WebContext webContext = new J2EContext(request, response);

		// Get user profile
		final UserProfile userProfile = this.client.getUserProfile(openIdCredentials, webContext);
		logger.debug("userProfile : {}", userProfile);

		if (userProfile != null) {
			final String id = userProfile.getId();
			if (StringHelper.isNotEmpty(id)) {
				openIdCredentials.setUserProfile(userProfile);

				return new HandlerResult(this, clientCredentials, new SimplePrincipal(id, userProfile.getAttributes()));
			}
		}

		throw new FailedLoginException("Provider did not produce profile for " + clientCredentials);
	}

}
