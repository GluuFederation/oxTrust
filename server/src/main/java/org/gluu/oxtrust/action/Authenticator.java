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
import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.AuthenticationSessionService;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.annotations.Out;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.log.Log;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuUserRole;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.oxauth.model.exception.InvalidJwtException;
import org.xdi.oxauth.model.jwt.Jwt;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Provides authentication using oAuth
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Named("authenticator")
@Scope(ScopeType.SESSION)
public class Authenticator implements Serializable {

	private static final long serialVersionUID = -3975272457541385597L;

	@Logger
	private Log log;

	@Inject
	private Identity identity;

	@Inject
	private Credentials credentials;
	
	@Inject
	Redirect redirect;
	
	@Inject
	private IPersonService personService;

	@Inject
	private SecurityService securityService;

	@Inject(create = true)
	private SsoLoginAction ssoLoginAction;

	@Inject
	private ApplianceService applianceService;
	
	@Inject
	private OpenIdService openIdService;

	@Inject
	private FacesMessages facesMessages;

	String viewIdBeforeLoginRedirect;

	@Inject(create = true)
	@Out(scope = ScopeType.SESSION, required = false)
	private OauthData oauthData;

	@Inject(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@Inject(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;
	
	public boolean preAuthenticate() throws IOException, Exception {
		boolean result = true;
		if (!identity.isLoggedIn()) {
			result = oAuthLogin();
		}
		
		return result;
	}

	public boolean authenticate() {
		String userName = null;
		try {
			userName = oauthData.getUserUid();
			identity.getCredentials().setUsername(userName);
			log.info("Authenticating user '{0}'", userName);

			User user = findUserByUserName(userName);
			if (user == null) {
				log.error("Person '{0}' not found in LDAP", userName);
				return false;
			}else if(GluuStatus.EXPIRED.getValue().equals(user.getAttribute("gluuStatus")) || GluuStatus.REGISTER.getValue().equals(user.getAttribute("gluuStatus"))){
			     redirect.setViewId("/register.xhtml");
			     redirect.setParameter("inum", user.getInum());
			     redirect.execute();
			     return false;
			}

			postLogin(user);
			log.info("User '{0}' authenticated successfully", userName);
		} catch (Exception ex) {
			log.error("Failed to authenticate user '{0}'", ex, userName);
			return false;
		}

		return true;
	}

	/**
	 * Set session variables after user login
	 * 
	 * @throws Exception
	 */
	private void postLogin(User user) {
		log.debug("Configuring application after user '{0}' login", user.getUid());
		GluuCustomPerson person = findPersonByDn(user.getDn());
		Contexts.getSessionContext().set(OxTrustConstants.CURRENT_PERSON, person);

		// Set user roles
		GluuUserRole[] userRoles = securityService.getUserRoles(user);
		if (ArrayHelper.isNotEmpty(userRoles)) {
			log.debug("Get '{0}' user roles", Arrays.toString(userRoles));
		} else {
			log.debug("Get 0 user roles");
		}
		for (GluuUserRole userRole : userRoles) {
			identity.addRole(userRole.getRoleName());
		}
		
		if (log.isDebugEnabled()) {
			for (Group sg : identity.getSubject().getPrincipals(java.security.acl.Group.class)) {
				if ("Roles".equals(sg.getName())) {
					log.debug("Using next user roles: '{0}'", sg.members());
					break;
				}
			}
		}
	}

	private User findUserByUserName(String userName) {
        User user = null;
		try {
			user = personService.getUserByUid(userName);
		} catch (Exception ex) {
			log.error("Failed to find user '{0}' in ldap", ex, userName);
		}
			
		return user;
    }

	private GluuCustomPerson findPersonByDn(String userDn) {
		GluuCustomPerson person = null;
		try {
			person = personService.getPersonByDn(userDn);
		} catch (Exception ex) {
			log.error("Failed to find person '{0}' in ldap", ex, userDn);
		}
			
		return person;
    }

	public void processLogout() throws Exception {
		ssoLoginAction.logout();
		oAuthlLogout();

		postLogout();
	}

	public String postLogout() {
		if (identity.isLoggedIn()) {
			identity.logout();
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}


	public void oAuthlLogout() throws Exception {
		if (StringHelper.isEmpty(oauthData.getUserUid())) {
			return;
		}

		ClientRequest clientRequest = new ClientRequest(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SESSION_STATE, oauthData.getSessionState());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_ID_TOKEN_HINT, oauthData.getIdToken());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_POST_LOGOUT_REDIRECT_URI, applicationConfiguration.getLogoutRedirectUrl());

		// Clean up OAuth token
		oauthData.setUserUid(null);
		oauthData.setIdToken(null);
		oauthData.setSessionState(null);
		oauthData = null;

		FacesContext.getCurrentInstance().getExternalContext().redirect(clientRequest.getUri());
	}

	/**
	 * Authenticate using credentials passed from web request header
	 */
	public boolean Shibboleth3Authenticate() {
		log.debug("Checking if user authenticated with shibboleth already");
		boolean result = false;
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		String authType = request.getAuthType();
		String userUid = request.getHeader("REMOTE_USER");
		String userUidlower = request.getHeader("remote_user");
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			log.trace(headerName + "-->" + request.getHeader(headerName));
		}
		log.debug("Username is " + userUid);
		log.debug("UsernameLower is " + userUidlower);
		log.debug("AuthType is " + authType);

		Map<String, String[]> headers = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderValuesMap();
		for (String name : headers.keySet()) {
			log.trace(name + "==>" + StringUtils.join(headers.get(name)));
		}

		if (StringHelper.isEmpty(userUid) || StringHelper.isEmpty(authType) || !authType.equals("shibboleth")) {
			result = false;
			return result;
		}

		Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher matcher = pattern.matcher(userUid);

		User user = null;
		if (matcher.matches()) {
			// Find user by uid
			user = personService.getPersonByEmail(userUid);
		} else {
			// Find user by uid
			user = personService.getUserByUid(userUid);
		}

		if (user == null) {
			result = false;
			return result;
		}
		log.debug("Person Inum is " + user.getInum());

		if (GluuStatus.ACTIVE.getValue().equals(user.getAttribute("gluuStatus"))){
		
			credentials.setUsername(user.getUid());
			// credentials.setPassword("");
			Principal principal = new SimplePrincipal(user.getUid());
			log.debug("Principal is " + principal.toString());
	
			identity.acceptExternallyAuthenticatedPrincipal(principal);
	
			log.info("User '{0}' authenticated with shibboleth already", userUid);
			identity.quietLogin();
			postLogin(user);
	
			Contexts.getSessionContext().set(OxTrustConstants.APPLICATION_AUTHORIZATION_TYPE,
					OxTrustConstants.APPLICATION_AUTHORIZATION_NAME_SHIBBOLETH3);
	
			result = true;
			if (Events.exists()) {
				facesMessages.clear();
				Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL);
			}
		}else{
			result = false;
		}
		
