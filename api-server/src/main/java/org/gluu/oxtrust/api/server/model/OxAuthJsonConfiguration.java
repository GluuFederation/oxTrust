package org.gluu.oxtrust.api.server.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "issuer", "baseEndpoint", "authorizationEndpoint", "tokenEndpoint", "tokenRevocationEndpoint",
		"userInfoEndpoint", "clientInfoEndpoint", "checkSessionIFrame", "endSessionEndpoint", "jwksUri",
		"registrationEndpoint", "openIdDiscoveryEndpoint", "openIdConfigurationEndpoint", "idGenerationEndpoint",
		"introspectionEndpoint", "umaConfigurationEndpoint", "sectorIdentifierEndpoint", "oxElevenGenerateKeyEndpoint",
		"oxElevenSignEndpoint", "oxElevenVerifySignatureEndpoint", "oxElevenDeleteKeyEndpoint", "oxElevenJwksEndpoint",
		"openidSubAttribute", "responseTypesSupported", "grantTypesSupported", "subjectTypesSupported",
		"defaultSubjectType", "userInfoSigningAlgValuesSupported", "userInfoEncryptionAlgValuesSupported",
		"userInfoEncryptionEncValuesSupported", "idTokenSigningAlgValuesSupported",
		"idTokenEncryptionAlgValuesSupported", "idTokenEncryptionEncValuesSupported",
		"requestObjectSigningAlgValuesSupported", "requestObjectEncryptionAlgValuesSupported",
		"requestObjectEncryptionEncValuesSupported", "tokenEndpointAuthMethodsSupported",
		"tokenEndpointAuthSigningAlgValuesSupported", "dynamicRegistrationCustomAttributes", "displayValuesSupported",
		"claimTypesSupported", "serviceDocumentation", "claimsLocalesSupported",
		"idTokenTokenBindingCnfValuesSupported", "uiLocalesSupported", "dynamicGrantTypeDefault",
		"claimsParameterSupported", "requestParameterSupported", "requestUriParameterSupported",
		"requireRequestUriRegistration", "allowPostLogoutRedirectWithoutValidation",
		"introspectionAccessTokenMustHaveUmaProtectionScope", "opPolicyUri", "opTosUri", "authorizationCodeLifetime",
		"refreshTokenLifetime", "idTokenLifetime", "accessTokenLifetime", "umaResourceLifetime", "sessionAsJwt",
		"umaRptLifetime", "umaTicketLifetime", "umaPctLifetime", "umaAddScopesAutomatically", "umaValidateClaimToken",
		"umaGrantAccessIfNoPolicies", "umaRestrictResourceToAssociatedClient",
		"umaKeepClientDuringResourceSetRegistration", "umaRptAsJwt", "cleanServiceInterval", "keyRegenerationEnabled",
		"keyRegenerationInterval", "defaultSignatureAlgorithm", "oxOpenIdConnectVersion", "organizationInum", "oxId",
		"dynamicRegistrationEnabled", "dynamicRegistrationExpirationTime",
		"dynamicRegistrationPersistClientAuthorizations", "trustedClientEnabled",
		"skipAuthorizationForOpenIdScopeAndPairwiseId", "dynamicRegistrationScopesParamEnabled",
		"dynamicRegistrationCustomObjectClass", "personCustomObjectClassList", "persistIdTokenInLdap",
		"persistRefreshTokenInLdap", "authenticationFiltersEnabled", "invalidateSessionCookiesAfterAuthorizationFlow",
		"clientAuthenticationFiltersEnabled", "authenticationFilters", "clientAuthenticationFilters", "configurationInum",
		"sessionIdUnusedLifetime", "sessionIdUnauthenticatedUnusedLifetime", "sessionIdEnabled",
		"sessionIdPersistOnPromptNone", "sessionIdLifetime", "configurationUpdateInterval", "cssLocation", "jsLocation",
		"imgLocation", "metricReporterInterval", "metricReporterKeepDataDays", "pairwiseIdType",
		"pairwiseCalculationKey", "pairwiseCalculationSalt", "shareSubjectIdBetweenClientsWithSameSectorId",
		"webKeysStorage", "dnName", "keyStoreFile",
		"keyStoreSecret", "endSessionWithAccessToken", "clientWhiteList", "clientBlackList", "legacyIdTokenClaims",
		"customHeadersWithAuthorizationResponse", "frontChannelLogoutSessionSupported", "updateUserLastLogonTime",
		"updateClientAccessTime", "enableClientGrantTypeUpdate", "corsConfigurationFilters",
		"logClientIdOnClientAuthentication", "logClientNameOnClientAuthentication", "httpLoggingEnabled",
		"httpLoggingExludePaths", "externalLoggerConfiguration", "authorizationRequestCustomAllowedParameters",
		"legacyDynamicRegistrationScopeParam", "openidScopeBackwardCompatibility", "useCacheForAllImplicitFlowObjects",
		"disableU2fEndpoint", "authenticationProtectionConfiguration", "fido2Configuration", "loggingLevel",
		"errorHandlingMethod" })
public class OxAuthJsonConfiguration {

