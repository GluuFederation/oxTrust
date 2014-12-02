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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AuthenticationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.AuthenticationSessionService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.AuthenticationException;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.log.Log;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuUserRole;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.oxauth.model.common.Parameters;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Provides authentication using oAuth
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Name("authenticator")
@Scope(ScopeType.SESSION)
public class Authenticator implements Serializable {
	/**
     *
     */
	private static final long serialVersionUID = -3975272457541385597L;

	@Logger
	private Log log;

	@In
	private Identity identity;

	@In
	private AuthenticationService authenticationService;

	@In
	private Credentials credentials;
	
	@In
	Redirect redirect;
	
	@In
	private PersonService personService;

	@In
	private SecurityService securityService;

	@In(create = true)
	private SsoLoginAction ssoLoginAction;

	@In
	private transient ApplianceService applianceService;

	@In
	private FacesMessages facesMessages;

	String viewIdBeforeLoginRedirect;

	@In(create = true)
	@Out(scope = ScopeType.SESSION, required = false)
	private OauthData oauthData;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;	
	
	public boolean preAuthenticate() throws IOException, Exception {
		boolean result = true;
		if (isOxAuthAuth()) {
			if (!identity.isLoggedIn()) {
				result = oAuthLogin();
			}
		} else {
			result = externalAuthenticate();
		}
		
		return result;
	}

	public boolean authenticate() {
		String userName = null;
		try {
			if (isBasicAuth()) {
				userName = identity.getCredentials().getUsername();
				log.info("Authenticating user '{0}'", userName);

				boolean authenticated = false;
				try {
					authenticated = this.authenticationService.authenticate(userName, this.credentials.getPassword());
				} catch (AuthenticationException ex) {
					this.log.error("Failed to authenticate user: '{0}'", ex, userName);
				}
				
				if (!authenticated) {
					return false;
				}
			} else if (isOxAuthAuth()) {
				userName = oauthData.getUserUid();
				identity.getCredentials().setUsername(userName);
				log.info("Authenticating user '{0}'", userName);
			} else {
				return false;
			}

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

	public boolean authenticateBasicWebService() {
		String userName = identity.getCredentials().getUsername();
		log.info("Authenticating user '{0}'", userName);

		boolean authenticated = false;
		try {
			authenticated = this.authenticationService.authenticate(userName, this.credentials.getPassword());
		} catch (AuthenticationException ex) {
			this.log.error("Failed to authenticate user: '{0}'", ex, userName);
		}
		
		if (!authenticated) {
			return false;
		}

		return postAuthenticateWebService(userName);
    }

	public boolean authenticateBearerWebService() {
		String userName = identity.getCredentials().getUsername();
		log.info("Authenticating user '{0}'", userName);

		return postAuthenticateWebService(userName);
    }

	public boolean postAuthenticateWebService(String userName) {
		try {
	        User user = findUserByUserName(userName);
			if (user == null) {
				log.error("Person '{0}' not found in LDAP", userName);
				return false;
			}
	
            Principal principal = new SimplePrincipal(userName);
            identity.acceptExternallyAuthenticatedPrincipal(principal);
            identity.quietLogin();

	        postLogin(user);
			log.info("User '{0}' authenticated successfully", userName);

	        return true;
		} catch (Exception ex) {
			log.error("Failed to authenticate user '{0}'", ex, userName);
		}

		return false;
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
		for (GluuUserRole userRole : userRoles) {
			identity.addRole(userRole.getRoleName());
		}
	}

	private User findUserByUserName(String userName) {
        User user = null;
		try {
			user = authenticationService.getUserByUid(userName);
		} catch (Exception ex) {
			log.error("Failed to find user '{0}' in ldap", ex, userName);
		}
			
		return user;
    }

	private GluuCustomPerson findPersonByDn(String userDn) {
		GluuCustomPerson person = null;
		try {
			person = authenticationService.getPersonByDn(userDn);
		} catch (Exception ex) {
			log.error("Failed to find person '{0}' in ldap", ex, userDn);
		}
			
		return person;
    }

	private boolean isBasicAuth() {
		return Utils.isBasicAuth();
	}

	private boolean isOxAuthAuth() {
		return !isBasicAuth();
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

		ClientRequest clientRequest = new ClientRequest(applicationConfiguration.getOxAuthLogoutUrl());

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_ID_TOKEN_HINT, oauthData.getIdToken());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_POST_LOGOUT_REDIRECT_URI, applicationConfiguration.getLogoutRedirectUrl());

		// Clean up OAuth token
		oauthData.setUserUid(null);
		oauthData.setIdToken(null);
		oauthData.setSessionId(null);
		oauthData = null;

		FacesContext.getCurrentInstance().getExternalContext().redirect(clientRequest.getUri());
	}

	/**
	 * Authenticate using credentials passed from web request header
	 */
	public boolean shibboleth2Authenticate() {
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
			user = authenticationService.getUserByUid(userUid);
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
					OxTrustConstants.APPLICATION_AUTHORIZATION_NAME_SHIBBOLETH2);
	
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
	 * Make attempt to authenticate using parameters passed in header
	 */
	public boolean externalAuthenticate() {
		if (identity.isLoggedIn()) {
			return true;
		}

		if (shibboleth2Authenticate()) {

			return true;
		}
		// try {
		// oAuthLogin();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return false;
	}

