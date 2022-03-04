/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.codehaus.jettison.json.JSONException;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.FacesService;
import org.gluu.model.GluuStatus;
import org.gluu.model.user.UserRole;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.client.TokenClient;
import org.gluu.oxauth.client.TokenResponse;
import org.gluu.oxauth.client.UserInfoClient;
import org.gluu.oxauth.client.UserInfoResponse;
import org.gluu.oxauth.model.exception.InvalidJwtException;
import org.gluu.oxauth.model.jwt.Jwt;
import org.gluu.oxauth.model.jwt.JwtClaimName;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.SecurityService;
import org.gluu.oxtrust.util.CloudEditionUtil;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.custom.CustomScriptService;
import org.gluu.util.ArrayHelper;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

/**
 * Provides authentication using oAuth
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Named("authenticator")
@RequestScoped
public class Authenticator implements Serializable {

    /**
     * 
     */
    private String LOGIN_FAILED_OX_TRUST = "Login failed, oxTrust wasn't allowed to access user data";

    private static final long serialVersionUID = -3975272457541385597L;

    @Inject
    private Logger log;

    @Inject
    private Identity identity;
    
    @Inject
    private ExternalContext externalContext;

    @Inject
    private FacesService facesService;

    @Inject
    private PersonService personService;

    @Inject
    private SecurityService securityService;

    @Inject
    private CustomScriptService customScriptService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private OpenIdService openIdService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private EncryptionService encryptionService;

    @Inject
    private Instance<Identity> identityInstance;

    public boolean preAuthenticate() throws IOException, Exception {
        boolean result = true;
        if (!identity.isLoggedIn()) {
            result = oAuthLogin();
        }

        return result;
    }

    protected String authenticate() {
        String userName = null;
        try {
            userName = identity.getOauthData().getUserUid();
            String idToken = identity.getOauthData().getIdToken();

            if (StringHelper.isEmpty(userName) || StringHelper.isEmpty(idToken)) {
                log.error("User is not authenticated");
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            }

            identity.getCredentials().setUsername(userName);
            log.info("Authenticating user '{}'", userName);

            User user = findUserByUserName(userName);
            if (user == null) {
                log.error("Person '{}' not found in LDAP", userName);
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            } else if (GluuStatus.EXPIRED.getValue().equals(user.getAttribute("gluuStatus"))
                    || GluuStatus.REGISTER.getValue().equals(user.getAttribute("gluuStatus"))) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("inum", user.getInum());
                facesService.redirect("/register.xhtml", params);
                return OxTrustConstants.RESULT_REGISTER;
            }

            postLogin(user);
            log.info("User '{}' authenticated successfully", userName);

            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception ex) {
            log.error("Failed to authenticate user '{}'", userName, ex);
        }

        return OxTrustConstants.RESULT_NO_PERMISSIONS;
    }

    /**
     * Set session variables after user login
     * 
     * @throws Exception
     */
    private void postLogin(User user) {
    	// At the end of this method execution new session and identity objects
    	// should properly create and initialized
    	
    	// Destroy current session and session objects
    	externalContext.invalidateSession();
    	
    	// Force to create new session
    	externalContext.getSession(true);
    	
        // Force to create new identity bean
        identityInstance.destroy(identityInstance.get());

        // After session end we should get new identity object
    	Identity newSessionIdentity = identityInstance.get();
        
    	log.debug("Old identity hash code '{}', new identity hash code '{}'", System.identityHashCode(identity), System.identityHashCode(newSessionIdentity));
    	
    	// We need to copy oauthData/user/sessionMap object from old identity to newSessionIdentity
    	// Additonal code here
        
    	newSessionIdentity.login();
        log.debug("Configuring application after user '{}' login", user.getUid());
        GluuCustomPerson person = findPersonByDn(user.getDn());
        identity.setUser(person);
        
        // Set user roles
        UserRole[] userRoles = securityService.getUserRoles(user);
        if (ArrayHelper.isNotEmpty(userRoles)) {
            log.debug("Get '{}' user roles", Arrays.toString(userRoles));
        } else {
            log.debug("Get 0 user roles");
        }
        for (UserRole userRole : userRoles) {
            identity.addRole(userRole.getRoleName());
        }
    }

    private User findUserByUserName(String userName) {
        User user = null;
        try {
            user = personService.getUserByUid(userName);
        } catch (Exception ex) {
            log.error("Failed to find user '{}' in ldap", userName, ex);
        }

        return user;
    }

    private GluuCustomPerson findPersonByDn(String userDn) {
        GluuCustomPerson person = null;
        try {
            person = personService.getPersonByDn(userDn);
        } catch (Exception ex) {
            log.error("Failed to find person '{}' in ldap", userDn, ex);
        }

        return person;
    }

    /**
     * Main entry point for oAuth authentication.
     * 
     * @throws IOException
     * 
     * @throws Exception
     */
    public boolean oAuthLogin() throws IOException, Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(getAuthorizationUrl());
        String clientId = appConfiguration.getOxAuthClientId();
        String scope = appConfiguration.getOxAuthClientScope();
        String responseType = "code";
        String nonce = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();
        target = target.queryParam(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
        target = target.queryParam(OxTrustConstants.OXAUTH_REDIRECT_URI, appConfiguration.getLoginRedirectUrl());
        target = target.queryParam(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
        target = target.queryParam(OxTrustConstants.OXAUTH_SCOPE, scope);
        target = target.queryParam(OxTrustConstants.OXAUTH_NONCE, nonce);
        target = target.queryParam(OxTrustConstants.OXAUTH_STATE, state);
        // Store state and nonce
        identity.getSessionMap().put(OxTrustConstants.OXAUTH_NONCE, nonce);
        identity.getSessionMap().put(OxTrustConstants.OXAUTH_STATE, state);
        GluuConfiguration configuration = configurationService
                .getConfiguration(new String[] { "oxTrustAuthenticationMode" });
        String acrValues = configuration.getOxTrustAuthenticationMode();
        if (StringHelper.isNotEmpty(acrValues)) {
            target = target.queryParam(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
            // Store authentication method
            identity.getSessionMap().put(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
        }
        facesService.redirectToExternalURL(target.getUri().toString().replaceAll("%2B", "+"));
        return true;
    }

    /**
     * After successful login, oxAuth will redirect user to this method. Obtains
     * access token using authorization code and verifies if access token is valid
     * 
     * @return
     * @throws JSONException
     */
    public String oAuthGetAccessToken() throws JSONException {
        String oxAuthAuthorizeUrl = getAuthorizationUrl();
        String oxAuthHost = getOxAuthHost(oxAuthAuthorizeUrl);
        if (StringHelper.isEmpty(oxAuthHost)) {
            log.info("Failed to determine oxAuth host using oxAuthAuthorizeUrl: '{}'", oxAuthAuthorizeUrl);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestCookieMap();
        String authorizationCode = requestParameterMap.get(OxTrustConstants.OXAUTH_CODE);
        // Check state
        if (!StringHelper.equals((String) identity.getSessionMap().get(OxTrustConstants.OXAUTH_STATE),
                requestParameterMap.get(OxTrustConstants.OXAUTH_STATE))) {
            String error = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR);
            String errorDescription = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION);
            log.error("No state sent. Error: " + error + ". Error description: " + errorDescription);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        Object sessionStateCookie = requestCookieMap.get(OxTrustConstants.OXAUTH_SESSION_STATE);
        String sessionState = null;
        if (sessionStateCookie != null) {
            sessionState = ((Cookie) sessionStateCookie).getValue();
        }
        if (authorizationCode == null) {
            log.error("No authorization code sent. Error: " + requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR)
                    + ". Error description: " + requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION));
            facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        String clientPassword = appConfiguration.getOxAuthClientPassword();
        if (clientPassword != null) {
            try {
                clientPassword = encryptionService.decrypt(clientPassword);
            } catch (EncryptionException ex) {
                log.error("Failed to decrypt client password", ex);
            }
        }
        String result = requestAccessToken(oxAuthHost, authorizationCode, sessionState,
                requestParameterMap.get(OxTrustConstants.OXAUTH_SCOPE), appConfiguration.getOxAuthClientId(),
                clientPassword);
        if (OxTrustConstants.RESULT_NO_PERMISSIONS.equals(result)) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
        } else if (OxTrustConstants.RESULT_FAILURE.equals(result)) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed");
        }
        return result;
    }

    private String requestAccessToken(String oxAuthHost, String authorizationCode, String sessionState, String scopes,
            String clientID, String clientPassword) {
        OpenIdConfigurationResponse openIdConfiguration = openIdService.getOpenIdConfiguration();
        // 1. Request access token using the authorization code.
        TokenClient tokenClient1 = new TokenClient(openIdConfiguration.getTokenEndpoint());
        TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode,
                appConfiguration.getLoginRedirectUrl(), clientID, clientPassword);
        log.debug(" tokenResponse : " + tokenResponse);
        if (tokenResponse == null) {
            log.error("Get empty token response. User rcan't log into application");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        log.debug(" tokenResponse.getErrorType() : " + tokenResponse.getErrorType());
        String accessToken = tokenResponse.getAccessToken();
        log.debug(" accessToken : " + accessToken);
        String idToken = tokenResponse.getIdToken();
        log.debug(" idToken : " + idToken);
        if (idToken == null) {
            log.error("Failed to get id_token");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        log.info("Session validation successful. User is logged in");
        UserInfoClient userInfoClient = new UserInfoClient(openIdConfiguration.getUserInfoEndpoint());
        UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);
        if (userInfoResponse == null) {
            log.error("Get empty token response. User can't log into application");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

        // Parse JWT
        Jwt jwt;
        try {
            jwt = Jwt.parse(idToken);
        } catch (InvalidJwtException ex) {
            log.error("Failed to parse id_token");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

        // Check nonce
        if (!StringHelper.equals((String) identity.getSessionMap().get(OxTrustConstants.OXAUTH_NONCE),
                (String) jwt.getClaims().getClaim(JwtClaimName.NONCE))) {
            log.error("User info response :  nonce is not matching.");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

        // Determine uid
        List<String> uidValues = userInfoResponse.getClaims().get(JwtClaimName.USER_NAME);
        if ((uidValues == null) || (uidValues.size() == 0)) {
            log.error("User info response doesn't contains uid claim");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }
        // Check requested authentication method
        if (identity.getSessionMap().containsKey(OxTrustConstants.OXAUTH_ACR_VALUES)) {
            String requestAcrValues = (String) identity.getSessionMap().get(OxTrustConstants.OXAUTH_ACR_VALUES);
            String issuer = openIdConfiguration.getIssuer();
            String responseIssuer = (String) jwt.getClaims().getClaim(JwtClaimName.ISSUER);
            if (issuer == null || responseIssuer == null || !issuer.equals(responseIssuer)) {
                log.error("User info response :  Issuer.");
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            }
            List<String> acrLevels = jwt.getClaims()
                    .getClaimAsStringList(JwtClaimName.AUTHENTICATION_METHOD_REFERENCES);
            if ((acrLevels == null) || (acrLevels.size() == 0)) {
                log.error("User info response doesn't contains acr claim");
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            }
            int currentAcrLevel = 0;
            if (requestAcrValues.equalsIgnoreCase(OxTrustConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME)) {
                currentAcrLevel = -1;
            } else {
                currentAcrLevel = customScriptService
                        .getScriptLevel(customScriptService.getScriptByDisplayName(requestAcrValues));
            }
            if (currentAcrLevel > Integer.valueOf(acrLevels.get(0))) {
                log.error("User info response doesn't contains acr claim");
                return OxTrustConstants.RESULT_NO_PERMISSIONS;
            }
        }
        OauthData oauthData = identity.getOauthData();
        oauthData.setHost(oxAuthHost);
        oauthData.setUserUid(uidValues.get(0));
        oauthData.setAccessToken(accessToken);
        oauthData.setAccessTokenExpirationInSeconds(tokenResponse.getExpiresIn());
        oauthData.setScopes(scopes);
        oauthData.setIdToken(idToken);
        oauthData.setSessionState(sessionState);
        identity.setWorkingParameter(OxTrustConstants.OXAUTH_SSO_SESSION_STATE, Boolean.FALSE);
        log.info("user uid:" + oauthData.getUserUid());
        return authenticate();
    }

    private String getOxAuthHost(String oxAuthAuthorizeUrl) {
        try {
            URL url = new URL(oxAuthAuthorizeUrl);
            return String.format("%s://%s:%s", url.getProtocol(), url.getHost(), url.getPort());
        } catch (MalformedURLException ex) {
            log.error("Invalid oxAuth authorization URI: '{}'", oxAuthAuthorizeUrl, ex);
        }
        return null;
    }

    private String getAuthorizationUrl() {
        try {
            URL url = new URL(openIdService.getOpenIdConfiguration().getAuthorizationEndpoint());
            if (CloudEditionUtil.getOxAuthHost().isPresent()) {
                url = CloudEditionUtil.getOxAuthUrl(url,CloudEditionUtil.getOxAuthHost().get());
            }
            return url.toString();
        } catch (MalformedURLException e) {
            log.error("Error reading authz endpoint", e);
            return openIdService.getOpenIdConfiguration().getAuthorizationEndpoint();
        }
    }



}