	@JsonProperty("issuer")
	private String issuer;
	@JsonProperty("baseEndpoint")
	private String baseEndpoint;
	@JsonProperty("authorizationEndpoint")
	private String authorizationEndpoint;
	@JsonProperty("tokenEndpoint")
	private String tokenEndpoint;
	@JsonProperty("tokenRevocationEndpoint")
	private String tokenRevocationEndpoint;
	@JsonProperty("userInfoEndpoint")
	private String userInfoEndpoint;
	@JsonProperty("clientInfoEndpoint")
	private String clientInfoEndpoint;
	@JsonProperty("checkSessionIFrame")
	private String checkSessionIFrame;
	@JsonProperty("endSessionEndpoint")
	private String endSessionEndpoint;
	@JsonProperty("jwksUri")
	private String jwksUri;
	@JsonProperty("registrationEndpoint")
	private String registrationEndpoint;
	@JsonProperty("openIdDiscoveryEndpoint")
	private String openIdDiscoveryEndpoint;
	@JsonProperty("openIdConfigurationEndpoint")
	private String openIdConfigurationEndpoint;
	@JsonProperty("idGenerationEndpoint")
	private String idGenerationEndpoint;
	@JsonProperty("introspectionEndpoint")
	private String introspectionEndpoint;
	@JsonProperty("umaConfigurationEndpoint")
	private String umaConfigurationEndpoint;
	@JsonProperty("sectorIdentifierEndpoint")
	private String sectorIdentifierEndpoint;
	@JsonProperty("oxElevenGenerateKeyEndpoint")
	private String oxElevenGenerateKeyEndpoint;
	@JsonProperty("oxElevenSignEndpoint")
	private String oxElevenSignEndpoint;
	@JsonProperty("oxElevenVerifySignatureEndpoint")
	private String oxElevenVerifySignatureEndpoint;
	@JsonProperty("oxElevenDeleteKeyEndpoint")
	private String oxElevenDeleteKeyEndpoint;
	@JsonProperty("oxElevenJwksEndpoint")
	private String oxElevenJwksEndpoint;
	@JsonProperty("openidSubAttribute")
	private String openidSubAttribute;
	@JsonProperty("responseTypesSupported")
	private List<List<String>> responseTypesSupported = null;
	@JsonProperty("grantTypesSupported")
	private List<String> grantTypesSupported = null;
	@JsonProperty("subjectTypesSupported")
	private List<String> subjectTypesSupported = null;
	@JsonProperty("defaultSubjectType")
	private String defaultSubjectType;
	@JsonProperty("userInfoSigningAlgValuesSupported")
	private List<String> userInfoSigningAlgValuesSupported = null;
	@JsonProperty("userInfoEncryptionAlgValuesSupported")
	private List<String> userInfoEncryptionAlgValuesSupported = null;
	@JsonProperty("userInfoEncryptionEncValuesSupported")
	private List<String> userInfoEncryptionEncValuesSupported = null;
	@JsonProperty("idTokenSigningAlgValuesSupported")
	private List<String> idTokenSigningAlgValuesSupported = null;
	@JsonProperty("idTokenEncryptionAlgValuesSupported")
	private List<String> idTokenEncryptionAlgValuesSupported = null;
	@JsonProperty("idTokenEncryptionEncValuesSupported")
	private List<String> idTokenEncryptionEncValuesSupported = null;
	@JsonProperty("requestObjectSigningAlgValuesSupported")
	private List<String> requestObjectSigningAlgValuesSupported = null;
	@JsonProperty("requestObjectEncryptionAlgValuesSupported")
	private List<String> requestObjectEncryptionAlgValuesSupported = null;
	@JsonProperty("requestObjectEncryptionEncValuesSupported")
	private List<String> requestObjectEncryptionEncValuesSupported = null;
	@JsonProperty("tokenEndpointAuthMethodsSupported")
	private List<String> tokenEndpointAuthMethodsSupported = null;
	@JsonProperty("tokenEndpointAuthSigningAlgValuesSupported")
	private List<String> tokenEndpointAuthSigningAlgValuesSupported = null;
	@JsonProperty("dynamicRegistrationCustomAttributes")
	private List<String> dynamicRegistrationCustomAttributes = null;
	@JsonProperty("displayValuesSupported")
	private List<String> displayValuesSupported = null;
	@JsonProperty("claimTypesSupported")
	private List<String> claimTypesSupported = null;
	@JsonProperty("serviceDocumentation")
	private String serviceDocumentation;
	@JsonProperty("claimsLocalesSupported")
	private List<String> claimsLocalesSupported = null;
	@JsonProperty("idTokenTokenBindingCnfValuesSupported")
	private List<String> idTokenTokenBindingCnfValuesSupported = null;
	@JsonProperty("uiLocalesSupported")
	private List<String> uiLocalesSupported = null;
	@JsonProperty("dynamicGrantTypeDefault")
	private List<String> dynamicGrantTypeDefault = null;
	@JsonProperty("claimsParameterSupported")
	private Boolean claimsParameterSupported;
	@JsonProperty("requestParameterSupported")
	private Boolean requestParameterSupported;
	@JsonProperty("requestUriParameterSupported")
	private Boolean requestUriParameterSupported;
	@JsonProperty("requireRequestUriRegistration")
	private Boolean requireRequestUriRegistration;
	@JsonProperty("allowPostLogoutRedirectWithoutValidation")
	private Boolean allowPostLogoutRedirectWithoutValidation;
	@JsonProperty("introspectionAccessTokenMustHaveUmaProtectionScope")
	private Boolean introspectionAccessTokenMustHaveUmaProtectionScope;
	@JsonProperty("opPolicyUri")
	private String opPolicyUri;
	@JsonProperty("opTosUri")
	private String opTosUri;
	@JsonProperty("authorizationCodeLifetime")
	private Integer authorizationCodeLifetime;
	@JsonProperty("refreshTokenLifetime")
	private Integer refreshTokenLifetime;
	@JsonProperty("idTokenLifetime")
	private Integer idTokenLifetime;
	@JsonProperty("accessTokenLifetime")
	private Integer accessTokenLifetime;
	@JsonProperty("umaResourceLifetime")
	private Integer umaResourceLifetime;
	@JsonProperty("sessionAsJwt")
	private Boolean sessionAsJwt;
	@JsonProperty("umaRptLifetime")
	private Integer umaRptLifetime;
	@JsonProperty("umaTicketLifetime")
	private Integer umaTicketLifetime;
	@JsonProperty("umaPctLifetime")
	private Integer umaPctLifetime;
	@JsonProperty("umaAddScopesAutomatically")
	private Boolean umaAddScopesAutomatically;
	@JsonProperty("umaValidateClaimToken")
	private Boolean umaValidateClaimToken;
	@JsonProperty("umaGrantAccessIfNoPolicies")
	private Boolean umaGrantAccessIfNoPolicies;
	@JsonProperty("umaRestrictResourceToAssociatedClient")
	private Boolean umaRestrictResourceToAssociatedClient;
	@JsonProperty("umaKeepClientDuringResourceSetRegistration")
	private Boolean umaKeepClientDuringResourceSetRegistration;
	@JsonProperty("umaRptAsJwt")
	private Boolean umaRptAsJwt;
	@JsonProperty("cleanServiceInterval")
	private Integer cleanServiceInterval;
	@JsonProperty("keyRegenerationEnabled")
	private Boolean keyRegenerationEnabled;
	@JsonProperty("keyRegenerationInterval")
	private Integer keyRegenerationInterval;
	@JsonProperty("defaultSignatureAlgorithm")
	private String defaultSignatureAlgorithm;
	@JsonProperty("oxOpenIdConnectVersion")
	private String oxOpenIdConnectVersion;
	@JsonProperty("organizationInum")
	private String organizationInum;
	@JsonProperty("oxId")
	private String oxId;
	@JsonProperty("dynamicRegistrationEnabled")
	private Boolean dynamicRegistrationEnabled;
	@JsonProperty("dynamicRegistrationExpirationTime")
	private Integer dynamicRegistrationExpirationTime;
	@JsonProperty("dynamicRegistrationPersistClientAuthorizations")
	private Boolean dynamicRegistrationPersistClientAuthorizations;
	@JsonProperty("trustedClientEnabled")
	private Boolean trustedClientEnabled;
	@JsonProperty("skipAuthorizationForOpenIdScopeAndPairwiseId")
	private Boolean skipAuthorizationForOpenIdScopeAndPairwiseId;
	@JsonProperty("dynamicRegistrationScopesParamEnabled")
	private Boolean dynamicRegistrationScopesParamEnabled;
	@JsonProperty("dynamicRegistrationCustomObjectClass")
	private String dynamicRegistrationCustomObjectClass;
	@JsonProperty("personCustomObjectClassList")
	private List<String> personCustomObjectClassList = null;
	@JsonProperty("persistIdTokenInLdap")
	private Boolean persistIdTokenInLdap;
	@JsonProperty("persistRefreshTokenInLdap")
	private Boolean persistRefreshTokenInLdap;
	@JsonProperty("authenticationFiltersEnabled")
	private Boolean authenticationFiltersEnabled;
	@JsonProperty("invalidateSessionCookiesAfterAuthorizationFlow")
	private Boolean invalidateSessionCookiesAfterAuthorizationFlow;
	@JsonProperty("clientAuthenticationFiltersEnabled")
	private Boolean clientAuthenticationFiltersEnabled;
	@JsonProperty("authenticationFilters")
	private List<AuthenticationFilter> authenticationFilters = null;
	@JsonProperty("clientAuthenticationFilters")
	private List<ClientAuthenticationFilter> clientAuthenticationFilters = null;
	@JsonProperty("configurationInum")
	private String configurationInum;
	@JsonProperty("sessionIdUnusedLifetime")
	private Integer sessionIdUnusedLifetime;
	@JsonProperty("sessionIdUnauthenticatedUnusedLifetime")
	private Integer sessionIdUnauthenticatedUnusedLifetime;
	@JsonProperty("sessionIdEnabled")
	private Boolean sessionIdEnabled;
	@JsonProperty("sessionIdPersistOnPromptNone")
	private Boolean sessionIdPersistOnPromptNone;
	@JsonProperty("sessionIdLifetime")
	private Integer sessionIdLifetime;
	@JsonProperty("configurationUpdateInterval")
	private Integer configurationUpdateInterval;
	@JsonProperty("cssLocation")
	private String cssLocation;
	@JsonProperty("jsLocation")
	private String jsLocation;
	@JsonProperty("imgLocation")
	private String imgLocation;
	@JsonProperty("metricReporterInterval")
	private Integer metricReporterInterval;
	@JsonProperty("metricReporterKeepDataDays")
	private Integer metricReporterKeepDataDays;
	@JsonProperty("pairwiseIdType")
	private String pairwiseIdType;
	@JsonProperty("pairwiseCalculationKey")
	private String pairwiseCalculationKey;
	@JsonProperty("pairwiseCalculationSalt")
	private String pairwiseCalculationSalt;
	@JsonProperty("shareSubjectIdBetweenClientsWithSameSectorId")
	private Boolean shareSubjectIdBetweenClientsWithSameSectorId;
	@JsonProperty("webKeysStorage")
	private String webKeysStorage;
	@JsonProperty("dnName")
	private String dnName;
	@JsonProperty("keyStoreFile")
	private String keyStoreFile;
	@JsonProperty("keyStoreSecret")
	private String keyStoreSecret;
	@JsonProperty("endSessionWithAccessToken")
	private Boolean endSessionWithAccessToken;
    @JsonProperty("cookieDomain")
    private String cookieDomain;
	@JsonProperty("clientWhiteList")
	private List<String> clientWhiteList = null;
	@JsonProperty("clientBlackList")
	private List<String> clientBlackList = null;
	@JsonProperty("legacyIdTokenClaims")
	private Boolean legacyIdTokenClaims;
	@JsonProperty("customHeadersWithAuthorizationResponse")
	private Boolean customHeadersWithAuthorizationResponse;
	@JsonProperty("frontChannelLogoutSessionSupported")
	private Boolean frontChannelLogoutSessionSupported;
	@JsonProperty("updateUserLastLogonTime")
	private Boolean updateUserLastLogonTime;
	@JsonProperty("updateClientAccessTime")
	private Boolean updateClientAccessTime;
	@JsonProperty("enableClientGrantTypeUpdate")
	private Boolean enableClientGrantTypeUpdate;
	@JsonProperty("corsConfigurationFilters")
	private List<CorsConfigurationFilter> corsConfigurationFilters = null;
	@JsonProperty("logClientIdOnClientAuthentication")
	private Boolean logClientIdOnClientAuthentication;
	@JsonProperty("logClientNameOnClientAuthentication")
	private Boolean logClientNameOnClientAuthentication;
	@JsonProperty("httpLoggingEnabled")
	private Boolean httpLoggingEnabled;
	@JsonProperty("httpLoggingExludePaths")
	private List<Object> httpLoggingExludePaths = null;
	@JsonProperty("externalLoggerConfiguration")
	private String externalLoggerConfiguration;
	@JsonProperty("authorizationRequestCustomAllowedParameters")
	private List<String> authorizationRequestCustomAllowedParameters = null;
	@JsonProperty("legacyDynamicRegistrationScopeParam")
	private Boolean legacyDynamicRegistrationScopeParam;
	@JsonProperty("openidScopeBackwardCompatibility")
	private Boolean openidScopeBackwardCompatibility;
	@JsonProperty("useCacheForAllImplicitFlowObjects")
	private Boolean useCacheForAllImplicitFlowObjects;
	@JsonProperty("disableU2fEndpoint")
	private Boolean disableU2fEndpoint;
	@JsonProperty("authenticationProtectionConfiguration")
	private AuthenticationProtectionConfiguration authenticationProtectionConfiguration;
	@JsonProperty("fido2Configuration")
	private Fido2Configuration fido2Configuration;
	@JsonProperty("loggingLevel")
	private String loggingLevel;
	@JsonProperty("errorHandlingMethod")
	private String errorHandlingMethod;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("issuer")
	public String getIssuer() {
		return issuer;
	}

