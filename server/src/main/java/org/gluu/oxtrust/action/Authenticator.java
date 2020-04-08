/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.codehaus.jettison.json.JSONException;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.FacesService;
import org.gluu.model.GluuStatus;
import org.gluu.model.user.UserRole;
import org.gluu.oxauth.client.*;
import org.gluu.oxauth.model.crypto.signature.RSAPrivateKey;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.exception.InvalidJwtException;
import org.gluu.oxauth.model.jws.RSASigner;
import org.gluu.oxauth.model.jwt.*;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.*;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.util.ArrayHelper;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Provides authentication using oAuth
 * 
 * @author Reda Zerrad Date: 05.11.2012
 * @author Yuriy Movchan Date: 02.12.2013
 */
@Named("authenticator")
@SessionScoped
public class Authenticator implements Serializable {

	private static final String LOGIN_FAILED_OX_TRUST = "Login failed, oxTrust wasn't allowed to access user data";

	private static final long serialVersionUID = -3975272457541385597L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

	@Inject
	private FacesService facesService;

	@Inject
	private PersonService personService;

	@Inject
	private SecurityService securityService;

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
	private JsonConfigurationService jsonConfigurationService;

	private org.gluu.oxauth.model.configuration.AppConfiguration oxAuthAppConfiguration;
	private Boolean fapiCompatibility;
	private String locationHash;

	@PostConstruct
	public void init() {
		try {
			oxAuthAppConfiguration = jsonConfigurationService.getOxauthAppConfiguration();
			fapiCompatibility = oxAuthAppConfiguration.getFapiCompatibility();
		} catch (IOException e) {
			log.error("Exceptio on loading oxAuth json configuration", e);
		}
	}

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

