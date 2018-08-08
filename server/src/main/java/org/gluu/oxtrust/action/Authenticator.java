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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.FacesService;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.resteasy.client.ClientRequest;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.model.security.Credentials;
import org.xdi.model.security.SimplePrincipal;
import org.xdi.model.user.UserRole;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.model.exception.InvalidJwtException;
import org.xdi.oxauth.model.jwt.Jwt;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Provides authentication using oAuth
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Named("authenticator")
@SessionScoped
public class Authenticator implements Serializable {

	private static final long serialVersionUID = -3975272457541385597L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

	@Inject
	private Credentials credentials;

	@Inject
	private FacesService facesService;

	@Inject
	private IPersonService personService;

	@Inject
	private SecurityService securityService;

	@Inject
	private SsoLoginAction ssoLoginAction;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private OpenIdService openIdService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private EncryptionService encryptionService;

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
			userName = identity.getOauthData().getUserUid();
			identity.getCredentials().setUsername(userName);
			log.info("Authenticating user '{}'", userName);

			User user = findUserByUserName(userName);
			if (user == null) {
				log.error("Person '{}' not found in LDAP", userName);
				return false;
			} else if (GluuStatus.EXPIRED.getValue().equals(user.getAttribute("gluuStatus"))
					|| GluuStatus.REGISTER.getValue().equals(user.getAttribute("gluuStatus"))) {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("inum", user.getInum());
				facesService.redirect("/register.xhtml", params);
				return false;
			}

