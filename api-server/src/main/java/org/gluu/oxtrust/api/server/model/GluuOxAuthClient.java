package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxtrust.model.AuthenticationMethod;
import org.gluu.oxtrust.model.BlockEncryptionAlgorithm;
import org.gluu.oxtrust.model.KeyEncryptionAlgorithm;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthSubjectType;
import org.gluu.oxtrust.model.SignatureAlgorithm;
import org.gluu.persist.annotation.AttributeName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GluuOxAuthClient implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1994703766536084032L;

	private String inum;

	private String displayName;

	private String description;

	@JsonIgnore
	private String dn;

	private OxAuthApplicationType oxAuthAppType;

	private List<String> contacts;

	private List<String> oxAuthRedirectURIs;

	private List<String> oxAuthPostLogoutRedirectURIs;

	private List<String> oxAuthScopes;

	private String encodedClientSecret;

	private String userPassword;

	private List<String> associatedPersons;

	private Boolean oxAuthTrustedClient = Boolean.FALSE;

	private ResponseType[] responseTypes;

	private GrantType[] grantTypes;

	private String logoUri;

	private String clientUri;

	private String policyUri;

	private String tosUri;

	private String jwksUri;

	private String jwks;

	private String sectorIdentifierUri;

	private OxAuthSubjectType subjectType;

	private SignatureAlgorithm idTokenSignedResponseAlg;

	private KeyEncryptionAlgorithm idTokenEncryptedResponseAlg;

	private BlockEncryptionAlgorithm idTokenEncryptedResponseEnc;

	private SignatureAlgorithm userInfoSignedResponseAlg;

	private KeyEncryptionAlgorithm userInfoEncryptedResponseAlg;

	private BlockEncryptionAlgorithm userInfoEncryptedResponseEnc;

	private SignatureAlgorithm requestObjectSigningAlg;

	private KeyEncryptionAlgorithm requestObjectEncryptionAlg;

	private BlockEncryptionAlgorithm requestObjectEncryptionEnc;

	private AuthenticationMethod tokenEndpointAuthMethod;

	private SignatureAlgorithm tokenEndpointAuthSigningAlg;

	private Integer defaultMaxAge;

	private Boolean requireAuthTime;

	private String[] postLogoutRedirectUris;

	private String[] claimRedirectURI;

	private List<String> logoutUri;

	private Boolean logoutSessionRequired = Boolean.FALSE;

	private Boolean oxAuthPersistClientAuthorizations = Boolean.TRUE;

	private Boolean oxIncludeClaimsInIdToken = Boolean.FALSE;

	private Integer oxRefreshTokenLifetime;

	private String[] defaultAcrValues;

	private String initiateLoginUri;

	private Date clientSecretExpiresAt;

	@AttributeName(name = "oxAuthRequestURI")
	private String[] requestUris;

	private boolean disabled;


	private String oxAuthClientSecret;


	public String[] getClaimRedirectURI() {
		return claimRedirectURI;
	}

	public void setClaimRedirectURI(String[] claimRedirectURI) {
		this.claimRedirectURI = claimRedirectURI;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public OxAuthApplicationType getOxAuthAppType() {
		return oxAuthAppType;
	}

	public void setOxAuthAppType(OxAuthApplicationType oxAuthAppType) {
		this.oxAuthAppType = oxAuthAppType;
	}

	public List<String> getContacts() {
		return contacts;
	}

	public void setContacts(List<String> contacts) {
		this.contacts = contacts;
	}

	public List<String> getOxAuthRedirectURIs() {
		return oxAuthRedirectURIs;
	}

	public void setOxAuthRedirectURIs(List<String> oxAuthRedirectURIs) {
		this.oxAuthRedirectURIs = oxAuthRedirectURIs;
	}

	public List<String> getOxAuthPostLogoutRedirectURIs() {
		return oxAuthPostLogoutRedirectURIs;
	}

	public void setOxAuthPostLogoutRedirectURIs(List<String> oxAuthPostLogoutRedirectURIs) {
		this.oxAuthPostLogoutRedirectURIs = oxAuthPostLogoutRedirectURIs;
	}

	public List<String> getOxAuthScopes() {
		return oxAuthScopes;
	}

	public void setOxAuthScopes(List<String> oxAuthScopes) {
		this.oxAuthScopes = oxAuthScopes;
	}

	public String getEncodedClientSecret() {
		return encodedClientSecret;
	}

	public void setEncodedClientSecret(String encodedClientSecret) {
		this.encodedClientSecret = encodedClientSecret;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public List<String> getAssociatedPersons() {
		return associatedPersons;
	}

	public void setAssociatedPersons(List<String> associatedPersons) {
		this.associatedPersons = associatedPersons;
	}

	public Boolean getOxAuthTrustedClient() {
		return oxAuthTrustedClient;
	}

	public void setOxAuthTrustedClient(Boolean oxAuthTrustedClient) {
		this.oxAuthTrustedClient = oxAuthTrustedClient;
	}

	public ResponseType[] getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(ResponseType[] responseTypes) {
		this.responseTypes = responseTypes;
	}

	public GrantType[] getGrantTypes() {
		return grantTypes;
	}

	public void setGrantTypes(GrantType[] grantTypes) {
		this.grantTypes = grantTypes;
	}

	public String getLogoUri() {
		return logoUri;
	}

	public void setLogoUri(String logoUri) {
		this.logoUri = logoUri;
	}

	public String getClientUri() {
		return clientUri;
	}

	public void setClientUri(String clientUri) {
		this.clientUri = clientUri;
	}

	public String getPolicyUri() {
		return policyUri;
	}

	public void setPolicyUri(String policyUri) {
		this.policyUri = policyUri;
	}

	public String getTosUri() {
		return tosUri;
	}

	public void setTosUri(String tosUri) {
		this.tosUri = tosUri;
	}

	public String getJwksUri() {
		return jwksUri;
	}

	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	public String getJwks() {
		return jwks;
	}

	public void setJwks(String jwks) {
		this.jwks = jwks;
	}

	public String getSectorIdentifierUri() {
		return sectorIdentifierUri;
	}

	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		this.sectorIdentifierUri = sectorIdentifierUri;
	}

	public OxAuthSubjectType getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(OxAuthSubjectType subjectType) {
		this.subjectType = subjectType;
	}

	public SignatureAlgorithm getIdTokenSignedResponseAlg() {
		return idTokenSignedResponseAlg;
	}

	public void setIdTokenSignedResponseAlg(SignatureAlgorithm idTokenSignedResponseAlg) {
		this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
	}

	public KeyEncryptionAlgorithm getIdTokenEncryptedResponseAlg() {
		return idTokenEncryptedResponseAlg;
	}

	public void setIdTokenEncryptedResponseAlg(KeyEncryptionAlgorithm idTokenEncryptedResponseAlg) {
		this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg;
	}

	public BlockEncryptionAlgorithm getIdTokenEncryptedResponseEnc() {
		return idTokenEncryptedResponseEnc;
	}

	public void setIdTokenEncryptedResponseEnc(BlockEncryptionAlgorithm idTokenEncryptedResponseEnc) {
		this.idTokenEncryptedResponseEnc = idTokenEncryptedResponseEnc;
	}

	public SignatureAlgorithm getUserInfoSignedResponseAlg() {
		return userInfoSignedResponseAlg;
	}

	public void setUserInfoSignedResponseAlg(SignatureAlgorithm userInfoSignedResponseAlg) {
		this.userInfoSignedResponseAlg = userInfoSignedResponseAlg;
	}

	public KeyEncryptionAlgorithm getUserInfoEncryptedResponseAlg() {
		return userInfoEncryptedResponseAlg;
	}

	public void setUserInfoEncryptedResponseAlg(KeyEncryptionAlgorithm userInfoEncryptedResponseAlg) {
		this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg;
	}

	public BlockEncryptionAlgorithm getUserInfoEncryptedResponseEnc() {
		return userInfoEncryptedResponseEnc;
	}

	public void setUserInfoEncryptedResponseEnc(BlockEncryptionAlgorithm userInfoEncryptedResponseEnc) {
		this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc;
	}

	public SignatureAlgorithm getRequestObjectSigningAlg() {
		return requestObjectSigningAlg;
	}

	public void setRequestObjectSigningAlg(SignatureAlgorithm requestObjectSigningAlg) {
		this.requestObjectSigningAlg = requestObjectSigningAlg;
	}

	public KeyEncryptionAlgorithm getRequestObjectEncryptionAlg() {
		return requestObjectEncryptionAlg;
	}

	public void setRequestObjectEncryptionAlg(KeyEncryptionAlgorithm requestObjectEncryptionAlg) {
		this.requestObjectEncryptionAlg = requestObjectEncryptionAlg;
	}

	public BlockEncryptionAlgorithm getRequestObjectEncryptionEnc() {
		return requestObjectEncryptionEnc;
	}

	public void setRequestObjectEncryptionEnc(BlockEncryptionAlgorithm requestObjectEncryptionEnc) {
		this.requestObjectEncryptionEnc = requestObjectEncryptionEnc;
	}

	public SignatureAlgorithm getTokenEndpointAuthSigningAlg() {
		return tokenEndpointAuthSigningAlg;
	}

	public void setTokenEndpointAuthSigningAlg(SignatureAlgorithm tokenEndpointAuthSigningAlg) {
		this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
	}

	public Integer getDefaultMaxAge() {
		return defaultMaxAge;
	}

	public void setDefaultMaxAge(Integer defaultMaxAge) {
		this.defaultMaxAge = defaultMaxAge;
	}

	public Boolean getRequireAuthTime() {
		return requireAuthTime;
	}

	public void setRequireAuthTime(Boolean requireAuthTime) {
		this.requireAuthTime = requireAuthTime;
	}

	public AuthenticationMethod getTokenEndpointAuthMethod() {
		return tokenEndpointAuthMethod;
	}

	public void setTokenEndpointAuthMethod(AuthenticationMethod tokenEndpointAuthMethod) {
		this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
	}

	public String[] getPostLogoutRedirectUris() {
		return postLogoutRedirectUris;
	}

	public void setPostLogoutRedirectUris(String[] postLogoutRedirectUris) {
		this.postLogoutRedirectUris = postLogoutRedirectUris;
	}

	public List<String> getLogoutUri() {
		return logoutUri;
	}

	public void setLogoutUri(List<String> logoutUri) {
		this.logoutUri = logoutUri;
	}

	public Boolean getLogoutSessionRequired() {
		return logoutSessionRequired;
	}

	public void setLogoutSessionRequired(Boolean logoutSessionRequired) {
		this.logoutSessionRequired = logoutSessionRequired;
	}

	public Boolean getOxAuthPersistClientAuthorizations() {
		return oxAuthPersistClientAuthorizations;
	}

	public void setOxAuthPersistClientAuthorizations(Boolean oxAuthPersistClientAuthorizations) {
		this.oxAuthPersistClientAuthorizations = oxAuthPersistClientAuthorizations;
	}

	public Boolean getOxIncludeClaimsInIdToken() {
		return oxIncludeClaimsInIdToken;
	}

	public void setOxIncludeClaimsInIdToken(Boolean oxIncludeClaimsInIdToken) {
		this.oxIncludeClaimsInIdToken = oxIncludeClaimsInIdToken;
	}

	public Integer getOxRefreshTokenLifetime() {
		return oxRefreshTokenLifetime;
	}

	public void setOxRefreshTokenLifetime(Integer oxRefreshTokenLifetime) {
		this.oxRefreshTokenLifetime = oxRefreshTokenLifetime;
	}

	public String[] getDefaultAcrValues() {
		return defaultAcrValues;
	}

	public void setDefaultAcrValues(String[] defaultAcrValues) {
		this.defaultAcrValues = defaultAcrValues;
	}

	public String getInitiateLoginUri() {
		return initiateLoginUri;
	}

	public void setInitiateLoginUri(String initiateLoginUri) {
		this.initiateLoginUri = initiateLoginUri;
	}

	public String[] getRequestUris() {
		return requestUris;
	}

	public void setRequestUris(String[] requestUris) {
		this.requestUris = requestUris;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getOxAuthClientSecret() {
		return oxAuthClientSecret;
	}

	public Date getClientSecretExpiresAt() {
		return clientSecretExpiresAt;
	}

	public void setClientSecretExpiresAt(Date clientSecretExpiresAt) {
		this.clientSecretExpiresAt = clientSecretExpiresAt;
	}
}
