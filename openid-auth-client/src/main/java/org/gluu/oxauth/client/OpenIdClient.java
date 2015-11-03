/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.gluu.oxauth.client.auth.principal.ClientCredential;
import org.gluu.oxauth.client.auth.user.CommonProfile;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.gluu.oxauth.client.conf.AppConfiguration;
import org.gluu.oxauth.client.exception.CommunicationException;
import org.gluu.oxauth.client.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.context.WebContext;
import org.xdi.oxauth.client.AuthorizationRequest;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.RegisterClient;
import org.xdi.oxauth.client.RegisterRequest;
import org.xdi.oxauth.client.RegisterResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.oxauth.model.register.ApplicationType;
import org.xdi.util.StringHelper;
import org.xdi.util.init.Initializable;

/**
 * This class is the oxAuth client to authenticate users and retreview user
 * profile
 * 
 * @author Yuriy Movchan 11/02/2015
 */
public class OpenIdClient extends Initializable implements Client<UserProfile> {

	private final Logger logger = LoggerFactory.getLogger(OpenIdClient.class);

	private static final String STATE_PARAMETER = "#oxauth_state_parameter";

	// Register new client earlier than old client was expired to allow execute authorization requests
	private static final long NEW_CLIENT_EXPIRATION_OVERLAP = 60 * 1000;

	private final ReentrantLock clientLock = new ReentrantLock();

	@NotNull
	private String appName;

	@NotNull
	private String openIdProvider;

	@NotNull
	private List<String> openIdScopes;

	@NotNull
	private String redirectUri;

	private String clientId;
	private String clientSecret;
	private long clientExpiration;

	private OpenIdConfigurationResponse openIdConfiguration;
	private boolean preRegisteredClient;

	public OpenIdClient() {}

	public OpenIdClient(final String appName, final String openIdProvider, final String clientId, final String clientSecret, final List<String> openIdScopes, final String redirectUri) {
		this.appName = appName;
		this.openIdProvider = openIdProvider;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.openIdScopes = openIdScopes;
		this.redirectUri = redirectUri;
	}

	public OpenIdClient(final AppConfiguration appConfiguration) {
		this(appConfiguration.getApplicationName(), appConfiguration.getOpenIdProviderUrl(),
				appConfiguration.getOpenIdClientId(), appConfiguration.getOpenIdClientPassword(),
				appConfiguration.getOpenIdScopes(), appConfiguration.getOpenIdRedirectUrl());
	}

	@Override
	public void init() {
		super.init();
		initClient();
	}

	protected void initInternal() {
		loadOpenIdConfiguration();
		this.preRegisteredClient = StringHelper.isNotEmpty(this.clientId) && StringHelper.isNotEmpty(this.clientSecret);
	}

	private void loadOpenIdConfiguration() {
		if (StringHelper.isEmpty(this.openIdProvider)) {
			throw new ConfigurationException("OpenIdProvider Url is invalid");
		}

		final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(this.openIdProvider);
		final OpenIdConfigurationResponse response = openIdConfigurationClient.execOpenIdConfiguration();
		if ((response == null) || (response.getStatus() != 200)) {
			throw new ConfigurationException("Failed to load oxAuth configuration");
		}

		logger.info("Successfully loaded oxAuth configuration");

		this.openIdConfiguration = response;
	}

	private void initClient() {
		if (this.preRegisteredClient) {
			return;
		}

		long now = System.currentTimeMillis();

		// Register new client if the previous one is missing or expired
		if (!isValidClient(now)) {
			clientLock.lock();
			try {
				now = System.currentTimeMillis();
				if (!isValidClient(now)) {
					RegisterResponse clientRegisterResponse = registerOpenIdClient();

					this.clientId = clientRegisterResponse.getClientId();
					this.clientSecret = clientRegisterResponse.getClientSecret();
					this.clientExpiration = clientRegisterResponse.getClientSecretExpiresAt().getTime();
				}
			} finally {
				clientLock.unlock();
			}
		}
	}

	private boolean isValidClient(final long now) {
		if (StringHelper.isEmpty(this.clientId) || StringHelper.isEmpty(this.clientSecret) || (this.clientExpiration - NEW_CLIENT_EXPIRATION_OVERLAP <= now)) {
			return false;
		}

		return true;
	}

