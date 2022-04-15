/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.RandomStringUtils;
import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.gluu.oxauth.client.auth.user.CommonProfile;
import org.gluu.oxauth.client.auth.user.UserProfile;
import org.gluu.oxauth.client.exception.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gluu.conf.model.AppConfiguration;
import org.gluu.conf.model.AppConfigurationEntry;
import org.gluu.conf.model.ClaimToAttributeMapping;
import org.gluu.conf.service.ConfigurationFactory;
import org.gluu.context.WebContext;
import org.gluu.oxauth.client.AuthorizationRequest;
import org.gluu.oxauth.client.EndSessionRequest;
import org.gluu.oxauth.client.OpenIdConfigurationClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.client.RegisterClient;
import org.gluu.oxauth.client.RegisterRequest;
import org.gluu.oxauth.client.RegisterResponse;
import org.gluu.oxauth.client.TokenClient;
import org.gluu.oxauth.client.TokenResponse;
import org.gluu.oxauth.client.UserInfoClient;
import org.gluu.oxauth.client.UserInfoResponse;
import org.gluu.oxauth.model.authorize.AuthorizeRequestParam;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.Prompt;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.exception.InvalidJwtException;
import org.gluu.oxauth.model.jwt.Jwt;
import org.gluu.oxauth.model.jwt.JwtClaimName;
import org.gluu.oxauth.model.jwt.JwtType;
import org.gluu.oxauth.model.register.ApplicationType;
import org.gluu.util.StringHelper;
import org.gluu.util.exception.ConfigurationException;
import org.gluu.util.init.Initializable;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;

/**
 * This class is the oxAuth client to authenticate users and retrieve user
 * profile
 * 
 * @author Yuriy Movchan 11/02/2015
 */
public class OpenIdClient<C extends AppConfiguration, L extends AppConfigurationEntry> extends Initializable implements Client<UserProfile> {

	private final Logger logger = LoggerFactory.getLogger(OpenIdClient.class);

	private static final String SESSION_STATE_PARAMETER = "#session_state_parameter";
    private static final String SESSION_NONCE_PARAMETER = "#session_nonce_parameter";
    private static final String SESSION_ID_TOKEN_PARAMETER = "#session_id_token";

	// Register new client earlier than old client was expired to allow execute authorization requests
	private static final long NEW_CLIENT_EXPIRATION_OVERLAP = 60 * 1000;

	private final ReentrantLock clientLock = new ReentrantLock();

	private C appConfiguration;

	private String clientId;
	private String clientSecret;
	private long clientExpiration;

	private boolean preRegisteredClient;

	private OpenIdConfigurationResponse openIdConfiguration;

	private ConfigurationFactory<C, L> configuration;

	public OpenIdClient(final ConfigurationFactory<C, L> configuration) {
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

        try {
            loadOpenIdConfiguration();
        } catch (IOException ex) {
            throw new ConfigurationException("Failed to load oxAuth configuration");
        }
	}

