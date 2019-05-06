/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.authentication;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.gluu.oxauth.client.session.AbstractOAuthFilter;
import org.gluu.oxauth.client.session.OAuthData;
import org.gluu.oxauth.client.util.Configuration;
import org.opensaml.profile.context.ProfileRequestContext;
import org.gluu.oxauth.client.AuthorizationRequest;
import org.gluu.oxauth.client.model.JwtState;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxauth.model.crypto.OxAuthCryptoProvider;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.util.StringUtils;
import org.gluu.util.ArrayHelper;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.json.JSONObject;

/**
 * Filter implementation to intercept all requests and attempt to authorize the
 * client by redirecting them to OAuth (unless the client has get authorization
 * code).
 * <p>
 * This filter allows you to specify the following parameters (at either the
 * context-level or the filter-level):
 * <ul>
 * <li><code>oAuthServerAuthorizeUrl</code> - the url to authorize OAuth client,
 * i.e. https://localhost/oxauth/authorize</li>
 * </ul>
 *
 * <p>
 * Please see AbstractOAuthFilter for additional properties
 * </p>
 *
 * @author Yuriy Movchan
 */
public class AuthenticationFilter extends AbstractOAuthFilter {

    public static final String SESSION_CONVERSATION_KEY = "saml_idp_conversation_key";

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
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {

        if (!preFilter(servletRequest, servletResponse, filterChain)) {
            log.debug("Execute validation filter");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        log.debug("No code and no OAuth data found");

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

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
    protected final boolean preFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {

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

        return true;
    }

    public String getOAuthRedirectUrl(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        String authorizeUrl = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_AUTHORIZE_URL, null);
        String clientScopes = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_SCOPE, null);

        String clientId = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_ID, null);
        String clientSecret = getPropertyFromInitParams(null, Configuration.OAUTH_PROPERTY_CLIENT_PASSWORD, null);
        if (clientSecret != null) {
            try {
                clientSecret = StringEncrypter.defaultInstance().decrypt(clientSecret, Configuration.instance().getCryptoPropertyValue());
            } catch (EncryptionException ex) {
                log.error("Failed to decrypt property: " + Configuration.OAUTH_PROPERTY_CLIENT_PASSWORD, ex);
            }
        }

        String redirectUri = constructRedirectUrl(request);

        List<String> scopes = Arrays.asList(clientScopes.split(StringUtils.SPACE));
        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE);

        String nonce = UUID.randomUUID().toString();
        String rfp = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();

        // Lookup for relying party ID
        final String key = request.getParameter(ExternalAuthentication.CONVERSATION_KEY);
        request.getSession().setAttribute(SESSION_CONVERSATION_KEY, key);
        ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(key, request);

        String relyingPartyId = "";
        final RelyingPartyContext relyingPartyCtx = prc.getSubcontext(RelyingPartyContext.class);
        if (relyingPartyCtx != null) {
            relyingPartyId = relyingPartyCtx.getRelyingPartyId();
            log.info("relyingPartyId found: " + relyingPartyId);
        } else
            log.warn("No RelyingPartyContext was available");

        // JWT
        OxAuthCryptoProvider cryptoProvider = new OxAuthCryptoProvider();
        JwtState jwtState = new JwtState(SignatureAlgorithm.HS256, clientSecret, cryptoProvider);
        jwtState.setRfp(rfp);
        jwtState.setJti(jti);
        if (relyingPartyId != null && !"".equals(relyingPartyId)) {
            String additionalClaims = String.format("{relyingPartyId: '%s'}", relyingPartyId);
            jwtState.setAdditionalClaims(new JSONObject(additionalClaims));
        } else
            log.warn("No relyingPartyId was available");
        String encodedState = jwtState.getEncodedJwt();

        AuthorizationRequest authorizationRequest = new AuthorizationRequest(responseTypes, clientId, scopes, redirectUri, nonce);
        authorizationRequest.setState(encodedState);

        Cookie currentShibstateCookie = getCurrentShibstateCookie(request);
        if (currentShibstateCookie != null) {
            String requestUri = decodeCookieValue(currentShibstateCookie.getValue());
            log.debug("requestUri = \"" + requestUri + "\"");

            String authenticationMode = determineAuthenticationMode(requestUri);

            if (StringHelper.isNotEmpty(authenticationMode)) {
                log.debug("acr_values = \"" + authenticationMode + "\"");
                authorizationRequest.setAcrValues(Arrays.asList(authenticationMode));
                updateShibstateCookie(response, currentShibstateCookie, requestUri, "/" + Configuration.OXAUTH_ACR_VALUES + "/" + authenticationMode);
            }
        }

        // Store for validation in session
        final HttpSession session = request.getSession(false);
        session.setAttribute(Configuration.SESSION_AUTH_STATE, encodedState);
        session.setAttribute(Configuration.SESSION_AUTH_NONCE, nonce);

        return authorizeUrl + "?" + authorizationRequest.getQueryString();
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
