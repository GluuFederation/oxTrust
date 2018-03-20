/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.RandomStringUtils;
import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.gluu.oxauth.client.auth.user.CommonProfile;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.gluu.oxauth.client.conf.AppConfiguration;
import org.gluu.oxauth.client.conf.ClaimToAttributeMapping;
import org.gluu.oxauth.client.conf.Configuration;
import org.gluu.oxauth.client.conf.LdapAppConfiguration;
import org.gluu.oxauth.client.exception.CommunicationException;
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
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.oxauth.model.register.ApplicationType;
import org.xdi.util.StringHelper;
import org.xdi.util.exception.ConfigurationException;
import org.xdi.util.init.Initializable;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * This class is the oxAuth client to authenticate users and retrieve user
 * profile
 * 
 * @author Yuriy Movchan 11/02/2015
 */
public class OpenIdClient<C extends AppConfiguration, L extends LdapAppConfiguration> extends Initializable implements Client<UserProfile> {

	private final Logger logger = LoggerFactory.getLogger(OpenIdClient.class);

	private static final String STATE_PARAMETER = "#state_parameter";
    private static final String NONCE_PARAMETER = "#nonce_parameter";

	// Register new client earlier than old client was expired to allow execute authorization requests
	private static final long NEW_CLIENT_EXPIRATION_OVERLAP = 60 * 1000;

	private final ReentrantLock clientLock = new ReentrantLock();

	private C appConfiguration;

	private String clientId;
	private String clientSecret;
	private long clientExpiration;

	private boolean preRegisteredClient;

	private OpenIdConfigurationResponse openIdConfiguration;

	private Configuration<C, L> configuration;

	public OpenIdClient(final Configuration<C, L> configuration) {
		this.configuration = configuration;
		this.appConfiguration = configuration.getAppConfiguration();
	}

	@Override
	public void init() {
		super.init();
		initClient();
	}

	protected void initInternal() {
		this.clientId = appConfiguration.getOpenIdClientId();
		this.clientSecret = appConfiguration.getOpenIdClientPassword();
		
		if (StringHelper.isNotEmpty(this.clientSecret)) {
			try {
				StringEncrypter stringEncrypter = StringEncrypter.instance(this.configuration.getCryptoConfigurationSalt());
				this.clientSecret = stringEncrypter.decrypt(this.clientSecret);
			} catch (EncryptionException ex) {
				logger.warn("Assuming that client password is not encrypted!");
			}
		}

		this.preRegisteredClient = StringHelper.isNotEmpty(this.clientId) && StringHelper.isNotEmpty(this.clientSecret);

		loadOpenIdConfiguration();
	}