		return result;
	}

	/**
	 * Main entry point for oAuth authentication.
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	public boolean oAuthLogin() throws IOException, Exception {
		ClientRequest clientRequest = new ClientRequest(openIdService.getOpenIdConfiguration().getAuthorizationEndpoint());
		String clientId = applicationConfiguration.getOxAuthClientId();
		String scope = applicationConfiguration.getOxAuthClientScope();
		String responseType = "code+id_token";
		String nonce = UUID.randomUUID().toString();

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_REDIRECT_URI, applicationConfiguration.getLoginRedirectUrl());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SCOPE, scope);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_NONCE, nonce);

		GluuAppliance appliance = applianceService.getAppliance(new String[] {"oxTrustAuthenticationMode"});
		String acrValues = appliance.getOxTrustAuthenticationMode();
		if (StringHelper.isNotEmpty(acrValues)) {
			clientRequest.queryParameter(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
			
			// Store request authentication method
			Contexts.getSessionContext().set(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
			Contexts.getSessionContext().set(OxTrustConstants.OXAUTH_NONCE, nonce);
		}

		if (viewIdBeforeLoginRedirect != null) {
			clientRequest.queryParameter(OxTrustConstants.OXAUTH_STATE, viewIdBeforeLoginRedirect);
		}

		FacesContext.getCurrentInstance().getExternalContext().redirect(clientRequest.getUri().replaceAll("%2B", "+"));
		
		return true;
	}

	/**
	 * After successful login, oxAuth will redirect user to this method. Obtains
	 * access token using authorization code and verifies if access token is
	 * valid
	 * 
	 * @return
	 * @throws JSONException
	 */
	public String oAuthGetAccessToken() throws JSONException {
		String oxAuthAuthorizeUrl = openIdService.getOpenIdConfiguration().getAuthorizationEndpoint();
		String oxAuthHost = getOxAuthHost(oxAuthAuthorizeUrl);
		if (StringHelper.isEmpty(oxAuthHost)) {
			log.info("Failed to determine oxAuth host using oxAuthAuthorizeUrl: '{0}'", oxAuthAuthorizeUrl);
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();

		String authorizationCode = requestParameterMap.get(OxTrustConstants.OXAUTH_CODE);

		Object sessionStateCookie = requestCookieMap.get(OxTrustConstants.OXAUTH_SESSION_STATE);
		String sessionState = null;
		if (sessionStateCookie != null) {
			sessionState = ((Cookie) sessionStateCookie).getValue();
		}

		String idToken = requestParameterMap.get(OxTrustConstants.OXAUTH_ID_TOKEN);

		if (authorizationCode == null) {
			String error = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR);
			String errorDescription = requestParameterMap
					.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION);

			log.info("No authorization code sent. Error: " + error + ". Error description: " + errorDescription);
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		if (viewIdBeforeLoginRedirect != null && !viewIdBeforeLoginRedirect.equals("")) {
			Redirect.instance().setViewId(viewIdBeforeLoginRedirect);
			viewIdBeforeLoginRedirect = "";
		}
		// todo hardcoded for now. Once clients are dynamically registered with
		// oxAuth, change this
		// String credentials = applicationConfiguration.getOxAuthClientId() +
		// ":secret";
//		String credentials = applicationConfiguration.getOxAuthClientId() + ":5967d41c-ce9c-4137-9068-42578df0c606";
		// String clientCredentials =
		// applicationConfiguration.getOxAuthClientCredentials();
		log.info("authorizationCode : " + authorizationCode);

		String scopes = requestParameterMap.get(OxTrustConstants.OXAUTH_SCOPE);
		log.info(" scopes : " + scopes);

		String clientID = applicationConfiguration.getOxAuthClientId();
		log.info("clientID : " + clientID);

		String clientPassword = applicationConfiguration.getOxAuthClientPassword();
		if (clientPassword != null) {
			try {
				clientPassword = StringEncrypter.defaultInstance().decrypt(clientPassword, cryptoConfigurationSalt);
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt client password", ex);
			}
		}

		String result = requestAccessToken(oxAuthHost, authorizationCode, sessionState, idToken, scopes, clientID, clientPassword);

		return result;
	}

	private String requestAccessToken(String oxAuthHost, String authorizationCode, String sessionState, String idToken, String scopes,
			String clientID, String clientPassword) {
		OpenIdConfigurationResponse openIdConfiguration = openIdService.getOpenIdConfiguration();

		// 1. Request access token using the authorization code.
		TokenClient tokenClient1 = new TokenClient(openIdConfiguration.getTokenEndpoint());

		log.info("Sending request to token endpoint");
		String redirectURL = applicationConfiguration.getLoginRedirectUrl();
		log.info("redirectURI : " + redirectURL);
		TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectURL, clientID, clientPassword);

		log.debug(" tokenResponse : " + tokenResponse);
		if (tokenResponse == null) {
			log.error("Get empty token response. User rcan't log into application");
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		log.debug(" tokenResponse.getErrorType() : " + tokenResponse.getErrorType());

		String accessToken = tokenResponse.getAccessToken();
		log.debug(" accessToken : " + accessToken);

		log.info("Session validation successful. User is logged in");
		UserInfoClient userInfoClient = new UserInfoClient(openIdConfiguration.getUserInfoEndpoint());
		UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);

		this.oauthData.setHost(oxAuthHost);
		// Determine uid
		List<String> uidValues = userInfoResponse.getClaims().get(JwtClaimName.USER_NAME);
		if ((uidValues == null) || (uidValues.size() == 0)) {
			log.error("User info response doesn't contains uid claim");
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}
		
		// Store request authentication method
		if (Contexts.getSessionContext().isSet(OxTrustConstants.OXAUTH_ACR_VALUES)) {
			String requestAcrValues = (String) Contexts.getSessionContext().get(OxTrustConstants.OXAUTH_ACR_VALUES);
			Jwt jwt;
            try {
				jwt = Jwt.parse(idToken);
			} catch (InvalidJwtException ex) {
				log.error("Failed to parse id_token");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
            
            String issuer = openIdConfiguration.getIssuer();
            String responseIssuer = (String) jwt.getClaims().getClaim(JwtClaimName.ISSUER);
            if (issuer == null ||  responseIssuer == null || !issuer.equals(responseIssuer)) {
				log.error("User info response :  Issuer.");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}

            List<String> acrValues = jwt.getClaims().getClaimAsStringList(JwtClaimName.AUTHENTICATION_CONTEXT_CLASS_REFERENCE);
			if ((acrValues == null) || (acrValues.size() == 0) || !acrValues.contains(requestAcrValues)) {
				log.error("User info response doesn't contains acr claim");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
			if (!acrValues.contains(requestAcrValues)) {
				log.error("User info response contains acr='{0}' claim but expected acr='{1}'", acrValues, requestAcrValues);
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
			
			String nonceResponse = (String) jwt.getClaims().getClaim(JwtClaimName.NONCE);
			String nonceSession = (String) Contexts.getSessionContext().get(OxTrustConstants.OXAUTH_NONCE);
			if (nonceResponse == null ||  nonceSession == null || !nonceSession.equals(nonceResponse)) {
				log.error("User info response :  nonce is not matching.");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}			
		}	

		this.oauthData.setUserUid(uidValues.get(0));
		this.oauthData.setAccessToken(accessToken);
		this.oauthData.setAccessTokenExpirationInSeconds(tokenResponse.getExpiresIn());
		this.oauthData.setScopes(scopes);
		this.oauthData.setIdToken(idToken);
		this.oauthData.setSessionState(sessionState);

		log.info("user uid:" + oauthData.getUserUid());

		// Create session scope authentication service
		Component.getInstance(AuthenticationSessionService.class);

		return OxTrustConstants.RESULT_SUCCESS;
		
	}

	private String getOxAuthHost(String oxAuthAuthorizeUrl) {
		try {
			URL url = new URL(oxAuthAuthorizeUrl);
			return String.format("%s://%s:%s", url.getProtocol(), url.getHost(), url.getPort());
		} catch (MalformedURLException ex) {
			log.error("Invalid oxAuth authorization URI: '{0}'", ex, oxAuthAuthorizeUrl);
		}

		return null;
	}

	/**
	 * Used to remember the view user tried to access prior to login. This is
	 * the view user will be redirected after successful login.
	 */
	public void captureCurrentView() {
		FacesContext context = FacesContext.getCurrentInstance();

		// If this isn't a faces request then just return
		if (context == null)
			return;

		viewIdBeforeLoginRedirect = Pages.getViewId(context);
	}


	public static Authenticator instance() {
		return (Authenticator) Component.getInstance(Authenticator.class, true);
	}

}