	/**
	 * Main entry point for oAuth authentication.
	 *
	 * @throws IOException
	 *
	 * @throws Exception
	 */
	public boolean oAuthLogin() throws IOException, Exception {
		GluuConfiguration configuration = configurationService
				.getConfiguration(new String[] { "oxTrustAuthenticationMode" });
		String acrValues = configuration.getOxTrustAuthenticationMode();
		String nonce = UUID.randomUUID().toString();
		String state = UUID.randomUUID().toString();
		String clientId = appConfiguration.getOxAuthClientId();
		String scope = appConfiguration.getOxAuthClientScope();

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(openIdService.getOpenIdConfiguration().getAuthorizationEndpoint());
		if (this.fapiCompatibility) {
			target = getFapiAuthenticationRequest(target, acrValues, nonce, state, clientId, scope);
		} else {
			target = getNormalAuthenticationRequest(target, clientId, nonce, state, acrValues, scope);
		}
		if (target == null)
			return false;
		// Store state and nonce
		identity.getSessionMap().put(OxTrustConstants.OXAUTH_NONCE, nonce);
		identity.getSessionMap().put(OxTrustConstants.OXAUTH_STATE, state);

		if (StringHelper.isNotEmpty(acrValues)) {
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
		String oxAuthAuthorizeUrl = openIdService.getOpenIdConfiguration().getAuthorizationEndpoint();
		String oxAuthHost = getOxAuthHost(oxAuthAuthorizeUrl);
		if (StringHelper.isEmpty(oxAuthHost)) {
			log.info("Failed to determine oxAuth host using oxAuthAuthorizeUrl: '{}'", oxAuthAuthorizeUrl);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
			return OxTrustConstants.RESULT_NO_PERMISSIONS;
		}
		Map<String, String> requestParameterMap;
		if (this.fapiCompatibility) {
			requestParameterMap = this.getFapiResponseParameters(this.locationHash);
		} else {
			requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
					.getRequestParameterMap();
		}
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
			facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);
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
			facesMessages.add(FacesMessage.SEVERITY_ERROR, LOGIN_FAILED_OX_TRUST);

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
		if (clientPassword != null && !this.fapiCompatibility) {
			try {
				clientPassword = encryptionService.decrypt(clientPassword);
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt client password", ex);
			}
		}

		String result = requestAccessToken(oxAuthHost, authorizationCode, sessionState, scopes, clientID,
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
		ApacheHttpClient4Executor apacheHttpClient4Executor = this.getApacheHttpClient4ExecutorForMTLS();

		// 1. Request access token using the authorization code.
		TokenClient tokenClient1 = new TokenClient(openIdConfiguration.getTokenEndpoint());
		if (this.fapiCompatibility && apacheHttpClient4Executor != null)
			tokenClient1.setExecutor(apacheHttpClient4Executor);

		log.info("Sending request to token endpoint");
		String redirectURL = appConfiguration.getLoginRedirectUrl();
		log.info("redirectURI : " + redirectURL);
		TokenResponse tokenResponse;
		if (this.fapiCompatibility) {
			tokenResponse = tokenClient1.execAuthorizationCodeMTLS(authorizationCode, redirectURL, clientID);
		} else {
			tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectURL, clientID,
					clientPassword);
		}

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
		if (this.fapiCompatibility && apacheHttpClient4Executor != null)
			userInfoClient.setExecutor(apacheHttpClient4Executor);
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

		String result = authenticate();

		return result;
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

	private WebTarget getNormalAuthenticationRequest(WebTarget target, String clientId, String nonce,
													 String state, String acrValues, String scope) {
		String responseType = "code";

		target = target.queryParam(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
		target = target.queryParam(OxTrustConstants.OXAUTH_REDIRECT_URI, appConfiguration.getLoginRedirectUrl());
		target = target.queryParam(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
		target = target.queryParam(OxTrustConstants.OXAUTH_SCOPE, scope);
		target = target.queryParam(OxTrustConstants.OXAUTH_NONCE, nonce);
		target = target.queryParam(OxTrustConstants.OXAUTH_STATE, state);
		if (StringHelper.isNotEmpty(acrValues)) {
			target = target.queryParam(OxTrustConstants.OXAUTH_ACR_VALUES, acrValues);
		}
		return target;
	}

	private WebTarget getFapiAuthenticationRequest(WebTarget target, String acrValues, String nonce,
												   String state, String clientId, String scope) {
		try {
			String responseType = "code id_token";

			Jwt jwt = new Jwt();
			JwtSubClaimObject acr = new JwtSubClaimObject();
			acr.setClaim("value", acrValues);
			acr.setClaim("essential", true);

			JwtSubClaimObject idToken = new JwtSubClaimObject();
			idToken.setClaim("acr", acr.toJsonObject());

			JwtSubClaimObject claims = new JwtSubClaimObject();
			claims.setClaim("id_token", idToken.toJsonObject());

			JwtClaims jwtClaims = new JwtClaims();
			jwtClaims.setClaim("aud", appConfiguration.getOxAuthIssuer());
			jwtClaims.setClaim("scope", StringUtils.replace(scope, "+", " "));
			jwtClaims.setClaim("claims", claims.toJsonObject());
			jwtClaims.setClaim("iss", clientId);
			jwtClaims.setClaim("response_type", responseType);
			jwtClaims.setClaim("redirect_uri", appConfiguration.getLoginRedirectUrl());
			jwtClaims.setClaim("state", state);
			jwtClaims.setClaim("exp", new Date().getTime() / 1000 + 180L);
			jwtClaims.setClaim("nonce", nonce);
			jwtClaims.setClaim("client_id", clientId);

			jwt.setClaims(jwtClaims);

			JwtHeader jwtHeader = new JwtHeader();
			jwtHeader.setAlgorithm(SignatureAlgorithm.PS256);
			jwtHeader.setKeyId(clientId);
			jwt.setHeader(jwtHeader);

			RSAPrivateKey privateKey = new RSAPrivateKey(appConfiguration.getOxAuthClientFapiJWKPrivateKeyModulus(),
					appConfiguration.getOxAuthClientFapiJWKPrivateKeyPrivateExponent());
			RSASigner signer = new RSASigner(SignatureAlgorithm.PS256, privateKey);
			String signing = signer.generateSignature(jwt.getSigningInput());
			jwt.setEncodedSignature(signing);

			String request = jwt.toString();

			log.info("Fapi authorization JWT to process it : {}", jwt);

			target = target.queryParam(OxTrustConstants.OXAUTH_CLIENT_ID, clientId);
			target = target.queryParam(OxTrustConstants.OXAUTH_REDIRECT_URI, appConfiguration.getLoginRedirectUrl());
			target = target.queryParam(OxTrustConstants.OXAUTH_RESPONSE_TYPE, responseType);
			target = target.queryParam(OxTrustConstants.OXAUTH_SCOPE, scope);
			target = target.queryParam(OxTrustConstants.OXAUTH_REQUEST, request);

			return target;
		} catch (SignatureException e) {
			log.error("Problems when signing the JWT to process FAPI authorization", e);
		} catch (InvalidJwtException e) {
			log.error("Problems processing JWT to process FAPI authorization", e);
		}
		return null;
	}

	private Map<String, String> getFapiResponseParameters(String locationHash) {
		locationHash = locationHash.replace("#", "");
		Map<String, String> urlParamsMap = Splitter.on('&').trimResults().withKeyValueSeparator('=').split(locationHash);

		Map<String, String> params = new HashMap<>();
		params.put(OxTrustConstants.OXAUTH_CODE, urlParamsMap.get(OxTrustConstants.OXAUTH_CODE));
		params.put(OxTrustConstants.OXAUTH_STATE, urlParamsMap.get(OxTrustConstants.OXAUTH_STATE));
		params.put(OxTrustConstants.OXAUTH_ERROR, urlParamsMap.get(OxTrustConstants.OXAUTH_ERROR));
		params.put(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION, urlParamsMap.get(OxTrustConstants.OXAUTH_ERROR_DESCRIPTION));
		return params;
	}

	private ApacheHttpClient4Executor getApacheHttpClient4ExecutorForMTLS() {
		if (!this.fapiCompatibility)
			return null;
		try {
			String keyPassphrase = appConfiguration.getOxAuthFapiClientKeystorePassword();
			KeyStore keyStore = KeyStore.getInstance(appConfiguration.getOxAuthFapiClientKeystoreType());
			keyStore.load(new FileInputStream(new File(appConfiguration.getOxAuthFapiClientKeystorePath())),
					keyPassphrase.toCharArray());

			SSLContext sslContext = SSLContexts.custom()
					.loadKeyMaterial(keyStore, keyPassphrase.toCharArray())
					.build();
			SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext,
					new DefaultHostnameVerifier());
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("https", sslConnectionFactory)
					.register("http", new PlainConnectionSocketFactory())
					.build();

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);

			CloseableHttpClient httpClient = HttpClients.custom()
					.setSSLContext(sslContext)
					.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
					.setConnectionManager(cm)
					.build();

			return new ApacheHttpClient4Executor(httpClient);
		} catch (FileNotFoundException e) {
			log.error("Fapi keystore not found in the path: {}", appConfiguration.getOxAuthFapiClientKeystorePath(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Incorrect algorithm used in Fapi keystore: {}", appConfiguration.getOxAuthFapiClientKeystoreType(), e);
		} catch (KeyManagementException e) {
			log.error("Problems processing Fapi keystore", e);
		} catch (CertificateException e) {
			log.error("Error processing Fapi certificate", e);
		} catch (KeyStoreException e) {
			log.error("Error processing Fapi keystore", e);
		} catch (UnrecoverableKeyException e) {
			log.error("Problems getting key from the fapi keystore", e);
		} catch (IOException e) {
			log.error("Error accessing Fapi file", e);
		}
		return null;
	}

	public Boolean getFapiCompatibility() {
		return fapiCompatibility;
	}

	public void setFapiCompatibility(Boolean fapiCompatibility) {
		this.fapiCompatibility = fapiCompatibility;
	}

	public String getLocationHash() {
		return locationHash;
	}

	public void setLocationHash(String locationHash) {
		this.locationHash = locationHash;
	}
}