	/**
	 * Main entry point for oAuth authentication.
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	public boolean oAuthLogin() throws IOException, Exception {
		ClientRequest clientRequest = new ClientRequest(applicationConfiguration.getOxAuthAuthorizeUrl());
		String clientId = applicationConfiguration.getOxAuthClientId();
		String scope = applicationConfiguration.getOxAuthClientScope();
		String responseType = "code+id_token";

		String nonce = "nonce";

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_REDIRECT_URI, applicationConfiguration.getLoginRedirectUrl());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SCOPE, scope);
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_NONCE, nonce);

		GluuAppliance appliance = applianceService.getAppliance(new String[] {"oxAuthenticationMode", "oxAuthenticationLevel"});
		String authenticationMode = appliance.getAuthenticationMode();
		if (StringHelper.isNotEmpty(authenticationMode)) {
			clientRequest.queryParameter(OxTrustConstants.OXAUTH_AUTH_MODE, authenticationMode);
		} else {
			String authenticationLevel = appliance.getAuthenticationLevel();
			if (StringHelper.isNotEmpty(authenticationLevel)) {
				clientRequest.queryParameter(OxTrustConstants.OXAUTH_AUTH_LEVEL, authenticationLevel);
			}
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
		String oxAuthAuthorizeUrl = applicationConfiguration.getOxAuthAuthorizeUrl();
		String oxAuthHost = getOxAuthHost(oxAuthAuthorizeUrl);
		if (StringHelper.isEmpty(oxAuthHost)) {
			log.info("Failed to determine oxAuth host using oxAuthAuthorizeUrl: '{0}'", oxAuthAuthorizeUrl);
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}

		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();

		String authorizationCode = requestParameterMap.get(OxTrustConstants.OXAUTH_CODE);

		Object sessionIdCookie = requestCookieMap.get(Parameters.SESSION_ID.getParamName());
		String sessionId = null;
		if (sessionIdCookie != null) {
			sessionId = ((Cookie) sessionIdCookie).getValue();
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
				clientPassword = StringEncrypter.defaultInstance().decrypt(clientPassword, cryptoConfiguration.getEncodeSalt());
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt client password", ex);
			}
		}

		log.info("getting accessToken");
		// 1. Request access token using the authorization code.
		log.info("tokenURL : " + applicationConfiguration.getOxAuthTokenUrl());
		String tokenURL = applicationConfiguration.getOxAuthTokenUrl();
		TokenClient tokenClient1 = new TokenClient(tokenURL);

		log.info("Sending request to token endpoint");
		String redirectURL = applicationConfiguration.getLoginRedirectUrl();
		log.info("redirectURI : " + applicationConfiguration.getLoginRedirectUrl());
		TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectURL, clientID, clientPassword);

		log.info(" tokenResponse : " + tokenResponse);
		log.info(" tokenResponse.getErrorType() : " + tokenResponse.getErrorType());

		String accessToken = tokenResponse.getAccessToken();
		log.info(" accessToken : " + accessToken);

		// 2. Validate the access token
		// ValidateTokenClient validateTokenClient = new
		// ValidateTokenClient(applicationConfiguration.getOxAuthValidateTokenUrl());
		// ValidateTokenResponse response3 = validateTokenClient
		// .execValidateToken(accessToken);
		log.info(" validating AccessToken ");

		String validateUrl = applicationConfiguration.getOxAuthTokenValidationUrl();
		ValidateTokenClient validateTokenClient = new ValidateTokenClient(validateUrl);
		ValidateTokenResponse response3 = validateTokenClient.execValidateToken(accessToken);
		log.info(" response3.getStatus() : " + response3.getStatus());

		log.info("validate check session status:" + response3.getStatus());
		if (response3.getErrorDescription() != null) {
			log.debug("validate token status message:" + response3.getErrorDescription());
		}

		if (response3.getStatus() == 200) {
			log.info("Session validation successful. User is logged in");
			String userInfoEndPoint = applicationConfiguration.getOxAuthUserInfo();
			UserInfoClient userInfoClient = new UserInfoClient(userInfoEndPoint);
			UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);

			this.oauthData.setHost(oxAuthHost);
			// Determine uid
			List<String> uidValues = userInfoResponse.getClaims().get(JwtClaimName.USER_NAME);
			if ((uidValues == null) || (uidValues.size() == 0)) {
				log.error("User infor response doesn't contains uid claim");
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}
			
			this.oauthData.setUserUid(uidValues.get(0));
			this.oauthData.setAccessToken(accessToken);
			this.oauthData.setAccessTokenExpirationInSeconds(response3.getExpiresIn());
			this.oauthData.setScopes(scopes);
			this.oauthData.setIdToken(idToken);
			this.oauthData.setSessionId(sessionId);

			log.info("user uid:" + oauthData.getUserUid());
			
			// Create session scope authentication service
			Component.getInstance(AuthenticationSessionService.class);

			return OxTrustConstants.RESULT_SUCCESS;
		}
		log.info("Token validation failed. User is NOT logged in");
		return OxTrustConstants.RESULT_NO_PERMISSIONS;
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