			postLogin(user);
			log.info("User '{}' authenticated successfully", userName);
		} catch (Exception ex) {
			log.error("Failed to authenticate user '{}'", userName, ex);
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
		identity.login();
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
		OauthData oauthData = identity.getOauthData();
		if (StringHelper.isEmpty(oauthData.getUserUid())) {
			return;
		}

		ClientRequest clientRequest = new ClientRequest(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SESSION_STATE, oauthData.getSessionState());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_ID_TOKEN_HINT, oauthData.getIdToken());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_POST_LOGOUT_REDIRECT_URI,
				appConfiguration.getLogoutRedirectUrl());

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
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();

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

		Map<String, String[]> headers = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestHeaderValuesMap();
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

		if (GluuStatus.ACTIVE.getValue().equals(user.getAttribute("gluuStatus"))) {

			credentials.setUsername(user.getUid());
			// credentials.setPassword("");
			Principal principal = new SimplePrincipal(user.getUid());
			log.debug("Principal is " + principal.toString());

			identity.acceptExternallyAuthenticatedPrincipal(principal);

			log.info("User '{}' authenticated with shibboleth already", userUid);
			identity.quietLogin();
			postLogin(user);

			identity.getSessionMap().put(OxTrustConstants.APPLICATION_AUTHORIZATION_TYPE,
					OxTrustConstants.APPLICATION_AUTHORIZATION_NAME_SHIBBOLETH3);

			result = true;
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * Main entry point for oAuth authentication.
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 */
	public boolean oAuthLogin() throws IOException, Exception {
		ClientRequest clientRequest = new ClientRequest(
				openIdService.getOpenIdConfiguration().getAuthorizationEndpoint());
		String clientId = appConfiguration.getOxAuthClientId();
		String scope = appConfiguration.getOxAuthClientScope();
		String responseType = "code";
		String nonce = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_REDIRECT_URI, appConfiguration.getLoginRedirectUrl());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SCOPE, scope);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_NONCE, nonce);
        clientRequest.queryParameter(OxTrustConstants.OXAUTH_STATE, state);

        // Store state and nonce
        identity.getSessionMap().put(OxTrustConstants.OXAUTH_NONCE, nonce);
        identity.getSessionMap().put(OxTrustConstants.OXAUTH_STATE, state);

        GluuAppliance appliance = applianceService.getAppliance(new String[] { "oxTrustAuthenticationMode" });
		String acrValues = appliance.getOxTrustAuthenticationMode();
		if (StringHelper.isNotEmpty(acrValues)) {
			clientRequest.queryParameter(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);

			// Store authentication method
			identity.getSessionMap().put(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
		}

		facesService.redirectToExternalURL(clientRequest.getUri().replaceAll("%2B", "+"));

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
			log.info("Failed to determine oxAuth host using oxAuthAuthorizeUrl: '{}'", oxAuthAuthorizeUrl);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed, oxTrust wasn't allow to access user data");
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap();
		Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestCookieMap();

		String authorizationCode = requestParameterMap.get(OxTrustConstants.OXAUTH_CODE);

		// Check state
        String authorizationState = requestParameterMap.get(OxTrustConstants.OXAUTH_STATE);
        String stateSession = (String) identity.getSessionMap().get(OxTrustConstants.OXAUTH_STATE);
        if (!StringHelper.equals(stateSession, authorizationState)) {
            String error = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR);
            String errorDescription = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION);
            log.error("No state sent. Error: " + error + ". Error description: " + errorDescription);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed, oxTrust wasn't allow to access user data");

            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

		Object sessionStateCookie = requestCookieMap.get(OxTrustConstants.OXAUTH_SESSION_STATE);
		String sessionState = null;
		if (sessionStateCookie != null) {
			sessionState = ((Cookie) sessionStateCookie).getValue();
		}

		if (authorizationCode == null) {
			String error = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR);
			String errorDescription = requestParameterMap.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION);

			log.error("No authorization code sent. Error: " + error + ". Error description: " + errorDescription);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed, oxTrust wasn't allow to access user data");

			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		// todo hardcoded for now. Once clients are dynamically registered with
		// oxAuth, change this
		// String credentials = appConfiguration.getOxAuthClientId() +
		// ":secret";
		// String credentials = appConfiguration.getOxAuthClientId() +
		// ":5967d41c-ce9c-4137-9068-42578df0c606";
		// String clientCredentials =
		// appConfiguration.getOxAuthClientCredentials();
		log.info("authorizationCode : " + authorizationCode);

		String scopes = requestParameterMap.get(OxTrustConstants.OXAUTH_SCOPE);
		log.info(" scopes : " + scopes);

		String clientID = appConfiguration.getOxAuthClientId();
		log.info("clientID : " + clientID);

		String clientPassword = appConfiguration.getOxAuthClientPassword();
		if (clientPassword != null) {
			try {
				clientPassword = encryptionService.decrypt(clientPassword);
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt client password", ex);
			}
		}

		String result = requestAccessToken(oxAuthHost, authorizationCode, sessionState, scopes, clientID,
				clientPassword);
		
		if (OxTrustConstants.RESULT_NO_PERMISSIONS.equals(result)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed, oxTrust wasn't allow to access user data");
		} else if (OxTrustConstants.RESULT_FAILURE.equals(result)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Login failed");
		}

		return result;
	}

	private String requestAccessToken(String oxAuthHost, String authorizationCode, String sessionState,
			String scopes, String clientID, String clientPassword) {
		OpenIdConfigurationResponse openIdConfiguration = openIdService.getOpenIdConfiguration();
		// 1. Request access token using the authorization code.
		TokenClient tokenClient1 = new TokenClient(openIdConfiguration.getTokenEndpoint());

		log.info("Sending request to token endpoint");
		String redirectURL = appConfiguration.getLoginRedirectUrl();
		log.info("redirectURI : " + redirectURL);
		TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectURL, clientID,
				clientPassword);

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

		log.info("Session validation successful. User is logged in");
		UserInfoClient userInfoClient = new UserInfoClient(openIdConfiguration.getUserInfoEndpoint());
		UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);
        if (userInfoResponse == null) {
            log.error("Get empty token response. User can't log into application");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

		OauthData oauthData = identity.getOauthData();

		oauthData.setHost(oxAuthHost);

		// Parse JWT
		Jwt jwt;
        try {
            jwt = Jwt.parse(idToken);
        } catch (InvalidJwtException ex) {
            log.error("Failed to parse id_token");
            return OxTrustConstants.RESULT_NO_PERMISSIONS;
        }

        // Check nonce
        String nonceResponse = (String) jwt.getClaims().getClaim(JwtClaimName.NONCE);
        String nonceSession = (String) identity.getSessionMap().get(OxTrustConstants.OXAUTH_NONCE);
        if (!StringHelper.equals(nonceSession, nonceResponse)) {
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

			List<String> acrValues = jwt.getClaims()
					.getClaimAsStringList(JwtClaimName.AUTHENTICATION_CONTEXT_CLASS_REFERENCE);
			if ((acrValues == null) || (acrValues.size() == 0) || !acrValues.contains(requestAcrValues)) {
				log.error("User info response doesn't contains acr claim");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
			if (!acrValues.contains(requestAcrValues)) {
				log.error("User info response contains acr='{}' claim but expected acr='{}'", acrValues,
						requestAcrValues);
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
		}

		oauthData.setUserUid(uidValues.get(0));
		oauthData.setAccessToken(accessToken);
		oauthData.setAccessTokenExpirationInSeconds(tokenResponse.getExpiresIn());
		oauthData.setScopes(scopes);
		oauthData.setIdToken(idToken);
		oauthData.setSessionState(sessionState);

		log.info("user uid:" + oauthData.getUserUid());

		return OxTrustConstants.RESULT_SUCCESS;

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

}
