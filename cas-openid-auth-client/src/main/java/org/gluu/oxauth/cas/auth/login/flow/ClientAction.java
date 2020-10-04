/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.login.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.gluu.oxauth.cas.auth.client.AuthClient;
import org.gluu.oxauth.cas.auth.principal.ClientCredential;
import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.gluu.context.J2EContext;
import org.gluu.context.WebContext;

/**
 * This class represents an action to do oxAuth authentication in CAS
 * 
 * @author Yuriy Movchan 11/13/2014
 */
public final class ClientAction extends AbstractAction {

	private final Logger logger = LoggerFactory.getLogger(ClientAction.class);

	public String DEFAULT_CLIENT_NAME_PARAMETER = "client_name";

	/**
	 * Constants to store request parameters
	 */
	public static final String SERVICE = "service";
	public static final String THEME = "theme";
	public static final String LOCALE = "locale";
	public static final String METHOD = "method";

	@NotNull
	private final AuthClient client;

	@NotNull
	private final CentralAuthenticationService centralAuthenticationService;

	/**
	 * Build the action
	 * 
	 * @param theCentralAuthenticationService
	 *            The service for CAS authentication
	 * @param theClients
	 *            The clients for authentication
	 */
	public ClientAction(final AuthClient client, final CentralAuthenticationService centralAuthenticationService) {
		this.client = client;
		this.centralAuthenticationService = centralAuthenticationService;
	}

	/**
	 * {@InheritDoc}
	 */
	@Override
	protected Event doExecute(final RequestContext context) throws Exception {
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		final HttpServletResponse response = WebUtils.getHttpServletResponse(context);

		// Web context
		final WebContext webContext = new J2EContext(request, response);

		// It's an authentication
		if (client.isAuthorizationResponse(webContext)) {
			logger.info("Procession authentication request");

			// Check if oxAuth request state is correct
			if (!client.isValidRequestState(webContext)) {
				logger.warn("The state in session and in request are not equals");

				// Reinit login page
				prepareForLoginPage(context, webContext);

				return new Event(this, "stop");
			}

			// Try to authenticate
			final ClientCredential credentials = getClientCrendentials(context, webContext);
			if (credentials != null) {
				WebUtils.putTicketGrantingTicketInRequestScope(context,
						this.centralAuthenticationService.createTicketGrantingTicket(credentials));
				return success();
			}
		}

		// Go to login page
		prepareForLoginPage(context, webContext);

		return error();
	}

	/**
	 * Build client credenatils from incomming request
	 * 
	 * @param context The current webflow context
	 * @param webContext The current web context
	 * @return client credentials
	 */
	private ClientCredential getClientCrendentials(final RequestContext context, final WebContext webContext) {
		final OpenIdCredentials openIdCredentials = client.getCredentials(webContext);
		final ClientCredential credentials = new ClientCredential(openIdCredentials);

		// Retrieve parameters from web session
		final Service service = (Service) webContext.getSessionAttribute(SERVICE);
		if (service != null) {
			webContext.setRequestAttribute(SERVICE, service.getId());
		}
		context.getFlowScope().put(SERVICE, service);

		restoreRequestAttribute(webContext, THEME);
		restoreRequestAttribute(webContext, LOCALE);
		restoreRequestAttribute(webContext, METHOD);

		return credentials;
	}

	/**
	 * Prepare the data for the login page
	 * 
	 * @param context The current webflow context
	 * @param webContext The current web context
	 */
	protected void prepareForLoginPage(final RequestContext context, final WebContext webContext) {
		// Save parameters in web session
		final Service service = (Service) context.getFlowScope().get(SERVICE);
		if (service != null) {
			webContext.setSessionAttribute(SERVICE, service);
		}
		saveRequestParameter(webContext, THEME);
		saveRequestParameter(webContext, LOCALE);
		saveRequestParameter(webContext, METHOD);

		final String keyRedirectionUrl = this.client.getName() + "Url";
		final String redirectionUrl = this.client.getRedirectionUrl(webContext);
		logger.debug("Generated redirection Url", redirectionUrl);

		context.getFlowScope().put(keyRedirectionUrl, redirectionUrl);

		final String keyAuthMethod = this.client.getName() + "OpenIdDefaultAuthenticator";
		final Boolean keyAuthMethodValue = this.client.isOpenIdDefaultAuthenticator();
		logger.debug("OpenIdDefaultAuthenticator", keyAuthMethodValue);

		context.getFlowScope().put(keyAuthMethod, keyAuthMethodValue);
	}

	/**
	 * Restore an attribute in web session as an attribute in request
	 * 
	 * @param webContext The current web context
	 * @param name The name of the parameter
	 */
	private void restoreRequestAttribute(final WebContext context, final String name) {
		final String value = (String) context.getSessionAttribute(name);
		context.setRequestAttribute(name, value);
	}

	/**
	 * Save a request parameter in the web session
	 * 
	 * @param webContext The current web context
	 * @param name The name of the parameter
	 */
	private void saveRequestParameter(final WebContext context, final String name) {
		final String value = context.getRequestParameter(name);
		if (value != null) {
			context.setSessionAttribute(name, value);
		}
	}

}
