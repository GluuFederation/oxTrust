/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.service.UmaAuthenticationService;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractFilter;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.StringHelper;

/**
 * Custom AuthenticationFilter
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Scope(ScopeType.APPLICATION)
@Name("org.gluu.oxtrust.action.authenticationFilter")
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
@Filter(within = "org.jboss.seam.web.exceptionFilter")
public class AuthenticationFilter extends AbstractFilter {

	private static final String DEFAULT_REALM = "oxTrust";
	private static final String BEARER_TOKEN_TYPE_HEADER = "BearerTokenType" ;

	@Logger
	private Log log;

	private String realm = DEFAULT_REALM;

	private int nonceValiditySeconds = 300;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		setUrlPattern("/seam/resource/restv1/*");
		setRealm("oxTrust REST services");
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getRealm() {
		return realm;
	}

	public int getNonceValiditySeconds() {
		return nonceValiditySeconds;
	}

	public void setNonceValiditySeconds(int value) {       
		this.nonceValiditySeconds = value;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			throw new ServletException("This filter can only process HttpServletRequest requests");
		}
		    
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Exclude SCIM configuration endpoint
		String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
		if (path.endsWith("/oxrust/scim-configuration")) {
		    chain.doFilter(request, response);
		    return;
		}

		new ContextualHttpServletRequest(httpRequest) {
			@Override
			public void process() throws ServletException, IOException, LoginException {
				final String header = httpRequest.getHeader("Authorization");
				if (((header != null) && header.startsWith("Bearer ")) || StringHelper.isNotEmpty(httpRequest.getParameter("authCreds"))) {
					processBearerAuth(httpRequest, httpResponse, chain);
				} else {
					throw new ServletException("Invalid authentication type");
				}
			}
		}.run();
	}

	private void processBearerAuth(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException,
			IOException {
		Identity identity = Identity.instance();
		ApplicationConfiguration applicationConfiguration = OxTrustConfiguration.instance().getApplicationConfiguration();

		if (identity == null) {
			throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.");
		}

		Credentials credentials = identity.getCredentials();

		boolean requireAuth = false;
		String header = request.getHeader("Authorization");
		if ((header != null && header.startsWith("Bearer ")) || StringHelper.isNotEmpty(request.getParameter("authCreds"))) {
			String accessToken = "";
			if (header != null && header.startsWith("Bearer ")) {
				accessToken = header.substring(7);
			} else {
				accessToken = request.getParameter("authCreds");
			}
			
			boolean oxAuthToken = true;
			String bearerTokenTypeHeader = request.getHeader(BEARER_TOKEN_TYPE_HEADER);
			if (StringHelper.isNotEmpty(bearerTokenTypeHeader)) {
				if (StringHelper.equalsIgnoreCase(bearerTokenTypeHeader, "uma")) {
					oxAuthToken = false;
				}
			}

			if (oxAuthToken) {
				requireAuth = !validateOAuthToken(identity, credentials, applicationConfiguration, accessToken);
			} else {
				requireAuth = true;
			}

			if (requireAuth) {
				UmaAuthenticationService umaAuthenticationService = (UmaAuthenticationService) Component.getInstance("umaAuthenticationService");
				if (umaAuthenticationService.isEnabledUmaAuthentication()) {
					chain.doFilter(request, response);
					return;
				}
			}
		}

		if (!identity.isLoggedIn() && !credentials.isSet()) {
			requireAuth = true;
		}

		try {
			if (!requireAuth) {
				chain.doFilter(request, response);
				return;
			}
		} catch (NotLoggedInException ex) {
			requireAuth = true;
		}

		if ((requireAuth && !identity.isLoggedIn())) {
			response.addHeader("WWW-Authenticate", "Bearer realm=\"" + realm + "\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
		}
	}


	public boolean validateOAuthToken(Identity identity, Credentials credentials, ApplicationConfiguration applicationConfiguration, String accessToken) {
		log.info("The accessToken is: " + accessToken);
		try {
			log.info("validating the Access token");

			String validateUrl = applicationConfiguration.getOxAuthTokenValidationUrl();
			ValidateTokenClient validateTokenClient = new ValidateTokenClient(validateUrl);
			ValidateTokenResponse response3 = validateTokenClient.execValidateToken(accessToken);
			log.info("Status is : " + response3.getStatus());
			if (response3.getStatus() == 200) {
				String userInfoEndPoint = applicationConfiguration.getOxAuthUserInfo();
				UserInfoClient userInfoClient = new UserInfoClient(userInfoEndPoint);
				UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);

				String username = null;
				List<String> usernameValues = userInfoResponse.getClaims().get(JwtClaimName.USER_NAME);
				if ((usernameValues != null) && (usernameValues.size() > 0)) {
					username = usernameValues.get(0);
				}

				return authenticateUserSilently(identity, credentials, username);
			}
		} catch (Exception ex) {
			log.warn("Could not validate accessToken." + ex.getMessage());
		}

		return false;
	}

	private boolean authenticateUserSilently(Identity identity, Credentials credentials, String userName) {
		if (!StringHelper.isEmpty(userName)) {
			// Only reauthenticate if username doesn't match Identity.username and user isn't authenticated
			if (!userName.equals(credentials.getUsername()) || !identity.isLoggedIn()) {
				try {
					Authenticator authenticator = getAuthenticator(userName, null);
					return authenticator.authenticateBearerWebService();
				} catch (Exception ex) {
					log.warn("Error authenticating: " + ex.getMessage());
				}
			}
		}

		return false;
	}	

	private Authenticator getAuthenticator(final String userName, String userPassword) {
		Identity identity = Identity.instance();
		identity.getCredentials().setUsername(userName);
		identity.getCredentials().setPassword(userPassword);

		return Authenticator.instance();
	}

}
