package org.gluu.oxtrust.api.configuration;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public class OxAuthConfig {

    static final String URL_PATTERN = "^(ftp|http|https):\\/\\/[^ \"]+$";

    private boolean persistIdTokenInLdap;

    private String dnName;

    @Required
    private List<String> tokenEndpointAuthSigningAlgValuesSupported;

    private boolean sessionIdPersistOnPromptNone;

    @Required
    private List<AuthenticationFilters> authenticationFilters;

    private int metricReporterKeepDataDays;

    private boolean customHeadersWithAuthorizationResponse;

    @Required
    private String defaultSignatureAlgorithm;

    @Required
    @Size(min = 1)
    private String openidSubAttribute;

    private int accessTokenLifetime;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String openIdDiscoveryEndpoint;

    private int authorizationCodeLifetime;

    @Required
    private List<String> claimTypesSupported;

    private boolean umaResourceLifetime;

    @Required
    @NotNull
    @Pattern(regexp = URL_PATTERN)
    private String oxId;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String oxElevenDeleteKeyEndpoint;

    private PairwiseIdType pairwiseIdType;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String clientInfoEndpoint;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String userInfoEndpoint;
    @Required
    private List<String> idTokenSigningAlgValuesSupported;

    @Required
    private String jsLocation;

    @Required
    private List<CorsConfigurationFilters> corsConfigurationFilters;

    private int metricReporterInterval;

    @Required
    private List<String> dynamicRegistrationCustomAttributes;

    private boolean requestUriParameterSupported;

    private int sessionIdUnauthenticatedUnusedLifetime;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String oxElevenSignEndpoint;

    @Required
    private String cssLocation;

    private boolean logClientNameOnClientAuthentication;

    private boolean updateClientAccessTime;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String openIdConfigurationEndpoint;

    private boolean updateUserLastLogonTime;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String endSessionEndpoint;

    private boolean trustedClientEnabled;

    @Required
    private List<String> clientWhiteList;

    @Required
    private List<String> claimsLocalesSupported;

    @Required
    private List<String> userInfoEncryptionAlgValuesSupported;

    @Required
    private Set<Set<ResponseTypeApi>> responseTypesSupported;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String oxElevenGenerateKeyEndpoint;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String loginPage;

    private boolean skipAuthorizationForOpenIdScopeAndPairwiseId;

    private int dynamicRegistrationExpirationTime;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String jwksUri;

    @Required
    @NotNull
    @Size(min = 1)
    private String applianceInum;

    private boolean persistRefreshTokenInLdap;

    @Required
    @NotNull
    @Size(min = 1)
    private String pairwiseCalculationKey;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String registrationEndpoint;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String idGenerationEndpoint;

    @Required
    @NotNull
    @Size(min = 1)
    private String keyStoreFile;

    @Required
    private List<String> idTokenEncryptionAlgValuesSupported;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String checkSessionIFrame;

    @Required
    private List<ClientAuthenticationFilters> clientAuthenticationFilters;
    @Required
    private String defaultSubjectType;

    private int idTokenLifetime;
    @Required
    private List<String> idTokenEncryptionEncValuesSupported;

    private boolean enableClientGrantTypeUpdate;

    @Required
    @NotNull
    @Size(min = 1)
    private String keyStoreSecret;

    @Required
    private String externalLoggerConfiguration;

    private int configurationUpdateInterval;

    private WebKeyStorageApi webKeysStorage;

    private boolean endSessionWithAccessToken;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String tokenEndpoint;

    private boolean dynamicRegistrationScopesParamEnabled;

    private String umaRequesterPermissionTokenLifetime;

    @Required
    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String opPolicyUri;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String authorizationPage;

    private boolean legacyDynamicRegistrationScopeParam;

    @Required
    private String dynamicRegistrationCustomObjectClass;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String oxElevenVerifySignatureEndpoint;

    private boolean keyRegenerationEnabled;

    private boolean clientAuthenticationFiltersEnabled;

    private boolean httpLoggingEnabled;

    @Required
    private List<String> clientBlackList;

    private int sessionIdUnusedLifetime;

    @Required
    private List<String> tokenEndpointAuthMethodsSupported;

    private boolean sessionAsJwt;

    private boolean claimsParameterSupported;

    @Required
    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String opTosUri;

    @Required
    private List<String> requestObjectEncryptionAlgValuesSupported;

    private boolean requestParameterSupported;

    @Required
    @NotNull
    @Size(min = 1)
    private String organizationInum;

    @Required
    private Set<String> authorizationRequestCustomAllowedParameters;

    @Required
    @NotNull
    @Size(min = 1)
    private String oxOpenIdConnectVersion;
    @Required
    private List<String> userInfoEncryptionEncValuesSupported;

    private boolean dynamicRegistrationEnabled;

    @Required
    private List<String> requestObjectSigningAlgValuesSupported;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String umaConfigurationEndpoint;

    private int cleanServiceInterval;

    @Required
    private Set<String> dynamicGrantTypeDefault;

    @Required
    private List<String> subjectTypesSupported;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String baseEndpoint;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String issuer;

    private int refreshTokenLifetime;

    private boolean useCacheForAllImplicitFlowObjects;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String introspectionEndpoint;

    @Required
    private String imgLocation;

    private boolean sessionIdEnabled;

    private boolean dynamicRegistrationPersistClientAuthorizations;

    @Required
    private Set<String> httpLoggingExludePaths;

    @Required
    private Set<String> grantTypesSupported;

    @Required
    @Size(min = 1)
    private String oxElevenJwksEndpoint;

    private boolean umaRptAsJwt;

    private boolean logClientIdOnClientAuthentication;

    @Required
    private List<String> userInfoSigningAlgValuesSupported;

    private int sessionIdLifetime;

    @Required
    private List<String> personCustomObjectClassList;

    @Required
    private List<String> uiLocalesSupported;

    @Required
    @Size(min = 1)
    private String pairwiseCalculationSalt;

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String authorizationEndpoint;

    private boolean umaValidateClaimToken;

    private boolean umaAddScopesAutomatically;

    private boolean umaGrantAccessIfNoPolicies;

    @Required
    private List<String> requestObjectEncryptionEncValuesSupported;

    private boolean legacyIdTokenClaims;

    private boolean umaKeepClientDuringResourceSetRegistration;

    private boolean requireRequestUriRegistration;

    private boolean frontChannelLogoutSessionSupported;

    @Required
    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String serviceDocumentation;

    private int keyRegenerationInterval;
    @Required
    private List<String> displayValuesSupported;

    @Required
    @Size(min = 1)
    private String sectorIdentifierEndpoint;

    private boolean authenticationFiltersEnabled;

    public boolean isPersistIdTokenInLdap() {
        return persistIdTokenInLdap;
    }

    public void setPersistIdTokenInLdap(boolean persistIdTokenInLdap) {
        this.persistIdTokenInLdap = persistIdTokenInLdap;
    }

    public String getDnName() {
        return dnName;
    }

    public void setDnName(String dnName) {
        this.dnName = dnName;
    }

    public List<String> getTokenEndpointAuthSigningAlgValuesSupported() {
        return tokenEndpointAuthSigningAlgValuesSupported;
    }

    public void setTokenEndpointAuthSigningAlgValuesSupported(List<String> tokenEndpointAuthSigningAlgValuesSupported) {
        this.tokenEndpointAuthSigningAlgValuesSupported = tokenEndpointAuthSigningAlgValuesSupported;
    }

    public boolean isSessionIdPersistOnPromptNone() {
        return sessionIdPersistOnPromptNone;
    }

    public void setSessionIdPersistOnPromptNone(boolean sessionIdPersistOnPromptNone) {
        this.sessionIdPersistOnPromptNone = sessionIdPersistOnPromptNone;
    }

    public List<AuthenticationFilters> getAuthenticationFilters() {
        return authenticationFilters;
    }

    public void setAuthenticationFilters(List<AuthenticationFilters> authenticationFilters) {
        this.authenticationFilters = authenticationFilters;
    }

    public int getMetricReporterKeepDataDays() {
        return metricReporterKeepDataDays;
    }

    public void setMetricReporterKeepDataDays(int metricReporterKeepDataDays) {
        this.metricReporterKeepDataDays = metricReporterKeepDataDays;
    }

    public boolean isCustomHeadersWithAuthorizationResponse() {
        return customHeadersWithAuthorizationResponse;
    }

    public void setCustomHeadersWithAuthorizationResponse(boolean customHeadersWithAuthorizationResponse) {
        this.customHeadersWithAuthorizationResponse = customHeadersWithAuthorizationResponse;
    }

    public String getDefaultSignatureAlgorithm() {
        return defaultSignatureAlgorithm;
    }

    public void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm) {
        this.defaultSignatureAlgorithm = defaultSignatureAlgorithm;
    }

    public String getOpenidSubAttribute() {
        return openidSubAttribute;
    }

    public void setOpenidSubAttribute(String openidSubAttribute) {
        this.openidSubAttribute = openidSubAttribute;
    }

    public int getAccessTokenLifetime() {
        return accessTokenLifetime;
    }

    public void setAccessTokenLifetime(int accessTokenLifetime) {
        this.accessTokenLifetime = accessTokenLifetime;
    }

    public String getOpenIdDiscoveryEndpoint() {
        return openIdDiscoveryEndpoint;
    }

    public void setOpenIdDiscoveryEndpoint(String openIdDiscoveryEndpoint) {
        this.openIdDiscoveryEndpoint = openIdDiscoveryEndpoint;
    }

    public int getAuthorizationCodeLifetime() {
        return authorizationCodeLifetime;
    }

    public void setAuthorizationCodeLifetime(int authorizationCodeLifetime) {
        this.authorizationCodeLifetime = authorizationCodeLifetime;
    }

    public List<String> getClaimTypesSupported() {
        return claimTypesSupported;
    }

    public void setClaimTypesSupported(List<String> claimTypesSupported) {
        this.claimTypesSupported = claimTypesSupported;
    }

    public boolean isUmaResourceLifetime() {
        return umaResourceLifetime;
    }

    public void setUmaResourceLifetime(boolean umaResourceLifetime) {
        this.umaResourceLifetime = umaResourceLifetime;
    }

    public String getOxId() {
        return oxId;
    }

    public void setOxId(String oxId) {
        this.oxId = oxId;
    }

    public String getOxElevenDeleteKeyEndpoint() {
        return oxElevenDeleteKeyEndpoint;
    }

    public void setOxElevenDeleteKeyEndpoint(String oxElevenDeleteKeyEndpoint) {
        this.oxElevenDeleteKeyEndpoint = oxElevenDeleteKeyEndpoint;
    }

    public PairwiseIdType getPairwiseIdType() {
        return pairwiseIdType;
    }

    public void setPairwiseIdType(PairwiseIdType pairwiseIdType) {
        this.pairwiseIdType = pairwiseIdType;
    }

    public String getClientInfoEndpoint() {
        return clientInfoEndpoint;
    }

    public void setClientInfoEndpoint(String clientInfoEndpoint) {
        this.clientInfoEndpoint = clientInfoEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public List<String> getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(List<String> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public String getJsLocation() {
        return jsLocation;
    }

    public void setJsLocation(String jsLocation) {
        this.jsLocation = jsLocation;
    }

    public List<CorsConfigurationFilters> getCorsConfigurationFilters() {
        return corsConfigurationFilters;
    }

    public void setCorsConfigurationFilters(List<CorsConfigurationFilters> corsConfigurationFilters) {
        this.corsConfigurationFilters = corsConfigurationFilters;
    }

    public int getMetricReporterInterval() {
        return metricReporterInterval;
    }

    public void setMetricReporterInterval(int metricReporterInterval) {
        this.metricReporterInterval = metricReporterInterval;
    }

    public List<String> getDynamicRegistrationCustomAttributes() {
        return dynamicRegistrationCustomAttributes;
    }

    public void setDynamicRegistrationCustomAttributes(List<String> dynamicRegistrationCustomAttributes) {
        this.dynamicRegistrationCustomAttributes = dynamicRegistrationCustomAttributes;
    }

    public boolean isRequestUriParameterSupported() {
        return requestUriParameterSupported;
    }

    public void setRequestUriParameterSupported(boolean requestUriParameterSupported) {
        this.requestUriParameterSupported = requestUriParameterSupported;
    }

    public int getSessionIdUnauthenticatedUnusedLifetime() {
        return sessionIdUnauthenticatedUnusedLifetime;
    }

    public void setSessionIdUnauthenticatedUnusedLifetime(int sessionIdUnauthenticatedUnusedLifetime) {
        this.sessionIdUnauthenticatedUnusedLifetime = sessionIdUnauthenticatedUnusedLifetime;
    }

    public String getOxElevenSignEndpoint() {
        return oxElevenSignEndpoint;
    }

    public void setOxElevenSignEndpoint(String oxElevenSignEndpoint) {
        this.oxElevenSignEndpoint = oxElevenSignEndpoint;
    }

    public String getCssLocation() {
        return cssLocation;
    }

    public void setCssLocation(String cssLocation) {
        this.cssLocation = cssLocation;
    }

    public boolean isLogClientNameOnClientAuthentication() {
        return logClientNameOnClientAuthentication;
    }

    public void setLogClientNameOnClientAuthentication(boolean logClientNameOnClientAuthentication) {
        this.logClientNameOnClientAuthentication = logClientNameOnClientAuthentication;
    }

    public boolean isUpdateClientAccessTime() {
        return updateClientAccessTime;
    }

    public void setUpdateClientAccessTime(boolean updateClientAccessTime) {
        this.updateClientAccessTime = updateClientAccessTime;
    }

    public String getOpenIdConfigurationEndpoint() {
        return openIdConfigurationEndpoint;
    }

    public void setOpenIdConfigurationEndpoint(String openIdConfigurationEndpoint) {
        this.openIdConfigurationEndpoint = openIdConfigurationEndpoint;
    }

    public boolean isUpdateUserLastLogonTime() {
        return updateUserLastLogonTime;
    }

    public void setUpdateUserLastLogonTime(boolean updateUserLastLogonTime) {
        this.updateUserLastLogonTime = updateUserLastLogonTime;
    }

    public String getEndSessionEndpoint() {
        return endSessionEndpoint;
    }

    public void setEndSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
    }

    public boolean isTrustedClientEnabled() {
        return trustedClientEnabled;
    }

    public void setTrustedClientEnabled(boolean trustedClientEnabled) {
        this.trustedClientEnabled = trustedClientEnabled;
    }

    public List<String> getClientWhiteList() {
        return clientWhiteList;
    }

    public void setClientWhiteList(List<String> clientWhiteList) {
        this.clientWhiteList = clientWhiteList;
    }

    public List<String> getClaimsLocalesSupported() {
        return claimsLocalesSupported;
    }

    public void setClaimsLocalesSupported(List<String> claimsLocalesSupported) {
        this.claimsLocalesSupported = claimsLocalesSupported;
    }

    public List<String> getUserInfoEncryptionAlgValuesSupported() {
        return userInfoEncryptionAlgValuesSupported;
    }

    public void setUserInfoEncryptionAlgValuesSupported(List<String> userInfoEncryptionAlgValuesSupported) {
        this.userInfoEncryptionAlgValuesSupported = userInfoEncryptionAlgValuesSupported;
    }

    public Set<Set<ResponseTypeApi>> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public void setResponseTypesSupported(Set<Set<ResponseTypeApi>> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public String getOxElevenGenerateKeyEndpoint() {
        return oxElevenGenerateKeyEndpoint;
    }

    public void setOxElevenGenerateKeyEndpoint(String oxElevenGenerateKeyEndpoint) {
        this.oxElevenGenerateKeyEndpoint = oxElevenGenerateKeyEndpoint;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public boolean isSkipAuthorizationForOpenIdScopeAndPairwiseId() {
        return skipAuthorizationForOpenIdScopeAndPairwiseId;
    }

    public void setSkipAuthorizationForOpenIdScopeAndPairwiseId(boolean skipAuthorizationForOpenIdScopeAndPairwiseId) {
        this.skipAuthorizationForOpenIdScopeAndPairwiseId = skipAuthorizationForOpenIdScopeAndPairwiseId;
    }

    public int getDynamicRegistrationExpirationTime() {
        return dynamicRegistrationExpirationTime;
    }

    public void setDynamicRegistrationExpirationTime(int dynamicRegistrationExpirationTime) {
        this.dynamicRegistrationExpirationTime = dynamicRegistrationExpirationTime;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getApplianceInum() {
        return applianceInum;
    }

    public void setApplianceInum(String applianceInum) {
        this.applianceInum = applianceInum;
    }

    public boolean isPersistRefreshTokenInLdap() {
        return persistRefreshTokenInLdap;
    }

    public void setPersistRefreshTokenInLdap(boolean persistRefreshTokenInLdap) {
        this.persistRefreshTokenInLdap = persistRefreshTokenInLdap;
    }

    public String getPairwiseCalculationKey() {
        return pairwiseCalculationKey;
    }

    public void setPairwiseCalculationKey(String pairwiseCalculationKey) {
        this.pairwiseCalculationKey = pairwiseCalculationKey;
    }

    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
    }

    public String getIdGenerationEndpoint() {
        return idGenerationEndpoint;
    }

    public void setIdGenerationEndpoint(String idGenerationEndpoint) {
        this.idGenerationEndpoint = idGenerationEndpoint;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public List<String> getIdTokenEncryptionAlgValuesSupported() {
        return idTokenEncryptionAlgValuesSupported;
    }

    public void setIdTokenEncryptionAlgValuesSupported(List<String> idTokenEncryptionAlgValuesSupported) {
        this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
    }

    public String getCheckSessionIFrame() {
        return checkSessionIFrame;
    }

    public void setCheckSessionIFrame(String checkSessionIFrame) {
        this.checkSessionIFrame = checkSessionIFrame;
    }

    public List<ClientAuthenticationFilters> getClientAuthenticationFilters() {
        return clientAuthenticationFilters;
    }

    public void setClientAuthenticationFilters(List<ClientAuthenticationFilters> clientAuthenticationFilters) {
        this.clientAuthenticationFilters = clientAuthenticationFilters;
    }

    public String getDefaultSubjectType() {
        return defaultSubjectType;
    }

    public void setDefaultSubjectType(String defaultSubjectType) {
        this.defaultSubjectType = defaultSubjectType;
    }

    public int getIdTokenLifetime() {
        return idTokenLifetime;
    }

    public void setIdTokenLifetime(int idTokenLifetime) {
        this.idTokenLifetime = idTokenLifetime;
    }

    public List<String> getIdTokenEncryptionEncValuesSupported() {
        return idTokenEncryptionEncValuesSupported;
    }

    public void setIdTokenEncryptionEncValuesSupported(List<String> idTokenEncryptionEncValuesSupported) {
        this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
    }

    public boolean isEnableClientGrantTypeUpdate() {
        return enableClientGrantTypeUpdate;
    }

    public void setEnableClientGrantTypeUpdate(boolean enableClientGrantTypeUpdate) {
        this.enableClientGrantTypeUpdate = enableClientGrantTypeUpdate;
    }

    public String getKeyStoreSecret() {
        return keyStoreSecret;
    }

    public void setKeyStoreSecret(String keyStoreSecret) {
        this.keyStoreSecret = keyStoreSecret;
    }

    public String getExternalLoggerConfiguration() {
        return externalLoggerConfiguration;
    }

    public void setExternalLoggerConfiguration(String externalLoggerConfiguration) {
        this.externalLoggerConfiguration = externalLoggerConfiguration;
    }

    public int getConfigurationUpdateInterval() {
        return configurationUpdateInterval;
    }

    public void setConfigurationUpdateInterval(int configurationUpdateInterval) {
        this.configurationUpdateInterval = configurationUpdateInterval;
    }

    public WebKeyStorageApi getWebKeysStorage() {
        return webKeysStorage;
    }

    public void setWebKeysStorage(WebKeyStorageApi webKeysStorage) {
        this.webKeysStorage = webKeysStorage;
    }

    public boolean isEndSessionWithAccessToken() {
        return endSessionWithAccessToken;
    }

    public void setEndSessionWithAccessToken(boolean endSessionWithAccessToken) {
        this.endSessionWithAccessToken = endSessionWithAccessToken;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public boolean isDynamicRegistrationScopesParamEnabled() {
        return dynamicRegistrationScopesParamEnabled;
    }

    public void setDynamicRegistrationScopesParamEnabled(boolean dynamicRegistrationScopesParamEnabled) {
        this.dynamicRegistrationScopesParamEnabled = dynamicRegistrationScopesParamEnabled;
    }

    public String getUmaRequesterPermissionTokenLifetime() {
        return umaRequesterPermissionTokenLifetime;
    }

    public void setUmaRequesterPermissionTokenLifetime(String umaRequesterPermissionTokenLifetime) {
        this.umaRequesterPermissionTokenLifetime = umaRequesterPermissionTokenLifetime;
    }

    public String getOpPolicyUri() {
        return opPolicyUri;
    }

    public void setOpPolicyUri(String opPolicyUri) {
        this.opPolicyUri = opPolicyUri;
    }

    public String getAuthorizationPage() {
        return authorizationPage;
    }

    public void setAuthorizationPage(String authorizationPage) {
        this.authorizationPage = authorizationPage;
    }

    public boolean isLegacyDynamicRegistrationScopeParam() {
        return legacyDynamicRegistrationScopeParam;
    }

    public void setLegacyDynamicRegistrationScopeParam(boolean legacyDynamicRegistrationScopeParam) {
        this.legacyDynamicRegistrationScopeParam = legacyDynamicRegistrationScopeParam;
    }

    public String getDynamicRegistrationCustomObjectClass() {
        return dynamicRegistrationCustomObjectClass;
    }

    public void setDynamicRegistrationCustomObjectClass(String dynamicRegistrationCustomObjectClass) {
        this.dynamicRegistrationCustomObjectClass = dynamicRegistrationCustomObjectClass;
    }

    public String getOxElevenVerifySignatureEndpoint() {
        return oxElevenVerifySignatureEndpoint;
    }

    public void setOxElevenVerifySignatureEndpoint(String oxElevenVerifySignatureEndpoint) {
        this.oxElevenVerifySignatureEndpoint = oxElevenVerifySignatureEndpoint;
    }

    public boolean isKeyRegenerationEnabled() {
        return keyRegenerationEnabled;
    }

    public void setKeyRegenerationEnabled(boolean keyRegenerationEnabled) {
        this.keyRegenerationEnabled = keyRegenerationEnabled;
    }

    public boolean isClientAuthenticationFiltersEnabled() {
        return clientAuthenticationFiltersEnabled;
    }

    public void setClientAuthenticationFiltersEnabled(boolean clientAuthenticationFiltersEnabled) {
        this.clientAuthenticationFiltersEnabled = clientAuthenticationFiltersEnabled;
    }

    public boolean isHttpLoggingEnabled() {
        return httpLoggingEnabled;
    }

    public void setHttpLoggingEnabled(boolean httpLoggingEnabled) {
        this.httpLoggingEnabled = httpLoggingEnabled;
    }

    public List<String> getClientBlackList() {
        return clientBlackList;
    }

    public void setClientBlackList(List<String> clientBlackList) {
        this.clientBlackList = clientBlackList;
    }

    public int getSessionIdUnusedLifetime() {
        return sessionIdUnusedLifetime;
    }

    public void setSessionIdUnusedLifetime(int sessionIdUnusedLifetime) {
        this.sessionIdUnusedLifetime = sessionIdUnusedLifetime;
    }

    public List<String> getTokenEndpointAuthMethodsSupported() {
        return tokenEndpointAuthMethodsSupported;
    }

    public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    public boolean isSessionAsJwt() {
        return sessionAsJwt;
    }

    public void setSessionAsJwt(boolean sessionAsJwt) {
        this.sessionAsJwt = sessionAsJwt;
    }

    public boolean isClaimsParameterSupported() {
        return claimsParameterSupported;
    }

    public void setClaimsParameterSupported(boolean claimsParameterSupported) {
        this.claimsParameterSupported = claimsParameterSupported;
    }

    public String getOpTosUri() {
        return opTosUri;
    }

    public void setOpTosUri(String opTosUri) {
        this.opTosUri = opTosUri;
    }

    public List<String> getRequestObjectEncryptionAlgValuesSupported() {
        return requestObjectEncryptionAlgValuesSupported;
    }

    public void setRequestObjectEncryptionAlgValuesSupported(List<String> requestObjectEncryptionAlgValuesSupported) {
        this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
    }

    public boolean isRequestParameterSupported() {
        return requestParameterSupported;
    }

    public void setRequestParameterSupported(boolean requestParameterSupported) {
        this.requestParameterSupported = requestParameterSupported;
    }

    public String getOrganizationInum() {
        return organizationInum;
    }

    public void setOrganizationInum(String organizationInum) {
        this.organizationInum = organizationInum;
    }

    public Set<String> getAuthorizationRequestCustomAllowedParameters() {
        return authorizationRequestCustomAllowedParameters;
    }

    public void setAuthorizationRequestCustomAllowedParameters(Set<String> authorizationRequestCustomAllowedParameters) {
        this.authorizationRequestCustomAllowedParameters = authorizationRequestCustomAllowedParameters;
    }

    public String getOxOpenIdConnectVersion() {
        return oxOpenIdConnectVersion;
    }

    public void setOxOpenIdConnectVersion(String oxOpenIdConnectVersion) {
        this.oxOpenIdConnectVersion = oxOpenIdConnectVersion;
    }

    public List<String> getUserInfoEncryptionEncValuesSupported() {
        return userInfoEncryptionEncValuesSupported;
    }

    public void setUserInfoEncryptionEncValuesSupported(List<String> userInfoEncryptionEncValuesSupported) {
        this.userInfoEncryptionEncValuesSupported = userInfoEncryptionEncValuesSupported;
    }

    public boolean isDynamicRegistrationEnabled() {
        return dynamicRegistrationEnabled;
    }

    public void setDynamicRegistrationEnabled(boolean dynamicRegistrationEnabled) {
        this.dynamicRegistrationEnabled = dynamicRegistrationEnabled;
    }

    public List<String> getRequestObjectSigningAlgValuesSupported() {
        return requestObjectSigningAlgValuesSupported;
    }

    public void setRequestObjectSigningAlgValuesSupported(List<String> requestObjectSigningAlgValuesSupported) {
        this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
    }

    public String getUmaConfigurationEndpoint() {
        return umaConfigurationEndpoint;
    }

    public void setUmaConfigurationEndpoint(String umaConfigurationEndpoint) {
        this.umaConfigurationEndpoint = umaConfigurationEndpoint;
    }

    public int getCleanServiceInterval() {
        return cleanServiceInterval;
    }

    public void setCleanServiceInterval(int cleanServiceInterval) {
        this.cleanServiceInterval = cleanServiceInterval;
    }

    public Set<String> getDynamicGrantTypeDefault() {
        return dynamicGrantTypeDefault;
    }

    public void setDynamicGrantTypeDefault(Set<String> dynamicGrantTypeDefault) {
        this.dynamicGrantTypeDefault = dynamicGrantTypeDefault;
    }

    public List<String> getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    public void setSubjectTypesSupported(List<String> subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
    }

    public String getBaseEndpoint() {
        return baseEndpoint;
    }

    public void setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getRefreshTokenLifetime() {
        return refreshTokenLifetime;
    }

    public void setRefreshTokenLifetime(int refreshTokenLifetime) {
        this.refreshTokenLifetime = refreshTokenLifetime;
    }

    public boolean isUseCacheForAllImplicitFlowObjects() {
        return useCacheForAllImplicitFlowObjects;
    }

    public void setUseCacheForAllImplicitFlowObjects(boolean useCacheForAllImplicitFlowObjects) {
        this.useCacheForAllImplicitFlowObjects = useCacheForAllImplicitFlowObjects;
    }

    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
    }

    public String getImgLocation() {
        return imgLocation;
    }

    public void setImgLocation(String imgLocation) {
        this.imgLocation = imgLocation;
    }

    public boolean isSessionIdEnabled() {
        return sessionIdEnabled;
    }

    public void setSessionIdEnabled(boolean sessionIdEnabled) {
        this.sessionIdEnabled = sessionIdEnabled;
    }

    public boolean isDynamicRegistrationPersistClientAuthorizations() {
        return dynamicRegistrationPersistClientAuthorizations;
    }

    public void setDynamicRegistrationPersistClientAuthorizations(boolean dynamicRegistrationPersistClientAuthorizations) {
        this.dynamicRegistrationPersistClientAuthorizations = dynamicRegistrationPersistClientAuthorizations;
    }

    public Set<String> getHttpLoggingExludePaths() {
        return httpLoggingExludePaths;
    }

    public void setHttpLoggingExludePaths(Set<String> httpLoggingExludePaths) {
        this.httpLoggingExludePaths = httpLoggingExludePaths;
    }

    public Set<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public void setGrantTypesSupported(Set<String> grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public String getOxElevenJwksEndpoint() {
        return oxElevenJwksEndpoint;
    }

    public void setOxElevenJwksEndpoint(String oxElevenJwksEndpoint) {
        this.oxElevenJwksEndpoint = oxElevenJwksEndpoint;
    }

    public boolean isUmaRptAsJwt() {
        return umaRptAsJwt;
    }

    public void setUmaRptAsJwt(boolean umaRptAsJwt) {
        this.umaRptAsJwt = umaRptAsJwt;
    }

    public boolean isLogClientIdOnClientAuthentication() {
        return logClientIdOnClientAuthentication;
    }

    public void setLogClientIdOnClientAuthentication(boolean logClientIdOnClientAuthentication) {
        this.logClientIdOnClientAuthentication = logClientIdOnClientAuthentication;
    }

    public List<String> getUserInfoSigningAlgValuesSupported() {
        return userInfoSigningAlgValuesSupported;
    }

    public void setUserInfoSigningAlgValuesSupported(List<String> userInfoSigningAlgValuesSupported) {
        this.userInfoSigningAlgValuesSupported = userInfoSigningAlgValuesSupported;
    }

    public int getSessionIdLifetime() {
        return sessionIdLifetime;
    }

    public void setSessionIdLifetime(int sessionIdLifetime) {
        this.sessionIdLifetime = sessionIdLifetime;
    }

    public List<String> getPersonCustomObjectClassList() {
        return personCustomObjectClassList;
    }

    public void setPersonCustomObjectClassList(List<String> personCustomObjectClassList) {
        this.personCustomObjectClassList = personCustomObjectClassList;
    }

    public List<String> getUiLocalesSupported() {
        return uiLocalesSupported;
    }

    public void setUiLocalesSupported(List<String> uiLocalesSupported) {
        this.uiLocalesSupported = uiLocalesSupported;
    }

    public String getPairwiseCalculationSalt() {
        return pairwiseCalculationSalt;
    }

    public void setPairwiseCalculationSalt(String pairwiseCalculationSalt) {
        this.pairwiseCalculationSalt = pairwiseCalculationSalt;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public boolean isUmaValidateClaimToken() {
        return umaValidateClaimToken;
    }

    public void setUmaValidateClaimToken(boolean umaValidateClaimToken) {
        this.umaValidateClaimToken = umaValidateClaimToken;
    }

    public boolean isUmaAddScopesAutomatically() {
        return umaAddScopesAutomatically;
    }

    public void setUmaAddScopesAutomatically(boolean umaAddScopesAutomatically) {
        this.umaAddScopesAutomatically = umaAddScopesAutomatically;
    }

    public boolean isUmaGrantAccessIfNoPolicies() {
        return umaGrantAccessIfNoPolicies;
    }

    public void setUmaGrantAccessIfNoPolicies(boolean umaGrantAccessIfNoPolicies) {
        this.umaGrantAccessIfNoPolicies = umaGrantAccessIfNoPolicies;
    }

    public List<String> getRequestObjectEncryptionEncValuesSupported() {
        return requestObjectEncryptionEncValuesSupported;
    }

    public void setRequestObjectEncryptionEncValuesSupported(List<String> requestObjectEncryptionEncValuesSupported) {
        this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
    }

    public boolean isLegacyIdTokenClaims() {
        return legacyIdTokenClaims;
    }

    public void setLegacyIdTokenClaims(boolean legacyIdTokenClaims) {
        this.legacyIdTokenClaims = legacyIdTokenClaims;
    }

    public boolean isUmaKeepClientDuringResourceSetRegistration() {
        return umaKeepClientDuringResourceSetRegistration;
    }

    public void setUmaKeepClientDuringResourceSetRegistration(boolean umaKeepClientDuringResourceSetRegistration) {
        this.umaKeepClientDuringResourceSetRegistration = umaKeepClientDuringResourceSetRegistration;
    }

    public boolean isRequireRequestUriRegistration() {
        return requireRequestUriRegistration;
    }

    public void setRequireRequestUriRegistration(boolean requireRequestUriRegistration) {
        this.requireRequestUriRegistration = requireRequestUriRegistration;
    }

    public boolean isFrontChannelLogoutSessionSupported() {
        return frontChannelLogoutSessionSupported;
    }

    public void setFrontChannelLogoutSessionSupported(boolean frontChannelLogoutSessionSupported) {
        this.frontChannelLogoutSessionSupported = frontChannelLogoutSessionSupported;
    }

    public String getServiceDocumentation() {
        return serviceDocumentation;
    }

    public void setServiceDocumentation(String serviceDocumentation) {
        this.serviceDocumentation = serviceDocumentation;
    }

    public int getKeyRegenerationInterval() {
        return keyRegenerationInterval;
    }

    public void setKeyRegenerationInterval(int keyRegenerationInterval) {
        this.keyRegenerationInterval = keyRegenerationInterval;
    }

    public List<String> getDisplayValuesSupported() {
        return displayValuesSupported;
    }

    public void setDisplayValuesSupported(List<String> displayValuesSupported) {
        this.displayValuesSupported = displayValuesSupported;
    }

    public String getSectorIdentifierEndpoint() {
        return sectorIdentifierEndpoint;
    }

    public void setSectorIdentifierEndpoint(String sectorIdentifierEndpoint) {
        this.sectorIdentifierEndpoint = sectorIdentifierEndpoint;
    }

    public boolean isAuthenticationFiltersEnabled() {
        return authenticationFiltersEnabled;
    }

    public void setAuthenticationFiltersEnabled(boolean authenticationFiltersEnabled) {
        this.authenticationFiltersEnabled = authenticationFiltersEnabled;
    }
}