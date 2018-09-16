/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.validation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gluu.oxauth.client.authentication.AuthenticationFilter;
import org.gluu.oxauth.client.session.AbstractOAuthFilter;
import org.gluu.oxauth.client.session.OAuthData;
import org.gluu.oxauth.client.util.Configuration;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.model.exception.InvalidJwtException;
import org.xdi.oxauth.model.jwt.Jwt;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import net.shibboleth.idp.authn.ExternalAuthentication;

/**
 * Validates grants recieved from OAuth server.
 * 
 * Add OAuth data to servlet session. Add remoteUser/Principal to servlet request for IDP.
 *
 * @author Yuriy Movchan
 */
public class OAuthValidationFilter extends AbstractOAuthFilter {

    @Override
    public final void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        log.debug("Attempting to validate grants");
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String conversation = request.getHeader(ExternalAuthentication.CONVERSATION_KEY);

        final HttpSession session = request.getSession(false);
        if (session != null && (conversation == null || conversation.isEmpty())) {

                conversation = (String)session.getAttribute(AuthenticationFilter.SESSION_CONVERSATION_KEY);
                if (conversation == null || conversation.isEmpty()) {
                        throw new ServletException("IDP v3 conversation param is null or empty");
                }

                log.debug("########## SESSION conversation = " + conversation);

        } else {
                log.error("Session not created yet");
        }
        
        CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);
        customRequest.addCustomParameter(ExternalAuthentication.CONVERSATION_KEY, conversation);
        
        if (!preFilter(servletRequest, servletResponse, filterChain)) {
            // unauthorized way
            filterChain.doFilter(customRequest, response);
            return;
        }
        
        // authorized way
        final String code = getParameter(request, Configuration.OAUTH_CODE);

        log.debug("Attempting to validate code: " + code);
        try {
                OAuthData oAuthData = getOAuthData(session, request, code);
                session.setAttribute(Configuration.SESSION_OAUTH_DATA, oAuthData);
                
                customRequest.setRemoteUser(oAuthData.getUserUid());
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("Failed to validate code and id_token", ex);

            throw new ServletException(ex);
        }
                
        filterChain.doFilter(customRequest, response);
    }

    /**
     * Determine filter execution conditions
     */
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                                      final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        final String code = getParameter(request, Configuration.OAUTH_CODE);
        if (StringHelper.isNotEmpty(code)) {
            return true;
        }

        return false;
    }

    private OAuthData getOAuthData(HttpSession session, HttpServletRequest request, String authorizationCode) throws Exception {
        // Check state
        String authorizationState = request.getParameter(Configuration.OAUTH_STATE);
        final String stateSession = session != null ? (String) session.getAttribute(Configuration.SESSION_AUTH_STATE) : null;
        if (!StringHelper.equals(stateSession, authorizationState)) {
            log.error("Login failed, oxTrust wasn't allowed to access user data");
            return null;
        }

        String oAuthAuthorizeUrl = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_AUTHORIZE_URL, null);
        String oAuthHost = getOAuthHost(oAuthAuthorizeUrl);

        String oAuthTokenUrl = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_TOKEN_URL, null);
        String oAuthUserInfoUrl = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_USERINFO_URL, null);

        String oAuthClientId = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_ID, null);
        String oAuthClientPassword = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_PASSWORD, null);
        if (oAuthClientPassword != null) {
            try {
                oAuthClientPassword = StringEncrypter.defaultInstance().decrypt(oAuthClientPassword, Configuration.instance().getCryptoPropertyValue());
            } catch (EncryptionException ex) {
                log.error("Failed to decrypt property: " + Configuration.OAUTH_PROPERTY_CLIENT_PASSWORD, ex);
            }
        }

        String scopes = getParameter(request, Configuration.OAUTH_SCOPE);
        log.trace("scopes : " + scopes);

        // 1. Request access token using the authorization code
        log.trace("Getting access token");
        TokenClient tokenClient1 = new TokenClient(oAuthTokenUrl);

        String redirectURL = constructRedirectUrl(request);
        TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectURL, oAuthClientId, oAuthClientPassword);
        if (tokenResponse == null) {
            log.error("Get empty token response. User can't log into application");
            return null;
        }

        log.trace("tokenResponse : " + tokenResponse);
        log.trace("tokenResponse.getErrorType() : " + tokenResponse.getErrorType());

        String accessToken = tokenResponse.getAccessToken();
        String idToken = tokenResponse.getIdToken();
        log.trace("accessToken : " + accessToken);
        log.trace("idToken : " + idToken);

        // Parse JWT
        Jwt jwt;
        try {
            jwt = Jwt.parse(idToken);
        } catch (InvalidJwtException ex) {
            log.error("Failed to parse id_token");
            return null;
        }

        // Check nonce
        String nonceResponse = (String) jwt.getClaims().getClaim(JwtClaimName.NONCE);
        final String nonceSession = session != null ? (String) session.getAttribute(Configuration.SESSION_AUTH_NONCE) : null;
        if (!StringHelper.equals(nonceSession, nonceResponse)) {
            log.error("User info response :  nonce is not matching.");
            return null;
        }

        log.info("Session validation successful. User is logged in");
        UserInfoClient userInfoClient = new UserInfoClient(oAuthUserInfoUrl);

        UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);
        if (userInfoResponse == null) {
            log.error("Get empty user info response. User can't log into application");
            return null;
        }

        OAuthData oAuthData = new OAuthData();
        oAuthData.setHost(oAuthHost);
        // Determine uid
        List<String> uidValues = userInfoResponse.getClaims().get(JwtClaimName.USER_NAME);
        if ((uidValues == null) || (uidValues.size() == 0)) {
            log.error("User infor response doesn't contains uid claim");
            return null;
        }

        oAuthData.setUserUid(uidValues.get(0));
        oAuthData.setAccessToken(accessToken);
        oAuthData.setAccessTokenExpirationInSeconds(tokenResponse.getExpiresIn());
        oAuthData.setScopes(scopes);
        oAuthData.setIdToken(idToken);

        log.trace("User uid: " + oAuthData.getUserUid());
        return oAuthData;
    }

    private String getOAuthHost(String oAuthAuthorizeUrl) {
        try {
            URL url = new URL(oAuthAuthorizeUrl);
            return String.format("%s://%s:%s", url.getProtocol(), url.getHost(), url.getPort());
        } catch (MalformedURLException ex) {
            log.error("Invalid oAuth authorization URI: " + oAuthAuthorizeUrl, ex);
        }

        return null;
    }

    @Override
    public void destroy() {
    }

}