	private void loadOpenIdConfiguration() throws IOException {
		String openIdProvider = appConfiguration.getOpenIdProviderUrl();
		if (StringHelper.isEmpty(openIdProvider)) {
			throw new ConfigurationException("OpenIdProvider Url is invalid");
		}

		final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(openIdProvider);
		ApacheHttpClient43Engine httpEngine = new ApacheHttpClient43Engine();
		httpEngine.setFollowRedirects(true);
		openIdConfigurationClient.setExecutor(httpEngine);
		final OpenIdConfigurationResponse response = openIdConfigurationClient.execOpenIdConfiguration();
		if ((response == null) || (response.getStatus() != 200)) {
			logger.info("Failed to load oxAuth configuration. Http code ( {} ). Body: {}", response.getStatus(),response.getEntity());
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

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

    @Override
    public String getRedirectionUrl(final WebContext context) {
        return getRedirectionUrl(context, null, null, false);
    }

    @Override
    public String getRedirectionUrl(final WebContext context, Map<String, String> customStateParameters, final Map<String, String> customParameters, final boolean force) {
		init();

		String state = RandomStringUtils.randomAlphanumeric(10);
		String nonce = RandomStringUtils.randomAlphanumeric(10);
        
        if (customStateParameters != null) {
            Jwt jwt = new Jwt();
            // Header
            jwt.getHeader().setType(JwtType.JWT);
            jwt.getHeader().setAlgorithm(SignatureAlgorithm.NONE);

            // Claims
            for (Entry<String, String> entry : customStateParameters.entrySet()) {
                jwt.getClaims().setClaim(entry.getKey(), entry.getValue());
            }

            // Put state
            jwt.getClaims().setClaim(AuthorizeRequestParam.STATE, state);

            // Store jwt in state
            state = jwt.toString();
        }
        

		final AuthorizationRequest authorizationRequest = new AuthorizationRequest(Arrays.asList(ResponseType.CODE), this.clientId, this.appConfiguration.getOpenIdScopes(),
				this.appConfiguration.getOpenIdRedirectUrl(), null);

		authorizationRequest.setState(state);
		authorizationRequest.setNonce(nonce);
		
		if (force) {
			authorizationRequest.setPrompts(Arrays.asList(Prompt.LOGIN));
		}

		context.setSessionAttribute(getName() + SESSION_STATE_PARAMETER, state);
        context.setSessionAttribute(getName() + SESSION_NONCE_PARAMETER, nonce);
        
        if (customParameters != null) {
            for (Entry<String, String> entry : customParameters.entrySet()) {
                authorizationRequest.addCustomParameter(entry.getKey(), entry.getValue());
            }
        }

		final String redirectionUrl = this.openIdConfiguration.getAuthorizationEndpoint() + "?" + authorizationRequest.getQueryString();
		logger.debug("oxAuth redirection Url: '{}'", redirectionUrl);

		return redirectionUrl;
	}

	@Override
    public String getLogoutRedirectionUrl(WebContext context) {
        init();

        final String state = RandomStringUtils.randomAlphanumeric(10);
        final String postLogoutRedirectUri = this.appConfiguration.getOpenIdPostLogoutRedirectUri();
        String idToken = (String) context.getSessionAttribute(getName() + SESSION_ID_TOKEN_PARAMETER);
        
        // Allow to send logout request if session is expired 
        if (idToken == null) {
            idToken = "";
        }
        
        final EndSessionRequest endSessionRequest = new EndSessionRequest(idToken, postLogoutRedirectUri, state);

        final String redirectionUrl = this.openIdConfiguration.getEndSessionEndpoint() + "?" + endSessionRequest.getQueryString();
        logger.debug("oxAuth redirection Url: '{}'", redirectionUrl);

        return redirectionUrl;
    }

    @Override
    public boolean isAuthorized(WebContext context) {
        init();

        String idToken = (String) context.getSessionAttribute(getName() + SESSION_ID_TOKEN_PARAMETER);
        if (idToken == null) {
            return false;
        }
        
        return true;
    }

    @Override
    public void clearAuthorized(WebContext context) {
        init();

        context.setSessionAttribute(getName() + SESSION_ID_TOKEN_PARAMETER, null);
    }

    @Override
	public boolean isAuthorizationResponse(final WebContext context) {
		final String authorizationCode = context.getRequestParameter(ResponseType.CODE.getValue());
		logger.debug("oxAuth authorization code: '{}'", authorizationCode);

		final boolean result = StringHelper.isNotEmpty(authorizationCode);
		logger.debug("Is authorization request: '{}'", result);

		return result;
	}

	@Override
	public boolean isValidRequestState(final WebContext context) {
		final String state = context.getRequestParameter(AuthorizeRequestParam.STATE);
		logger.debug("oxAuth request state: '{}'", state);

		final Object sessionState = context.getSessionAttribute(getName() + SESSION_STATE_PARAMETER);
		logger.debug("Session context state: '{}'", sessionState);

		final boolean emptySessionState = StringHelper.isEmptyString(sessionState);
		if (emptySessionState) {
			return false;
		}

		final boolean result = StringHelper.equals(state, (String) sessionState);
		logger.debug("Is valid state: '{}'", result);

		return result;
	}

	@Override
    public String getRequestState(WebContext context) {
        final String state = context.getRequestParameter(AuthorizeRequestParam.STATE);
        
        return state;
    }

    @Override
	public final OpenIdCredentials getCredentials(final WebContext context) {
		final String authorizationCode = context.getRequestParameter(ResponseType.CODE.getValue());

		final OpenIdCredentials clientCredential = new OpenIdCredentials(authorizationCode);
		clientCredential.setClientName(getName());
		logger.debug("Client credential: '{}'", clientCredential);

		return clientCredential;
	}

	@Override
	public UserProfile getUserProfile(final OpenIdCredentials credential, final WebContext context) {
		init();

		try {
	        // Request access token using the authorization code
	        logger.debug("Getting access token");

	        final TokenClient tokenClient = new TokenClient(this.openIdConfiguration.getTokenEndpoint());

	        final TokenResponse tokenResponse = tokenClient.execAuthorizationCode(credential.getAuthorizationCode(), this.appConfiguration.getOpenIdRedirectUrl(), this.clientId, this.clientSecret);
	        logger.trace("tokenResponse.getStatus(): '{}'", tokenResponse.getStatus());
	        logger.trace("tokenResponse.getErrorType(): '{}'", tokenResponse.getErrorType());

	        final String accessToken = tokenResponse.getAccessToken();
	        logger.trace("accessToken : " + accessToken);

	        final String idToken = tokenResponse.getIdToken();
            logger.trace("idToken : " + idToken);

            // Store id_token in session
            context.setSessionAttribute(getName() + SESSION_ID_TOKEN_PARAMETER, idToken);

            // Parse JWT
            Jwt jwt;
            try {
                jwt = Jwt.parse(idToken);
            } catch (InvalidJwtException ex) {
                logger.error("Failed to parse id_token: {}", idToken);
                throw new CommunicationException("Failed to parse id_token");
            }

	        final UserInfoResponse userInfoResponse = getUserInfo(accessToken);

			final UserProfile profile = retrieveUserProfileFromUserInfoResponse(context, jwt, userInfoResponse);
			logger.debug("User profile: '{}'", profile);

			return profile;
		} catch (final Exception ex) {
			throw new CommunicationException(ex);
		}
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

	protected CommonProfile retrieveUserProfileFromUserInfoResponse(final WebContext context, final Jwt jwt, final UserInfoResponse userInfoResponse) {
		final CommonProfile profile = new CommonProfile();

		String nonceResponse = (String) jwt.getClaims().getClaim(JwtClaimName.NONCE);
        final String nonceSession = (String) context.getSessionAttribute(getName() + SESSION_NONCE_PARAMETER);
        logger.debug("Session nonce: '{}'", nonceSession);
        if (!StringHelper.equals(nonceSession, nonceResponse)) {
            logger.error("User info response:  nonce is not matching.");
            throw new CommunicationException("Nonce is not match" + nonceResponse + " : " + nonceSession);
        }

		String id = getFirstClaim(userInfoResponse, JwtClaimName.USER_NAME);
		if (StringHelper.isEmpty(id)) {
			id = getFirstClaim(userInfoResponse, JwtClaimName.SUBJECT_IDENTIFIER);
		}
		profile.setId(id);

		String acrResponse = (String) jwt.getClaims().getClaim(JwtClaimName.AUTHENTICATION_CONTEXT_CLASS_REFERENCE);
        logger.debug("Authentication ACR: '{}'", acrResponse);
        profile.setUsedAcr(acrResponse);

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

    @Override
    public void setAttribute(WebContext context, String attributeName, Object attributeValue) {
        init();

        context.setSessionAttribute(getName() + attributeName, attributeValue);
    }

    @Override
    public Object getAttribute(WebContext context, String attributeName) {
        init();

        return context.getSessionAttribute(getName() + attributeName);
    }

}
