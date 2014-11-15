/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.client;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.gluu.oxauth.cas.auth.exception.CommunicationException;
import org.gluu.oxauth.cas.auth.exception.ConfigurationException;
import org.gluu.oxauth.cas.auth.login.flow.ClientAction;
import org.gluu.oxauth.cas.auth.principal.ClientCredential;
import org.gluu.oxauth.cas.auth.user.CommonProfile;
import org.gluu.oxauth.cas.auth.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.context.WebContext;
import org.xdi.oxauth.client.AuthorizationRequest;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.oxauth.model.jwt.JwtClaimName;
import org.xdi.util.StringHelper;
import org.xdi.util.init.Initializable;

/**
 * This class is the oxAuth client to authenticate users and retreview user profile
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public class AuthClient extends Initializable implements Client<UserProfile> {

	private final Logger logger = LoggerFactory.getLogger(ClientAction.class);

	private static final String STATE_PARAMETER = "#oxauth_state_parameter";

	@NotNull
	private String clientId;

	@NotNull
	private String clientSecret;

	@NotNull
	private String discoveryUrl;

	@NotNull
	private String callbackUrl;

	private OpenIdConfigurationResponse openIdConfiguration;

	public AuthClient() {}

	public AuthClient(final String clientId, final String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	protected void initInternal() {
		loadOpenIdConfiguration();
	}

	private void loadOpenIdConfiguration() {
		if (StringHelper.isEmpty(this.discoveryUrl)) {
			throw new ConfigurationException("Discovery Url is invalid");
		}

		final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(this.discoveryUrl);
		final OpenIdConfigurationResponse response = openIdConfigurationClient.execOpenIdConfiguration();
		if ((response == null) || (response.getStatus() != 200)) {
			throw new ConfigurationException("Failed to load oxAuth configuration");
		}

		logger.info("Successfully loaded oxAuth configuration");

		this.openIdConfiguration = response;
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

		final AuthorizationRequest authorizationRequest = new AuthorizationRequest(Arrays.asList(ResponseType.CODE), this.clientId,
				Arrays.asList("openid", "profile", "email"), this.callbackUrl, null);

		authorizationRequest.setNonce("none");
		authorizationRequest.setState(state);

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

		final TokenResponse tokenResponse = tokenClient.execAuthorizationCode(credential.getAuthorizationCode(), this.callbackUrl, this.clientId, this.clientSecret);
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

		profile.setId(getFirstClaim(userInfoResponse, JwtClaimName.SUBJECT_IDENTIFIER));
		profile.setUserName(getFirstClaim(userInfoResponse, JwtClaimName.SUBJECT_IDENTIFIER));

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

	public String getDiscoveryUrl() {
		return discoveryUrl;
	}

	public void setDiscoveryUrl(String discoveryUrl) {
		this.discoveryUrl = discoveryUrl;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthClient [clientId=").append(clientId).append(", discoveryUrl=").append(discoveryUrl).append(", callbackUrl=")
				.append(callbackUrl).append("]");
		return builder.toString();
	}

}