	@JsonProperty("issuer")
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@JsonProperty("baseEndpoint")
	public String getBaseEndpoint() {
		return baseEndpoint;
	}

	@JsonProperty("baseEndpoint")
	public void setBaseEndpoint(String baseEndpoint) {
		this.baseEndpoint = baseEndpoint;
	}

	@JsonProperty("authorizationEndpoint")
	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	@JsonProperty("authorizationEndpoint")
	public void setAuthorizationEndpoint(String authorizationEndpoint) {
		this.authorizationEndpoint = authorizationEndpoint;
	}

	@JsonProperty("tokenEndpoint")
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	@JsonProperty("tokenEndpoint")
	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	@JsonProperty("tokenRevocationEndpoint")
	public String getTokenRevocationEndpoint() {
		return tokenRevocationEndpoint;
	}

	@JsonProperty("tokenRevocationEndpoint")
	public void setTokenRevocationEndpoint(String tokenRevocationEndpoint) {
		this.tokenRevocationEndpoint = tokenRevocationEndpoint;
	}

	@JsonProperty("userInfoEndpoint")
	public String getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	@JsonProperty("userInfoEndpoint")
	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
	}

	@JsonProperty("clientInfoEndpoint")
	public String getClientInfoEndpoint() {
		return clientInfoEndpoint;
	}

	@JsonProperty("clientInfoEndpoint")
	public void setClientInfoEndpoint(String clientInfoEndpoint) {
		this.clientInfoEndpoint = clientInfoEndpoint;
	}

	@JsonProperty("checkSessionIFrame")
	public String getCheckSessionIFrame() {
		return checkSessionIFrame;
	}

	@JsonProperty("checkSessionIFrame")
	public void setCheckSessionIFrame(String checkSessionIFrame) {
		this.checkSessionIFrame = checkSessionIFrame;
	}

	@JsonProperty("endSessionEndpoint")
	public String getEndSessionEndpoint() {
		return endSessionEndpoint;
	}

	@JsonProperty("endSessionEndpoint")
	public void setEndSessionEndpoint(String endSessionEndpoint) {
		this.endSessionEndpoint = endSessionEndpoint;
	}

	@JsonProperty("jwksUri")
	public String getJwksUri() {
		return jwksUri;
	}

	@JsonProperty("jwksUri")
	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	@JsonProperty("registrationEndpoint")
	public String getRegistrationEndpoint() {
		return registrationEndpoint;
	}

	@JsonProperty("registrationEndpoint")
	public void setRegistrationEndpoint(String registrationEndpoint) {
		this.registrationEndpoint = registrationEndpoint;
	}

	@JsonProperty("openIdDiscoveryEndpoint")
	public String getOpenIdDiscoveryEndpoint() {
		return openIdDiscoveryEndpoint;
	}

	@JsonProperty("openIdDiscoveryEndpoint")
	public void setOpenIdDiscoveryEndpoint(String openIdDiscoveryEndpoint) {
		this.openIdDiscoveryEndpoint = openIdDiscoveryEndpoint;
	}

	@JsonProperty("openIdConfigurationEndpoint")
	public String getOpenIdConfigurationEndpoint() {
		return openIdConfigurationEndpoint;
	}

	@JsonProperty("openIdConfigurationEndpoint")
	public void setOpenIdConfigurationEndpoint(String openIdConfigurationEndpoint) {
		this.openIdConfigurationEndpoint = openIdConfigurationEndpoint;
	}

	@JsonProperty("idGenerationEndpoint")
	public String getIdGenerationEndpoint() {
		return idGenerationEndpoint;
	}

	@JsonProperty("idGenerationEndpoint")
	public void setIdGenerationEndpoint(String idGenerationEndpoint) {
		this.idGenerationEndpoint = idGenerationEndpoint;
	}

	@JsonProperty("introspectionEndpoint")
	public String getIntrospectionEndpoint() {
		return introspectionEndpoint;
	}

	@JsonProperty("introspectionEndpoint")
	public void setIntrospectionEndpoint(String introspectionEndpoint) {
		this.introspectionEndpoint = introspectionEndpoint;
	}

	@JsonProperty("umaConfigurationEndpoint")
	public String getUmaConfigurationEndpoint() {
		return umaConfigurationEndpoint;
	}

	@JsonProperty("umaConfigurationEndpoint")
	public void setUmaConfigurationEndpoint(String umaConfigurationEndpoint) {
		this.umaConfigurationEndpoint = umaConfigurationEndpoint;
	}

	@JsonProperty("sectorIdentifierEndpoint")
	public String getSectorIdentifierEndpoint() {
		return sectorIdentifierEndpoint;
	}

	@JsonProperty("sectorIdentifierEndpoint")
	public void setSectorIdentifierEndpoint(String sectorIdentifierEndpoint) {
		this.sectorIdentifierEndpoint = sectorIdentifierEndpoint;
	}

	@JsonProperty("oxElevenGenerateKeyEndpoint")
	public String getOxElevenGenerateKeyEndpoint() {
		return oxElevenGenerateKeyEndpoint;
	}

	@JsonProperty("oxElevenGenerateKeyEndpoint")
	public void setOxElevenGenerateKeyEndpoint(String oxElevenGenerateKeyEndpoint) {
		this.oxElevenGenerateKeyEndpoint = oxElevenGenerateKeyEndpoint;
	}

	@JsonProperty("oxElevenSignEndpoint")
	public String getOxElevenSignEndpoint() {
		return oxElevenSignEndpoint;
	}

	@JsonProperty("oxElevenSignEndpoint")
	public void setOxElevenSignEndpoint(String oxElevenSignEndpoint) {
		this.oxElevenSignEndpoint = oxElevenSignEndpoint;
	}

	@JsonProperty("oxElevenVerifySignatureEndpoint")
	public String getOxElevenVerifySignatureEndpoint() {
		return oxElevenVerifySignatureEndpoint;
	}

	@JsonProperty("oxElevenVerifySignatureEndpoint")
	public void setOxElevenVerifySignatureEndpoint(String oxElevenVerifySignatureEndpoint) {
		this.oxElevenVerifySignatureEndpoint = oxElevenVerifySignatureEndpoint;
	}

	@JsonProperty("oxElevenDeleteKeyEndpoint")
	public String getOxElevenDeleteKeyEndpoint() {
		return oxElevenDeleteKeyEndpoint;
	}

	@JsonProperty("oxElevenDeleteKeyEndpoint")
	public void setOxElevenDeleteKeyEndpoint(String oxElevenDeleteKeyEndpoint) {
		this.oxElevenDeleteKeyEndpoint = oxElevenDeleteKeyEndpoint;
	}

	@JsonProperty("oxElevenJwksEndpoint")
	public String getOxElevenJwksEndpoint() {
		return oxElevenJwksEndpoint;
	}

	@JsonProperty("oxElevenJwksEndpoint")
	public void setOxElevenJwksEndpoint(String oxElevenJwksEndpoint) {
		this.oxElevenJwksEndpoint = oxElevenJwksEndpoint;
	}

	@JsonProperty("openidSubAttribute")
	public String getOpenidSubAttribute() {
		return openidSubAttribute;
	}

	@JsonProperty("openidSubAttribute")
	public void setOpenidSubAttribute(String openidSubAttribute) {
		this.openidSubAttribute = openidSubAttribute;
	}

	@JsonProperty("responseTypesSupported")
	public List<List<String>> getResponseTypesSupported() {
		return responseTypesSupported;
	}

	@JsonProperty("responseTypesSupported")
	public void setResponseTypesSupported(List<List<String>> responseTypesSupported) {
		this.responseTypesSupported = responseTypesSupported;
	}

	@JsonProperty("grantTypesSupported")
	public List<String> getGrantTypesSupported() {
		return grantTypesSupported;
	}

	@JsonProperty("grantTypesSupported")
	public void setGrantTypesSupported(List<String> grantTypesSupported) {
		this.grantTypesSupported = grantTypesSupported;
	}

	@JsonProperty("subjectTypesSupported")
	public List<String> getSubjectTypesSupported() {
		return subjectTypesSupported;
	}

	@JsonProperty("subjectTypesSupported")
	public void setSubjectTypesSupported(List<String> subjectTypesSupported) {
		this.subjectTypesSupported = subjectTypesSupported;
	}

	@JsonProperty("defaultSubjectType")
	public String getDefaultSubjectType() {
		return defaultSubjectType;
	}

	@JsonProperty("defaultSubjectType")
	public void setDefaultSubjectType(String defaultSubjectType) {
		this.defaultSubjectType = defaultSubjectType;
	}

	@JsonProperty("userInfoSigningAlgValuesSupported")
	public List<String> getUserInfoSigningAlgValuesSupported() {
		return userInfoSigningAlgValuesSupported;
	}

	@JsonProperty("userInfoSigningAlgValuesSupported")
	public void setUserInfoSigningAlgValuesSupported(List<String> userInfoSigningAlgValuesSupported) {
		this.userInfoSigningAlgValuesSupported = userInfoSigningAlgValuesSupported;
	}

	@JsonProperty("userInfoEncryptionAlgValuesSupported")
	public List<String> getUserInfoEncryptionAlgValuesSupported() {
		return userInfoEncryptionAlgValuesSupported;
	}

	@JsonProperty("userInfoEncryptionAlgValuesSupported")
	public void setUserInfoEncryptionAlgValuesSupported(List<String> userInfoEncryptionAlgValuesSupported) {
		this.userInfoEncryptionAlgValuesSupported = userInfoEncryptionAlgValuesSupported;
	}

	@JsonProperty("userInfoEncryptionEncValuesSupported")
	public List<String> getUserInfoEncryptionEncValuesSupported() {
		return userInfoEncryptionEncValuesSupported;
	}

	@JsonProperty("userInfoEncryptionEncValuesSupported")
	public void setUserInfoEncryptionEncValuesSupported(List<String> userInfoEncryptionEncValuesSupported) {
		this.userInfoEncryptionEncValuesSupported = userInfoEncryptionEncValuesSupported;
	}

	@JsonProperty("idTokenSigningAlgValuesSupported")
	public List<String> getIdTokenSigningAlgValuesSupported() {
		return idTokenSigningAlgValuesSupported;
	}

	@JsonProperty("idTokenSigningAlgValuesSupported")
	public void setIdTokenSigningAlgValuesSupported(List<String> idTokenSigningAlgValuesSupported) {
		this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
	}

	@JsonProperty("idTokenEncryptionAlgValuesSupported")
	public List<String> getIdTokenEncryptionAlgValuesSupported() {
		return idTokenEncryptionAlgValuesSupported;
	}

	@JsonProperty("idTokenEncryptionAlgValuesSupported")
	public void setIdTokenEncryptionAlgValuesSupported(List<String> idTokenEncryptionAlgValuesSupported) {
		this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
	}

	@JsonProperty("idTokenEncryptionEncValuesSupported")
	public List<String> getIdTokenEncryptionEncValuesSupported() {
		return idTokenEncryptionEncValuesSupported;
	}

	@JsonProperty("idTokenEncryptionEncValuesSupported")
	public void setIdTokenEncryptionEncValuesSupported(List<String> idTokenEncryptionEncValuesSupported) {
		this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
	}

	@JsonProperty("requestObjectSigningAlgValuesSupported")
	public List<String> getRequestObjectSigningAlgValuesSupported() {
		return requestObjectSigningAlgValuesSupported;
	}

	@JsonProperty("requestObjectSigningAlgValuesSupported")
	public void setRequestObjectSigningAlgValuesSupported(List<String> requestObjectSigningAlgValuesSupported) {
		this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
	}

	@JsonProperty("requestObjectEncryptionAlgValuesSupported")
	public List<String> getRequestObjectEncryptionAlgValuesSupported() {
		return requestObjectEncryptionAlgValuesSupported;
	}

	@JsonProperty("requestObjectEncryptionAlgValuesSupported")
	public void setRequestObjectEncryptionAlgValuesSupported(List<String> requestObjectEncryptionAlgValuesSupported) {
		this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
	}

	@JsonProperty("requestObjectEncryptionEncValuesSupported")
	public List<String> getRequestObjectEncryptionEncValuesSupported() {
		return requestObjectEncryptionEncValuesSupported;
	}

	@JsonProperty("requestObjectEncryptionEncValuesSupported")
	public void setRequestObjectEncryptionEncValuesSupported(List<String> requestObjectEncryptionEncValuesSupported) {
		this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
	}

	@JsonProperty("tokenEndpointAuthMethodsSupported")
	public List<String> getTokenEndpointAuthMethodsSupported() {
		return tokenEndpointAuthMethodsSupported;
	}

	@JsonProperty("tokenEndpointAuthMethodsSupported")
	public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
		this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
	}

	@JsonProperty("tokenEndpointAuthSigningAlgValuesSupported")
	public List<String> getTokenEndpointAuthSigningAlgValuesSupported() {
		return tokenEndpointAuthSigningAlgValuesSupported;
	}

	@JsonProperty("tokenEndpointAuthSigningAlgValuesSupported")
	public void setTokenEndpointAuthSigningAlgValuesSupported(List<String> tokenEndpointAuthSigningAlgValuesSupported) {
		this.tokenEndpointAuthSigningAlgValuesSupported = tokenEndpointAuthSigningAlgValuesSupported;
	}

	@JsonProperty("dynamicRegistrationCustomAttributes")
	public List<String> getDynamicRegistrationCustomAttributes() {
		return dynamicRegistrationCustomAttributes;
	}

	@JsonProperty("dynamicRegistrationCustomAttributes")
	public void setDynamicRegistrationCustomAttributes(List<String> dynamicRegistrationCustomAttributes) {
		this.dynamicRegistrationCustomAttributes = dynamicRegistrationCustomAttributes;
	}

	@JsonProperty("displayValuesSupported")
	public List<String> getDisplayValuesSupported() {
		return displayValuesSupported;
	}

	@JsonProperty("displayValuesSupported")
	public void setDisplayValuesSupported(List<String> displayValuesSupported) {
		this.displayValuesSupported = displayValuesSupported;
	}

	@JsonProperty("claimTypesSupported")
	public List<String> getClaimTypesSupported() {
		return claimTypesSupported;
	}

	@JsonProperty("claimTypesSupported")
	public void setClaimTypesSupported(List<String> claimTypesSupported) {
		this.claimTypesSupported = claimTypesSupported;
	}

	@JsonProperty("serviceDocumentation")
	public String getServiceDocumentation() {
		return serviceDocumentation;
	}

	@JsonProperty("serviceDocumentation")
	public void setServiceDocumentation(String serviceDocumentation) {
		this.serviceDocumentation = serviceDocumentation;
	}

	@JsonProperty("claimsLocalesSupported")
	public List<String> getClaimsLocalesSupported() {
		return claimsLocalesSupported;
	}

	@JsonProperty("claimsLocalesSupported")
	public void setClaimsLocalesSupported(List<String> claimsLocalesSupported) {
		this.claimsLocalesSupported = claimsLocalesSupported;
	}

	@JsonProperty("idTokenTokenBindingCnfValuesSupported")
	public List<String> getIdTokenTokenBindingCnfValuesSupported() {
		return idTokenTokenBindingCnfValuesSupported;
	}

	@JsonProperty("idTokenTokenBindingCnfValuesSupported")
	public void setIdTokenTokenBindingCnfValuesSupported(List<String> idTokenTokenBindingCnfValuesSupported) {
		this.idTokenTokenBindingCnfValuesSupported = idTokenTokenBindingCnfValuesSupported;
	}

	@JsonProperty("uiLocalesSupported")
	public List<String> getUiLocalesSupported() {
		return uiLocalesSupported;
	}

	@JsonProperty("uiLocalesSupported")
	public void setUiLocalesSupported(List<String> uiLocalesSupported) {
		this.uiLocalesSupported = uiLocalesSupported;
	}

	@JsonProperty("dynamicGrantTypeDefault")
	public List<String> getDynamicGrantTypeDefault() {
		return dynamicGrantTypeDefault;
	}

	@JsonProperty("dynamicGrantTypeDefault")
	public void setDynamicGrantTypeDefault(List<String> dynamicGrantTypeDefault) {
		this.dynamicGrantTypeDefault = dynamicGrantTypeDefault;
	}

	@JsonProperty("claimsParameterSupported")
	public Boolean getClaimsParameterSupported() {
		return claimsParameterSupported;
	}

	@JsonProperty("claimsParameterSupported")
	public void setClaimsParameterSupported(Boolean claimsParameterSupported) {
		this.claimsParameterSupported = claimsParameterSupported;
	}

	@JsonProperty("requestParameterSupported")
	public Boolean getRequestParameterSupported() {
		return requestParameterSupported;
	}

	@JsonProperty("requestParameterSupported")
	public void setRequestParameterSupported(Boolean requestParameterSupported) {
		this.requestParameterSupported = requestParameterSupported;
	}

	@JsonProperty("requestUriParameterSupported")
	public Boolean getRequestUriParameterSupported() {
		return requestUriParameterSupported;
	}

	@JsonProperty("requestUriParameterSupported")
	public void setRequestUriParameterSupported(Boolean requestUriParameterSupported) {
		this.requestUriParameterSupported = requestUriParameterSupported;
	}

	@JsonProperty("requireRequestUriRegistration")
	public Boolean getRequireRequestUriRegistration() {
		return requireRequestUriRegistration;
	}

	@JsonProperty("requireRequestUriRegistration")
	public void setRequireRequestUriRegistration(Boolean requireRequestUriRegistration) {
		this.requireRequestUriRegistration = requireRequestUriRegistration;
	}

	@JsonProperty("allowPostLogoutRedirectWithoutValidation")
	public Boolean getAllowPostLogoutRedirectWithoutValidation() {
		return allowPostLogoutRedirectWithoutValidation;
	}

	@JsonProperty("allowPostLogoutRedirectWithoutValidation")
	public void setAllowPostLogoutRedirectWithoutValidation(Boolean allowPostLogoutRedirectWithoutValidation) {
		this.allowPostLogoutRedirectWithoutValidation = allowPostLogoutRedirectWithoutValidation;
	}

	@JsonProperty("introspectionAccessTokenMustHaveUmaProtectionScope")
	public Boolean getIntrospectionAccessTokenMustHaveUmaProtectionScope() {
		return introspectionAccessTokenMustHaveUmaProtectionScope;
	}

	@JsonProperty("introspectionAccessTokenMustHaveUmaProtectionScope")
	public void setIntrospectionAccessTokenMustHaveUmaProtectionScope(
			Boolean introspectionAccessTokenMustHaveUmaProtectionScope) {
		this.introspectionAccessTokenMustHaveUmaProtectionScope = introspectionAccessTokenMustHaveUmaProtectionScope;
	}

	@JsonProperty("opPolicyUri")
	public String getOpPolicyUri() {
		return opPolicyUri;
	}

	@JsonProperty("opPolicyUri")
	public void setOpPolicyUri(String opPolicyUri) {
		this.opPolicyUri = opPolicyUri;
	}

	@JsonProperty("opTosUri")
	public String getOpTosUri() {
		return opTosUri;
	}

	@JsonProperty("opTosUri")
	public void setOpTosUri(String opTosUri) {
		this.opTosUri = opTosUri;
	}

	@JsonProperty("authorizationCodeLifetime")
	public Integer getAuthorizationCodeLifetime() {
		return authorizationCodeLifetime;
	}

	@JsonProperty("authorizationCodeLifetime")
	public void setAuthorizationCodeLifetime(Integer authorizationCodeLifetime) {
		this.authorizationCodeLifetime = authorizationCodeLifetime;
	}

	@JsonProperty("refreshTokenLifetime")
	public Integer getRefreshTokenLifetime() {
		return refreshTokenLifetime;
	}

	@JsonProperty("refreshTokenLifetime")
	public void setRefreshTokenLifetime(Integer refreshTokenLifetime) {
		this.refreshTokenLifetime = refreshTokenLifetime;
	}

	@JsonProperty("idTokenLifetime")
	public Integer getIdTokenLifetime() {
		return idTokenLifetime;
	}

	@JsonProperty("idTokenLifetime")
	public void setIdTokenLifetime(Integer idTokenLifetime) {
		this.idTokenLifetime = idTokenLifetime;
	}

	@JsonProperty("accessTokenLifetime")
	public Integer getAccessTokenLifetime() {
		return accessTokenLifetime;
	}

	@JsonProperty("accessTokenLifetime")
	public void setAccessTokenLifetime(Integer accessTokenLifetime) {
		this.accessTokenLifetime = accessTokenLifetime;
	}

	@JsonProperty("umaResourceLifetime")
	public Integer getUmaResourceLifetime() {
		return umaResourceLifetime;
	}

	@JsonProperty("umaResourceLifetime")
	public void setUmaResourceLifetime(Integer umaResourceLifetime) {
		this.umaResourceLifetime = umaResourceLifetime;
	}

	@JsonProperty("sessionAsJwt")
	public Boolean getSessionAsJwt() {
		return sessionAsJwt;
	}

	@JsonProperty("sessionAsJwt")
	public void setSessionAsJwt(Boolean sessionAsJwt) {
		this.sessionAsJwt = sessionAsJwt;
	}

	@JsonProperty("umaRptLifetime")
	public Integer getUmaRptLifetime() {
		return umaRptLifetime;
	}

	@JsonProperty("umaRptLifetime")
	public void setUmaRptLifetime(Integer umaRptLifetime) {
		this.umaRptLifetime = umaRptLifetime;
	}

	@JsonProperty("umaTicketLifetime")
	public Integer getUmaTicketLifetime() {
		return umaTicketLifetime;
	}

	@JsonProperty("umaTicketLifetime")
	public void setUmaTicketLifetime(Integer umaTicketLifetime) {
		this.umaTicketLifetime = umaTicketLifetime;
	}

	@JsonProperty("umaPctLifetime")
	public Integer getUmaPctLifetime() {
		return umaPctLifetime;
	}

	@JsonProperty("umaPctLifetime")
	public void setUmaPctLifetime(Integer umaPctLifetime) {
		this.umaPctLifetime = umaPctLifetime;
	}

	@JsonProperty("umaAddScopesAutomatically")
	public Boolean getUmaAddScopesAutomatically() {
		return umaAddScopesAutomatically;
	}

	@JsonProperty("umaAddScopesAutomatically")
	public void setUmaAddScopesAutomatically(Boolean umaAddScopesAutomatically) {
		this.umaAddScopesAutomatically = umaAddScopesAutomatically;
	}

	@JsonProperty("umaValidateClaimToken")
	public Boolean getUmaValidateClaimToken() {
		return umaValidateClaimToken;
	}

	@JsonProperty("umaValidateClaimToken")
	public void setUmaValidateClaimToken(Boolean umaValidateClaimToken) {
		this.umaValidateClaimToken = umaValidateClaimToken;
	}

	@JsonProperty("umaGrantAccessIfNoPolicies")
	public Boolean getUmaGrantAccessIfNoPolicies() {
		return umaGrantAccessIfNoPolicies;
	}

	@JsonProperty("umaGrantAccessIfNoPolicies")
	public void setUmaGrantAccessIfNoPolicies(Boolean umaGrantAccessIfNoPolicies) {
		this.umaGrantAccessIfNoPolicies = umaGrantAccessIfNoPolicies;
	}

	@JsonProperty("umaRestrictResourceToAssociatedClient")
	public Boolean getUmaRestrictResourceToAssociatedClient() {
		return umaRestrictResourceToAssociatedClient;
	}

	@JsonProperty("umaRestrictResourceToAssociatedClient")
	public void setUmaRestrictResourceToAssociatedClient(Boolean umaRestrictResourceToAssociatedClient) {
		this.umaRestrictResourceToAssociatedClient = umaRestrictResourceToAssociatedClient;
	}

	@JsonProperty("umaKeepClientDuringResourceSetRegistration")
	public Boolean getUmaKeepClientDuringResourceSetRegistration() {
		return umaKeepClientDuringResourceSetRegistration;
	}

	@JsonProperty("umaKeepClientDuringResourceSetRegistration")
	public void setUmaKeepClientDuringResourceSetRegistration(Boolean umaKeepClientDuringResourceSetRegistration) {
		this.umaKeepClientDuringResourceSetRegistration = umaKeepClientDuringResourceSetRegistration;
	}

	@JsonProperty("umaRptAsJwt")
	public Boolean getUmaRptAsJwt() {
		return umaRptAsJwt;
	}

	@JsonProperty("umaRptAsJwt")
	public void setUmaRptAsJwt(Boolean umaRptAsJwt) {
		this.umaRptAsJwt = umaRptAsJwt;
	}

	@JsonProperty("cleanServiceInterval")
	public Integer getCleanServiceInterval() {
		return cleanServiceInterval;
	}

	@JsonProperty("cleanServiceInterval")
	public void setCleanServiceInterval(Integer cleanServiceInterval) {
		this.cleanServiceInterval = cleanServiceInterval;
	}

	@JsonProperty("keyRegenerationEnabled")
	public Boolean getKeyRegenerationEnabled() {
		return keyRegenerationEnabled;
	}

	@JsonProperty("keyRegenerationEnabled")
	public void setKeyRegenerationEnabled(Boolean keyRegenerationEnabled) {
		this.keyRegenerationEnabled = keyRegenerationEnabled;
	}

	@JsonProperty("keyRegenerationInterval")
	public Integer getKeyRegenerationInterval() {
		return keyRegenerationInterval;
	}

	@JsonProperty("keyRegenerationInterval")
	public void setKeyRegenerationInterval(Integer keyRegenerationInterval) {
		this.keyRegenerationInterval = keyRegenerationInterval;
	}

	@JsonProperty("defaultSignatureAlgorithm")
	public String getDefaultSignatureAlgorithm() {
		return defaultSignatureAlgorithm;
	}

	@JsonProperty("defaultSignatureAlgorithm")
	public void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm) {
		this.defaultSignatureAlgorithm = defaultSignatureAlgorithm;
	}

	@JsonProperty("oxOpenIdConnectVersion")
	public String getOxOpenIdConnectVersion() {
		return oxOpenIdConnectVersion;
	}

	@JsonProperty("oxOpenIdConnectVersion")
	public void setOxOpenIdConnectVersion(String oxOpenIdConnectVersion) {
		this.oxOpenIdConnectVersion = oxOpenIdConnectVersion;
	}

	@JsonProperty("organizationInum")
	public String getOrganizationInum() {
		return organizationInum;
	}

	@JsonProperty("organizationInum")
	public void setOrganizationInum(String organizationInum) {
		this.organizationInum = organizationInum;
	}

	@JsonProperty("oxId")
	public String getOxId() {
		return oxId;
	}

	@JsonProperty("oxId")
	public void setOxId(String oxId) {
		this.oxId = oxId;
	}

	@JsonProperty("dynamicRegistrationEnabled")
	public Boolean getDynamicRegistrationEnabled() {
		return dynamicRegistrationEnabled;
	}

	@JsonProperty("dynamicRegistrationEnabled")
	public void setDynamicRegistrationEnabled(Boolean dynamicRegistrationEnabled) {
		this.dynamicRegistrationEnabled = dynamicRegistrationEnabled;
	}

	@JsonProperty("dynamicRegistrationExpirationTime")
	public Integer getDynamicRegistrationExpirationTime() {
		return dynamicRegistrationExpirationTime;
	}

	@JsonProperty("dynamicRegistrationExpirationTime")
	public void setDynamicRegistrationExpirationTime(Integer dynamicRegistrationExpirationTime) {
		this.dynamicRegistrationExpirationTime = dynamicRegistrationExpirationTime;
	}

	@JsonProperty("dynamicRegistrationPersistClientAuthorizations")
	public Boolean getDynamicRegistrationPersistClientAuthorizations() {
		return dynamicRegistrationPersistClientAuthorizations;
	}

	@JsonProperty("dynamicRegistrationPersistClientAuthorizations")
	public void setDynamicRegistrationPersistClientAuthorizations(
			Boolean dynamicRegistrationPersistClientAuthorizations) {
		this.dynamicRegistrationPersistClientAuthorizations = dynamicRegistrationPersistClientAuthorizations;
	}

	@JsonProperty("trustedClientEnabled")
	public Boolean getTrustedClientEnabled() {
		return trustedClientEnabled;
	}

	@JsonProperty("trustedClientEnabled")
	public void setTrustedClientEnabled(Boolean trustedClientEnabled) {
		this.trustedClientEnabled = trustedClientEnabled;
	}

	@JsonProperty("skipAuthorizationForOpenIdScopeAndPairwiseId")
	public Boolean getSkipAuthorizationForOpenIdScopeAndPairwiseId() {
		return skipAuthorizationForOpenIdScopeAndPairwiseId;
	}

	@JsonProperty("skipAuthorizationForOpenIdScopeAndPairwiseId")
	public void setSkipAuthorizationForOpenIdScopeAndPairwiseId(Boolean skipAuthorizationForOpenIdScopeAndPairwiseId) {
		this.skipAuthorizationForOpenIdScopeAndPairwiseId = skipAuthorizationForOpenIdScopeAndPairwiseId;
	}

	@JsonProperty("dynamicRegistrationScopesParamEnabled")
	public Boolean getDynamicRegistrationScopesParamEnabled() {
		return dynamicRegistrationScopesParamEnabled;
	}

	@JsonProperty("dynamicRegistrationScopesParamEnabled")
	public void setDynamicRegistrationScopesParamEnabled(Boolean dynamicRegistrationScopesParamEnabled) {
		this.dynamicRegistrationScopesParamEnabled = dynamicRegistrationScopesParamEnabled;
	}

	@JsonProperty("dynamicRegistrationCustomObjectClass")
	public String getDynamicRegistrationCustomObjectClass() {
		return dynamicRegistrationCustomObjectClass;
	}

	@JsonProperty("dynamicRegistrationCustomObjectClass")
	public void setDynamicRegistrationCustomObjectClass(String dynamicRegistrationCustomObjectClass) {
		this.dynamicRegistrationCustomObjectClass = dynamicRegistrationCustomObjectClass;
	}

	@JsonProperty("personCustomObjectClassList")
	public List<String> getPersonCustomObjectClassList() {
		return personCustomObjectClassList;
	}

	@JsonProperty("personCustomObjectClassList")
	public void setPersonCustomObjectClassList(List<String> personCustomObjectClassList) {
		this.personCustomObjectClassList = personCustomObjectClassList;
	}

	@JsonProperty("persistIdTokenInLdap")
	public Boolean getPersistIdTokenInLdap() {
		return persistIdTokenInLdap;
	}

	@JsonProperty("persistIdTokenInLdap")
	public void setPersistIdTokenInLdap(Boolean persistIdTokenInLdap) {
		this.persistIdTokenInLdap = persistIdTokenInLdap;
	}

	@JsonProperty("persistRefreshTokenInLdap")
	public Boolean getPersistRefreshTokenInLdap() {
		return persistRefreshTokenInLdap;
	}

	@JsonProperty("persistRefreshTokenInLdap")
	public void setPersistRefreshTokenInLdap(Boolean persistRefreshTokenInLdap) {
		this.persistRefreshTokenInLdap = persistRefreshTokenInLdap;
	}

	@JsonProperty("authenticationFiltersEnabled")
	public Boolean getAuthenticationFiltersEnabled() {
		return authenticationFiltersEnabled;
	}

	@JsonProperty("authenticationFiltersEnabled")
	public void setAuthenticationFiltersEnabled(Boolean authenticationFiltersEnabled) {
		this.authenticationFiltersEnabled = authenticationFiltersEnabled;
	}

	@JsonProperty("invalidateSessionCookiesAfterAuthorizationFlow")
	public Boolean getInvalidateSessionCookiesAfterAuthorizationFlow() {
		return invalidateSessionCookiesAfterAuthorizationFlow;
	}

	@JsonProperty("invalidateSessionCookiesAfterAuthorizationFlow")
	public void setInvalidateSessionCookiesAfterAuthorizationFlow(
			Boolean invalidateSessionCookiesAfterAuthorizationFlow) {
		this.invalidateSessionCookiesAfterAuthorizationFlow = invalidateSessionCookiesAfterAuthorizationFlow;
	}

	@JsonProperty("clientAuthenticationFiltersEnabled")
	public Boolean getClientAuthenticationFiltersEnabled() {
		return clientAuthenticationFiltersEnabled;
	}

	@JsonProperty("clientAuthenticationFiltersEnabled")
	public void setClientAuthenticationFiltersEnabled(Boolean clientAuthenticationFiltersEnabled) {
		this.clientAuthenticationFiltersEnabled = clientAuthenticationFiltersEnabled;
	}

	@JsonProperty("authenticationFilters")
	public List<AuthenticationFilter> getAuthenticationFilters() {
		return authenticationFilters;
	}

	@JsonProperty("authenticationFilters")
	public void setAuthenticationFilters(List<AuthenticationFilter> authenticationFilters) {
		this.authenticationFilters = authenticationFilters;
	}

	@JsonProperty("clientAuthenticationFilters")
	public List<ClientAuthenticationFilter> getClientAuthenticationFilters() {
		return clientAuthenticationFilters;
	}

	@JsonProperty("clientAuthenticationFilters")
	public void setClientAuthenticationFilters(List<ClientAuthenticationFilter> clientAuthenticationFilters) {
		this.clientAuthenticationFilters = clientAuthenticationFilters;
	}

	@JsonProperty("configurationInum")
	public String getConfigurationInum() {
		return configurationInum;
	}

	@JsonProperty("configurationInum")
	public void setConfigurationInum(String configurationInum) {
		this.configurationInum = configurationInum;
	}

	@JsonProperty("sessionIdUnusedLifetime")
	public Integer getSessionIdUnusedLifetime() {
		return sessionIdUnusedLifetime;
	}

	@JsonProperty("sessionIdUnusedLifetime")
	public void setSessionIdUnusedLifetime(Integer sessionIdUnusedLifetime) {
		this.sessionIdUnusedLifetime = sessionIdUnusedLifetime;
	}

	@JsonProperty("sessionIdUnauthenticatedUnusedLifetime")
	public Integer getSessionIdUnauthenticatedUnusedLifetime() {
		return sessionIdUnauthenticatedUnusedLifetime;
	}

	@JsonProperty("sessionIdUnauthenticatedUnusedLifetime")
	public void setSessionIdUnauthenticatedUnusedLifetime(Integer sessionIdUnauthenticatedUnusedLifetime) {
		this.sessionIdUnauthenticatedUnusedLifetime = sessionIdUnauthenticatedUnusedLifetime;
	}

	@JsonProperty("sessionIdEnabled")
	public Boolean getSessionIdEnabled() {
		return sessionIdEnabled;
	}

	@JsonProperty("sessionIdEnabled")
	public void setSessionIdEnabled(Boolean sessionIdEnabled) {
		this.sessionIdEnabled = sessionIdEnabled;
	}

	@JsonProperty("sessionIdPersistOnPromptNone")
	public Boolean getSessionIdPersistOnPromptNone() {
		return sessionIdPersistOnPromptNone;
	}

	@JsonProperty("sessionIdPersistOnPromptNone")
	public void setSessionIdPersistOnPromptNone(Boolean sessionIdPersistOnPromptNone) {
		this.sessionIdPersistOnPromptNone = sessionIdPersistOnPromptNone;
	}

	@JsonProperty("sessionIdLifetime")
	public Integer getSessionIdLifetime() {
		return sessionIdLifetime;
	}

	@JsonProperty("sessionIdLifetime")
	public void setSessionIdLifetime(Integer sessionIdLifetime) {
		this.sessionIdLifetime = sessionIdLifetime;
	}

    @JsonProperty("configurationUpdateInterval")
	public Integer getConfigurationUpdateInterval() {
		return configurationUpdateInterval;
	}

	@JsonProperty("configurationUpdateInterval")
	public void setConfigurationUpdateInterval(Integer configurationUpdateInterval) {
		this.configurationUpdateInterval = configurationUpdateInterval;
	}

	@JsonProperty("cssLocation")
	public String getCssLocation() {
		return cssLocation;
	}

	@JsonProperty("cssLocation")
	public void setCssLocation(String cssLocation) {
		this.cssLocation = cssLocation;
	}

	@JsonProperty("jsLocation")
	public String getJsLocation() {
		return jsLocation;
	}

	@JsonProperty("jsLocation")
	public void setJsLocation(String jsLocation) {
		this.jsLocation = jsLocation;
	}

	@JsonProperty("imgLocation")
	public String getImgLocation() {
		return imgLocation;
	}

	@JsonProperty("imgLocation")
	public void setImgLocation(String imgLocation) {
		this.imgLocation = imgLocation;
	}

	@JsonProperty("metricReporterInterval")
	public Integer getMetricReporterInterval() {
		return metricReporterInterval;
	}

	@JsonProperty("metricReporterInterval")
	public void setMetricReporterInterval(Integer metricReporterInterval) {
		this.metricReporterInterval = metricReporterInterval;
	}

	@JsonProperty("metricReporterKeepDataDays")
	public Integer getMetricReporterKeepDataDays() {
		return metricReporterKeepDataDays;
	}

	@JsonProperty("metricReporterKeepDataDays")
	public void setMetricReporterKeepDataDays(Integer metricReporterKeepDataDays) {
		this.metricReporterKeepDataDays = metricReporterKeepDataDays;
	}

	@JsonProperty("pairwiseIdType")
	public String getPairwiseIdType() {
		return pairwiseIdType;
	}

	@JsonProperty("pairwiseIdType")
	public void setPairwiseIdType(String pairwiseIdType) {
		this.pairwiseIdType = pairwiseIdType;
	}

	@JsonProperty("pairwiseCalculationKey")
	public String getPairwiseCalculationKey() {
		return pairwiseCalculationKey;
	}

	@JsonProperty("pairwiseCalculationKey")
	public void setPairwiseCalculationKey(String pairwiseCalculationKey) {
		this.pairwiseCalculationKey = pairwiseCalculationKey;
	}

	@JsonProperty("pairwiseCalculationSalt")
	public String getPairwiseCalculationSalt() {
		return pairwiseCalculationSalt;
	}

	@JsonProperty("pairwiseCalculationSalt")
	public void setPairwiseCalculationSalt(String pairwiseCalculationSalt) {
		this.pairwiseCalculationSalt = pairwiseCalculationSalt;
	}

	@JsonProperty("shareSubjectIdBetweenClientsWithSameSectorId")
	public Boolean getShareSubjectIdBetweenClientsWithSameSectorId() {
		return shareSubjectIdBetweenClientsWithSameSectorId;
	}

	@JsonProperty("shareSubjectIdBetweenClientsWithSameSectorId")
	public void setShareSubjectIdBetweenClientsWithSameSectorId(Boolean shareSubjectIdBetweenClientsWithSameSectorId) {
		this.shareSubjectIdBetweenClientsWithSameSectorId = shareSubjectIdBetweenClientsWithSameSectorId;
	}

	@JsonProperty("webKeysStorage")
	public String getWebKeysStorage() {
		return webKeysStorage;
	}

	@JsonProperty("webKeysStorage")
	public void setWebKeysStorage(String webKeysStorage) {
		this.webKeysStorage = webKeysStorage;
	}

	@JsonProperty("dnName")
	public String getDnName() {
		return dnName;
	}

	@JsonProperty("dnName")
	public void setDnName(String dnName) {
		this.dnName = dnName;
	}

	@JsonProperty("keyStoreFile")
	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	@JsonProperty("keyStoreFile")
	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	@JsonProperty("keyStoreSecret")
	public String getKeyStoreSecret() {
		return keyStoreSecret;
	}

	@JsonProperty("keyStoreSecret")
	public void setKeyStoreSecret(String keyStoreSecret) {
		this.keyStoreSecret = keyStoreSecret;
	}

	@JsonProperty("endSessionWithAccessToken")
	public Boolean getEndSessionWithAccessToken() {
		return endSessionWithAccessToken;
	}

	@JsonProperty("endSessionWithAccessToken")
	public void setEndSessionWithAccessToken(Boolean endSessionWithAccessToken) {
		this.endSessionWithAccessToken = endSessionWithAccessToken;
	}

    public String getCookieDomain() {
		return cookieDomain;
	}

	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}

	@JsonProperty("clientWhiteList")
	public List<String> getClientWhiteList() {
		return clientWhiteList;
	}

	@JsonProperty("clientWhiteList")
	public void setClientWhiteList(List<String> clientWhiteList) {
		this.clientWhiteList = clientWhiteList;
	}

	@JsonProperty("clientBlackList")
	public List<String> getClientBlackList() {
		return clientBlackList;
	}

	@JsonProperty("clientBlackList")
	public void setClientBlackList(List<String> clientBlackList) {
		this.clientBlackList = clientBlackList;
	}

	@JsonProperty("legacyIdTokenClaims")
	public Boolean getLegacyIdTokenClaims() {
		return legacyIdTokenClaims;
	}

	@JsonProperty("legacyIdTokenClaims")
	public void setLegacyIdTokenClaims(Boolean legacyIdTokenClaims) {
		this.legacyIdTokenClaims = legacyIdTokenClaims;
	}

	@JsonProperty("customHeadersWithAuthorizationResponse")
	public Boolean getCustomHeadersWithAuthorizationResponse() {
		return customHeadersWithAuthorizationResponse;
	}

	@JsonProperty("customHeadersWithAuthorizationResponse")
	public void setCustomHeadersWithAuthorizationResponse(Boolean customHeadersWithAuthorizationResponse) {
		this.customHeadersWithAuthorizationResponse = customHeadersWithAuthorizationResponse;
	}

	@JsonProperty("frontChannelLogoutSessionSupported")
	public Boolean getFrontChannelLogoutSessionSupported() {
		return frontChannelLogoutSessionSupported;
	}

	@JsonProperty("frontChannelLogoutSessionSupported")
	public void setFrontChannelLogoutSessionSupported(Boolean frontChannelLogoutSessionSupported) {
		this.frontChannelLogoutSessionSupported = frontChannelLogoutSessionSupported;
	}

	@JsonProperty("updateUserLastLogonTime")
	public Boolean getUpdateUserLastLogonTime() {
		return updateUserLastLogonTime;
	}

	@JsonProperty("updateUserLastLogonTime")
	public void setUpdateUserLastLogonTime(Boolean updateUserLastLogonTime) {
		this.updateUserLastLogonTime = updateUserLastLogonTime;
	}

	@JsonProperty("updateClientAccessTime")
	public Boolean getUpdateClientAccessTime() {
		return updateClientAccessTime;
	}

	@JsonProperty("updateClientAccessTime")
	public void setUpdateClientAccessTime(Boolean updateClientAccessTime) {
		this.updateClientAccessTime = updateClientAccessTime;
	}

	@JsonProperty("enableClientGrantTypeUpdate")
	public Boolean getEnableClientGrantTypeUpdate() {
		return enableClientGrantTypeUpdate;
	}

	@JsonProperty("enableClientGrantTypeUpdate")
	public void setEnableClientGrantTypeUpdate(Boolean enableClientGrantTypeUpdate) {
		this.enableClientGrantTypeUpdate = enableClientGrantTypeUpdate;
	}

	@JsonProperty("corsConfigurationFilters")
	public List<CorsConfigurationFilter> getCorsConfigurationFilters() {
		return corsConfigurationFilters;
	}

	@JsonProperty("corsConfigurationFilters")
	public void setCorsConfigurationFilters(List<CorsConfigurationFilter> corsConfigurationFilters) {
		this.corsConfigurationFilters = corsConfigurationFilters;
	}

	@JsonProperty("logClientIdOnClientAuthentication")
	public Boolean getLogClientIdOnClientAuthentication() {
		return logClientIdOnClientAuthentication;
	}

	@JsonProperty("logClientIdOnClientAuthentication")
	public void setLogClientIdOnClientAuthentication(Boolean logClientIdOnClientAuthentication) {
		this.logClientIdOnClientAuthentication = logClientIdOnClientAuthentication;
	}

	@JsonProperty("logClientNameOnClientAuthentication")
	public Boolean getLogClientNameOnClientAuthentication() {
		return logClientNameOnClientAuthentication;
	}

	@JsonProperty("logClientNameOnClientAuthentication")
	public void setLogClientNameOnClientAuthentication(Boolean logClientNameOnClientAuthentication) {
		this.logClientNameOnClientAuthentication = logClientNameOnClientAuthentication;
	}

	@JsonProperty("httpLoggingEnabled")
	public Boolean getHttpLoggingEnabled() {
		return httpLoggingEnabled;
	}

	@JsonProperty("httpLoggingEnabled")
	public void setHttpLoggingEnabled(Boolean httpLoggingEnabled) {
		this.httpLoggingEnabled = httpLoggingEnabled;
	}

	@JsonProperty("httpLoggingExludePaths")
	public List<Object> getHttpLoggingExludePaths() {
		return httpLoggingExludePaths;
	}

	@JsonProperty("httpLoggingExludePaths")
	public void setHttpLoggingExludePaths(List<Object> httpLoggingExludePaths) {
		this.httpLoggingExludePaths = httpLoggingExludePaths;
	}

	@JsonProperty("externalLoggerConfiguration")
	public String getExternalLoggerConfiguration() {
		return externalLoggerConfiguration;
	}

	@JsonProperty("externalLoggerConfiguration")
	public void setExternalLoggerConfiguration(String externalLoggerConfiguration) {
		this.externalLoggerConfiguration = externalLoggerConfiguration;
	}

	@JsonProperty("authorizationRequestCustomAllowedParameters")
	public List<String> getAuthorizationRequestCustomAllowedParameters() {
		return authorizationRequestCustomAllowedParameters;
	}

	@JsonProperty("authorizationRequestCustomAllowedParameters")
	public void setAuthorizationRequestCustomAllowedParameters(
			List<String> authorizationRequestCustomAllowedParameters) {
		this.authorizationRequestCustomAllowedParameters = authorizationRequestCustomAllowedParameters;
	}

	@JsonProperty("legacyDynamicRegistrationScopeParam")
	public Boolean getLegacyDynamicRegistrationScopeParam() {
		return legacyDynamicRegistrationScopeParam;
	}

	@JsonProperty("legacyDynamicRegistrationScopeParam")
	public void setLegacyDynamicRegistrationScopeParam(Boolean legacyDynamicRegistrationScopeParam) {
		this.legacyDynamicRegistrationScopeParam = legacyDynamicRegistrationScopeParam;
	}

	@JsonProperty("openidScopeBackwardCompatibility")
	public Boolean getOpenidScopeBackwardCompatibility() {
		return openidScopeBackwardCompatibility;
	}

	@JsonProperty("openidScopeBackwardCompatibility")
	public void setOpenidScopeBackwardCompatibility(Boolean openidScopeBackwardCompatibility) {
		this.openidScopeBackwardCompatibility = openidScopeBackwardCompatibility;
	}

	@JsonProperty("useCacheForAllImplicitFlowObjects")
	public Boolean getUseCacheForAllImplicitFlowObjects() {
		return useCacheForAllImplicitFlowObjects;
	}

	@JsonProperty("useCacheForAllImplicitFlowObjects")
	public void setUseCacheForAllImplicitFlowObjects(Boolean useCacheForAllImplicitFlowObjects) {
		this.useCacheForAllImplicitFlowObjects = useCacheForAllImplicitFlowObjects;
	}

	@JsonProperty("disableU2fEndpoint")
	public Boolean getDisableU2fEndpoint() {
		return disableU2fEndpoint;
	}

	@JsonProperty("disableU2fEndpoint")
	public void setDisableU2fEndpoint(Boolean disableU2fEndpoint) {
		this.disableU2fEndpoint = disableU2fEndpoint;
	}

	@JsonProperty("authenticationProtectionConfiguration")
	public AuthenticationProtectionConfiguration getAuthenticationProtectionConfiguration() {
		return authenticationProtectionConfiguration;
	}

	@JsonProperty("authenticationProtectionConfiguration")
	public void setAuthenticationProtectionConfiguration(
			AuthenticationProtectionConfiguration authenticationProtectionConfiguration) {
		this.authenticationProtectionConfiguration = authenticationProtectionConfiguration;
	}

	@JsonProperty("fido2Configuration")
	public Fido2Configuration getFido2Configuration() {
		return fido2Configuration;
	}

	@JsonProperty("fido2Configuration")
	public void setFido2Configuration(Fido2Configuration fido2Configuration) {
		this.fido2Configuration = fido2Configuration;
	}

	@JsonProperty("loggingLevel")
	public String getLoggingLevel() {
		return loggingLevel;
	}

	@JsonProperty("loggingLevel")
	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	@JsonProperty("errorHandlingMethod")
	public String getErrorHandlingMethod() {
		return errorHandlingMethod;
	}

	@JsonProperty("errorHandlingMethod")
	public void setErrorHandlingMethod(String errorHandlingMethod) {
		this.errorHandlingMethod = errorHandlingMethod;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}