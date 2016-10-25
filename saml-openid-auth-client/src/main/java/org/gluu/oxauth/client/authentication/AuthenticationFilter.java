/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.authentication;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gluu.oxauth.client.session.AbstractOAuthFilter;
import org.gluu.oxauth.client.session.OAuthData;
import org.gluu.oxauth.client.util.Configuration;
import org.jboss.resteasy.client.ClientRequest;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;


/**
 * Filter implementation to intercept all requests and attempt to authorize
 * the client by redirecting them to OAuth (unless the client has get authorization code).
 * <p>
 * This filter allows you to specify the following parameters (at either the context-level or the filter-level):
 * <ul>
 * <li><code>oAuthServerAuthorizeUrl</code> - the url to authorize OAuth client, i.e. https://localhost/oxauth/authorize</li>
 * </ul>
 *
 * <p>Please see AbstractOAuthFilter for additional properties</p>
 *
 * @author Yuriy Movchan
 * @version 0.1, 03/20/2013
 */
public class AuthenticationFilter extends AbstractOAuthFilter {

	/**
	 * The URL to the OAuth Server authorization services
	 */

	private final Pattern authModePattern = Pattern.compile(".+/acr_values/([\\d\\w]+)$");

        @Override
	public final void init(final FilterConfig filterConfig) throws ServletException {
	}

        @Override
	public final void destroy() {
	}

        @Override
	public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

		// TODO: check chain
		if (!preFilter(servletRequest, servletResponse, filterChain)) {
			log.debug("Execute validation filter");
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		log.debug("No code and no OAuth data found");

		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		String conversation = request.getParameter("conversation");
		log.info("########## PARAM conversation = " + conversation);

		HttpSession session = request.getSession(false);
		session.setAttribute("conversation", conversation);

		String urlToRedirectTo;
		try {
			urlToRedirectTo = getOAuthRedirectUrl(request, response);
		} catch (Exception ex) {
			log.error("Failed to preapre request to OAuth server", ex);
			return;
		}

		log.debug("Redirecting to \"" + urlToRedirectTo + "\"");

		response.sendRedirect(urlToRedirectTo);
	}

	/**
	 * Determine filter execution conditions
	 */
	protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) servletRequest;

		final HttpSession session = request.getSession(false);

		final OAuthData oAuthData = session != null ? (OAuthData) session.getAttribute(Configuration.SESSION_OAUTH_DATA) : null;
		if (oAuthData != null) {
			return false;
		}

		final String code = getParameter(request, Configuration.OAUTH_CODE);
		log.trace("code value: " + code);
		if (StringHelper.isNotEmpty(code)) {
			return false;
		}

		final String idToken = getParameter(request, Configuration.OAUTH_ID_TOKEN);
		log.trace("id_token value: " + idToken);
		if (StringHelper.isNotEmpty(idToken)) {
			return false;
		}

		return true;
	}

	public String getOAuthRedirectUrl(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		String oAuthAuthorizeUrl = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_AUTHORIZE_URL, null);

		String oAuthClientId = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_ID, null);
		String oAuthClientScope = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_SCOPE, null);

		ClientRequest clientRequest = new ClientRequest(oAuthAuthorizeUrl);
		String responseType = "code+id_token";
		String nonce = "nonce";

		String redirectUri = constructRedirectUrl(request);

		clientRequest.queryParameter("client_id", oAuthClientId);
		clientRequest.queryParameter("scope", oAuthClientScope);
		clientRequest.queryParameter("redirect_uri", redirectUri);
		clientRequest.queryParameter("response_type", responseType);
		clientRequest.queryParameter("nonce", nonce);

		Cookie currentShibstateCookie = getCurrentShibstateCookie(request);
		if (currentShibstateCookie != null) {
			String requestUri = decodeCookieValue(currentShibstateCookie.getValue());
			log.debug("requestUri = \"" + requestUri + "\"");
	
			String authenticationMode = determineAuthenticationMode(requestUri);
	
			if (StringHelper.isNotEmpty(authenticationMode)) {
				log.debug("auth_mode = \"" + authenticationMode + "\"");
				clientRequest.queryParameter(Configuration.OAUTH_AUTH_MODE, authenticationMode);
				updateShibstateCookie(response, currentShibstateCookie, requestUri, "/" + Configuration.OAUTH_AUTH_MODE +"/" + authenticationMode);
			}
		}

		return clientRequest.getUri().replaceAll("%2B", "+");
	}

	private Cookie getCurrentShibstateCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (ArrayHelper.isEmpty(cookies)) {
			return null;
		}

		Cookie resultCookie = null;
		for (Cookie cookie : cookies) {
			String cookieName = cookie.getName();
			if (cookieName.startsWith("_shibstate_")) {
				if (resultCookie == null) {
					resultCookie = cookie;
				} else {
					if (cookieName.compareTo(resultCookie.getName()) > 0) {
						resultCookie = cookie;
					}
				}
			}
		}

		if (resultCookie == null) {
			return null;
		}
		return resultCookie;
	}

	private void updateShibstateCookie(HttpServletResponse response, Cookie shibstateCookie, String requestUri, String acrPathParam) {
		// Check if parameter exists
		if (!requestUri.contains(acrPathParam)) {
			return;
		}

		String newRequestUri = requestUri.replace(acrPathParam, "");

		// Set new cookie
		Cookie updateShibstateCookie = cloneCokie(shibstateCookie, encodeCookieValue(newRequestUri), shibstateCookie.getMaxAge());
		response.addCookie(updateShibstateCookie);
	}
	
	private Cookie cloneCokie(Cookie sourceCookie, String newValue, int maxAge) {
		Cookie resultCookie = new Cookie(sourceCookie.getName(), newValue);

		resultCookie.setPath("/");
		resultCookie.setMaxAge(maxAge);
		resultCookie.setVersion(1);
		resultCookie.setSecure(true);
		
		return resultCookie;
	}

	private String decodeCookieValue(String cookieValue) {
		if (StringHelper.isEmpty(cookieValue)) {
			return null;
		}

		return URLDecoder.decode(cookieValue);
	}

	private String encodeCookieValue(String cookieValue) {
		if (StringHelper.isEmpty(cookieValue)) {
			return null;
		}

		return URLEncoder.encode(cookieValue);
	}

	private String determineAuthenticationMode(String requestUri) {
		return determineAuthenticationParameter(requestUri, authModePattern);
	}

	private String determineAuthenticationParameter(String requestUri, Pattern pattern) {
		Matcher matcher = pattern.matcher(requestUri);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
