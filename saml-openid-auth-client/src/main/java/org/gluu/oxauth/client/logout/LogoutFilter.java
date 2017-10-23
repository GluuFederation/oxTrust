/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxauth.client.logout;

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

import org.codehaus.jettison.json.JSONObject;
import org.gluu.oxauth.client.session.AbstractOAuthFilter;
import org.gluu.oxauth.client.session.OAuthData;
import org.gluu.oxauth.client.session.SignOutHandler;
import org.gluu.oxauth.client.util.Configuration;
import org.opensaml.profile.context.ProfileRequestContext;
import org.xdi.oxauth.client.AuthorizationRequest;
import org.xdi.oxauth.client.model.JwtState;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.oxauth.model.crypto.OxAuthCryptoProvider;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.xdi.oxauth.model.util.StringUtils;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Filter implementation to intercept all requests and attempt to logout
 * the client by redirecting them to OAuth.
 * 
 * @author Dmitry Ognyannikov
 */
public class LogoutFilter extends AbstractOAuthFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
	final HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        HttpSession session = request.getSession(false);
        
        String urlToRedirectTo;
        try {
            SignOutHandler signOutHandler = SignOutHandler.instance();
            urlToRedirectTo = signOutHandler.getOAuthLogoutUrl(request);
            if (urlToRedirectTo == null) {
                urlToRedirectTo = signOutHandler.constructRedirectUrl(request);
            }
        } catch (Exception ex) {
            log.error("Failed to preapre request to OAuth server", ex);
            if (session != null) {
                // clear session
                session.invalidate();
            }
            return;
        }
        
        if (session != null) {
            // clear session
            session.invalidate();
        }
        
        log.debug("Redirecting to \"" + urlToRedirectTo + "\"");
        response.sendRedirect(urlToRedirectTo);
    }
    
}