	private RegisterResponse registerOpenIdClient() {
		String clientName = this.appName + " client";
		RegisterRequest registerRequest = new RegisterRequest(ApplicationType.WEB, clientName, Arrays.asList(this.redirectUri));
		registerRequest.setRequestObjectSigningAlg(SignatureAlgorithm.RS256);
		registerRequest.setTokenEndpointAuthMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

		RegisterClient registerClient = new RegisterClient(openIdConfiguration.getRegistrationEndpoint());
		registerClient.setRequest(registerRequest);
		RegisterResponse response = registerClient.exec();

		if ((response == null) || (response.getStatus() != 200)) {
			throw new ConfigurationException("Failed to register new client");
		}

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRedirectionUrl(final WebContext context) {
		init();

		final String state = RandomStringUtils.randomAlphanumeric(10);
		final String nonce = RandomStringUtils.randomAlphanumeric(10);

		final AuthorizationRequest authorizationRequest = new AuthorizationRequest(Arrays.asList(ResponseType.CODE), this.clientId, this.openIdScopes,
				this.redirectUri, null);

		authorizationRequest.setState(state);
		authorizationRequest.setNonce(nonce);

		context.setSessionAttribute(getName() + STATE_PARAMETER, state);

		final String redirectionUrl = this.openIdConfiguration.getAuthorizationEndpoint() + "?" + authorizationRequest.getQueryString();
		logger.debug("oxAuth redirection Url: '{}'", redirectionUrl);

		return redirectionUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAuthorizationResponse(final WebContext context) {
		final String authorizationCode = context.getRequestParameter(ResponseType.CODE.getValue());
		logger.debug("oxAuth authorization code: '{}'", authorizationCode);

		final boolean result = StringHelper.isNotEmpty(authorizationCode);
		logger.debug("Is authorization request: '{}'", result);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValidRequestState(final WebContext context) {
		final String state = context.getRequestParameter("state");
		logger.debug("oxAuth request state: '{}'", state);

		final Object sessionState = context.getSessionAttribute(getName() + STATE_PARAMETER);
		logger.debug("Session context state: '{}'", sessionState);

		final boolean emptySessionState = StringHelper.isEmptyString(sessionState);
		if (emptySessionState) {
			return false;
		}

		final boolean result = StringHelper.equals(state, (String) sessionState);
		logger.debug("Is valid state: '{}'", result);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ClientCredential getCredentials(final WebContext context) {
		final String authorizationCode = context.getRequestParameter(ResponseType.CODE.getValue());

		final ClientCredential clientCredential = new ClientCredential(authorizationCode);
		logger.debug("Client credential: '{}'", clientCredential);

		return clientCredential;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserProfile getUserProfile(final ClientCredential credential, final WebContext context) {
		init();

		try {
			final String accessToken = getAccessToken(credential);
			final UserInfoResponse userInfoResponse = getUserInfo(accessToken);

			final UserProfile profile = retrieveUserProfileFromUserInfoResponse(userInfoResponse);
			logger.debug("User profile: '{}'", profile);

			return profile;
		} catch (final Exception ex) {
			throw new CommunicationException(ex);
		}
	}

	private String getAccessToken(final ClientCredential credential) {
		// Request access token using the authorization code
		logger.debug("Getting access token");

		final TokenClient tokenClient = new TokenClient(this.openIdConfiguration.getTokenEndpoint());

		final TokenResponse tokenResponse = tokenClient.execAuthorizationCode(credential.getAuthorizationCode(), this.redirectUri, this.clientId, this.clientSecret);
		logger.trace("tokenResponse.getStatus(): '{}'", tokenResponse.getStatus());
		logger.trace("tokenResponse.getErrorType(): '{}'", tokenResponse.getErrorType());

		final String accessToken = tokenResponse.getAccessToken();
		logger.trace("accessToken : " + accessToken);

		// Validate the access token
		logger.debug("Validating access token");
		ValidateTokenClient validateTokenClient = new ValidateTokenClient(this.openIdConfiguration.getValidateTokenEndpoint());

		final ValidateTokenResponse tokenValidationResponse = validateTokenClient.execValidateToken(accessToken);
		logger.trace("tokenValidationResponse.getStatus(): '{}'", tokenValidationResponse.getStatus());
		logger.trace("tokenValidationResponse.getErrorType(): '{}'", tokenValidationResponse.getErrorType());

		return accessToken;
	}

	private UserInfoResponse getUserInfo(final String accessToken) {
		logger.debug("Session validation successful. Getting user information");

		final UserInfoClient userInfoClient = new UserInfoClient(this.openIdConfiguration.getUserInfoEndpoint());
		final UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);

		logger.trace("userInfoResponse.getStatus(): '{}'", userInfoResponse.getStatus());
		logger.trace("userInfoResponse.getErrorType(): '{}'", userInfoResponse.getErrorType());

		return userInfoResponse;
	}

	private CommonProfile retrieveUserProfileFromUserInfoResponse(final UserInfoResponse userInfoResponse) {
		final CommonProfile profile = new CommonProfile();

		String id = getFirstClaim(userInfoResponse, JwtClaimName.USER_NAME);
		if (StringHelper.isEmpty(id)) {
			id = getFirstClaim(userInfoResponse, JwtClaimName.SUBJECT_IDENTIFIER);
		}
		profile.setId(id);
		profile.setUserName(id);

		profile.setEmail(getFirstClaim(userInfoResponse, JwtClaimName.EMAIL));

		profile.setDisplayName(getFirstClaim(userInfoResponse, JwtClaimName.NAME));
		profile.setFirstName(getFirstClaim(userInfoResponse, JwtClaimName.GIVEN_NAME));
		profile.setFamilyName(getFirstClaim(userInfoResponse, JwtClaimName.FAMILY_NAME));
		profile.setZone(getFirstClaim(userInfoResponse, JwtClaimName.ZONEINFO));
		profile.setLocale(getFirstClaim(userInfoResponse, JwtClaimName.LOCALE));

		return profile;
	}

	private String getFirstClaim(final UserInfoResponse userInfoResponse, final String claimName) {
		final List<String> claims = userInfoResponse.getClaim(claimName);

		if ((claims == null) || claims.isEmpty()) {
			return null;
		}

		return claims.get(0);
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getOpenIdProvider() {
		return openIdProvider;
	}

	public void setOpenIdProvider(String openIdProvider) {
		this.openIdProvider = openIdProvider;
	}

	public List<String> getOpenIdScopes() {
		return openIdScopes;
	}

	public void setOpenIdScopes(List<String> openIdScopes) {
		this.openIdScopes = openIdScopes;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

}