	private void loadOpenIdConfiguration() {
		String openIdProvider = appConfiguration.getOpenIdProviderUrl();
		if (StringHelper.isEmpty(openIdProvider)) {
			throw new ConfigurationException("OpenIdProvider Url is invalid");
		}

		final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(openIdProvider);
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
		logger.info("Registering OpenId client");

		String clientName = this.appConfiguration.getApplicationName() + " client";
		RegisterRequest registerRequest = new RegisterRequest(ApplicationType.WEB, clientName, Arrays.asList(this.appConfiguration.getOpenIdRedirectUrl()));
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
	 * {@InheritDoc}
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * {@InheritDoc}
	 */
	public String getRedirectionUrl(final WebContext context) {
		init();

		final String state = RandomStringUtils.randomAlphanumeric(10);
		final String nonce = RandomStringUtils.randomAlphanumeric(10);

		final AuthorizationRequest authorizationRequest = new AuthorizationRequest(Arrays.asList(ResponseType.CODE), this.clientId, this.appConfiguration.getOpenIdScopes(),
				this.appConfiguration.getOpenIdRedirectUrl(), null);

		authorizationRequest.setState(state);
		authorizationRequest.setNonce(nonce);

		context.setSessionAttribute(getName() + STATE_PARAMETER, state);
        context.setSessionAttribute(getName() + NONCE_PARAMETER, nonce);

		final String redirectionUrl = this.openIdConfiguration.getAuthorizationEndpoint() + "?" + authorizationRequest.getQueryString();
		logger.debug("oxAuth redirection Url: '{}'", redirectionUrl);

		return redirectionUrl;
	}

	/**
	 * {@InheritDoc}
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
	 * {@InheritDoc}
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
	 * {@InheritDoc}
	 */
	@Override
	public final OpenIdCredentials getCredentials(final WebContext context) {
		final String authorizationCode = context.getRequestParameter(ResponseType.CODE.getValue());

		final OpenIdCredentials clientCredential = new OpenIdCredentials(authorizationCode);
		clientCredential.setClientName(getName());
		logger.debug("Client credential: '{}'", clientCredential);

		return clientCredential;
	}

	/**
	 * {@InheritDoc}
	 */
	@Override
	public UserProfile getUserProfile(final OpenIdCredentials credential, final WebContext context) {
		init();

		try {
			final String accessToken = getAccessToken(credential);
			final UserInfoResponse userInfoResponse = getUserInfo(accessToken);

			final UserProfile profile = retrieveUserProfileFromUserInfoResponse(context, userInfoResponse);
			logger.debug("User profile: '{}'", profile);

			return profile;
		} catch (final Exception ex) {
			throw new CommunicationException(ex);
		}
	}

	private String getAccessToken(final OpenIdCredentials credential) {
		// Request access token using the authorization code
		logger.debug("Getting access token");

		final TokenClient tokenClient = new TokenClient(this.openIdConfiguration.getTokenEndpoint());

		final TokenResponse tokenResponse = tokenClient.execAuthorizationCode(credential.getAuthorizationCode(), this.appConfiguration.getOpenIdRedirectUrl(), this.clientId, this.clientSecret);
		logger.trace("tokenResponse.getStatus(): '{}'", tokenResponse.getStatus());
		logger.trace("tokenResponse.getErrorType(): '{}'", tokenResponse.getErrorType());

		final String accessToken = tokenResponse.getAccessToken();
		logger.trace("accessToken : " + accessToken);

		return accessToken;
	}

	private UserInfoResponse getUserInfo(final String accessToken) {
		logger.debug("Session validation successful. Getting user information");

		final UserInfoClient userInfoClient = new UserInfoClient(this.openIdConfiguration.getUserInfoEndpoint());
		final UserInfoResponse userInfoResponse = userInfoClient.execUserInfo(accessToken);

		logger.trace("userInfoResponse.getStatus(): '{}'", userInfoResponse.getStatus());
		logger.trace("userInfoResponse.getErrorType(): '{}'", userInfoResponse.getErrorType());
		logger.debug("userInfoResponse.getClaims(): '{}'", userInfoResponse.getClaims());

		return userInfoResponse;
	}

	protected CommonProfile retrieveUserProfileFromUserInfoResponse(final WebContext context, final UserInfoResponse userInfoResponse) {
		final CommonProfile profile = new CommonProfile();

		String nonceResponse = getFirstClaim(userInfoResponse, JwtClaimName.NONCE);
        final String nonceSession = (String) context.getSessionAttribute(getName() + NONCE_PARAMETER);
        logger.debug("Session nonce: '{}'", nonceSession);
        if (!StringHelper.equals(nonceSession, nonceResponse)) {
            logger.error("User info response:  nonce is not matching.");
            throw new CommunicationException("Nonce is not match");
        }

		String id = getFirstClaim(userInfoResponse, JwtClaimName.USER_NAME);
		if (StringHelper.isEmpty(id)) {
			id = getFirstClaim(userInfoResponse, JwtClaimName.SUBJECT_IDENTIFIER);
		}
		profile.setId(id);

		List<ClaimToAttributeMapping> claimMappings = this.appConfiguration.getOpenIdClaimMapping();
		if ((claimMappings == null) || (claimMappings.size() == 0)) {
			logger.info("Using default claims to attributes mapping");
			profile.setUserName(id);
			profile.setEmail(getFirstClaim(userInfoResponse, JwtClaimName.EMAIL));
	
			profile.setDisplayName(getFirstClaim(userInfoResponse, JwtClaimName.NAME));
			profile.setFirstName(getFirstClaim(userInfoResponse, JwtClaimName.GIVEN_NAME));
			profile.setFamilyName(getFirstClaim(userInfoResponse, JwtClaimName.FAMILY_NAME));
			profile.setZone(getFirstClaim(userInfoResponse, JwtClaimName.ZONEINFO));
			profile.setLocale(getFirstClaim(userInfoResponse, JwtClaimName.LOCALE));
		} else {
			for (ClaimToAttributeMapping mapping : claimMappings) {
				String attribute = mapping.getAttribute();
				String value = getFirstClaim(userInfoResponse, mapping.getClaim());
				profile.addAttribute(attribute, value);
				logger.trace("Adding attribute '{}' with value '{}'", attribute, value);
			}
		}

		return profile;
	}

	protected String getFirstClaim(final UserInfoResponse userInfoResponse, final String claimName) {
		final List<String> claims = userInfoResponse.getClaim(claimName);

		if ((claims == null) || claims.isEmpty()) {
			return null;
		}

		return claims.get(0);
	}

	public C getAppConfiguration() {
		return appConfiguration;
	}

	public OpenIdConfigurationResponse getOpenIdConfiguration() {
		return openIdConfiguration;
	}

}
