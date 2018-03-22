/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.persist.model.base.Entry;
import org.gluu.persist.model.base.GluuBoolean;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * oxAuthClient
 *
 * @author Reda Zerrad Date: 06.08.2012
 * @author Yuriy Movchan Date: 05/22/2013
 * @author Javier Rojas Blum
 * @version March 21, 2018
 */
@LdapEntry(sortBy = {"displayName"})
@LdapObjectClass(values = {"top", "oxAuthClient"})
public class OxAuthClient extends Entry implements Serializable {

    private static final long serialVersionUID = -2310140703735705346L;

    private transient boolean selected;

    @LdapAttribute(ignoreDuringUpdate = true)
    private String inum;

    @LdapAttribute(ignoreDuringUpdate = true)
    private String iname;

    @NotNull
    @Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
    @LdapAttribute
    private String displayName;

    @NotNull
    @Size(min = 0, max = 250, message = "Length of the Description should not exceed 250")
    @LdapAttribute
    private String description;

    @NotNull
    @LdapAttribute(name = "oxAuthAppType")
    private OxAuthApplicationType oxAuthAppType;

    @LdapAttribute(name = "oxAuthContact")
    private List<String> contacts;

    @LdapAttribute(name = "oxAuthRedirectURI")
    private List<String> oxAuthRedirectURIs;

    @LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
    private List<String> oxAuthPostLogoutRedirectURIs;

    @LdapAttribute(name = "oxAuthScope")
    private List<String> oxAuthScopes;

    @NotNull
    @LdapAttribute(name = "oxAuthClientSecret")
    private String encodedClientSecret;

    @LdapAttribute(ignoreDuringUpdate = true)
    private String userPassword;

    @LdapAttribute(name = "associatedPerson")
    private List<String> associatedPersons;

    @LdapAttribute(name = "oxAuthTrustedClient")
    private GluuBoolean oxAuthTrustedClient = GluuBoolean.FALSE;

    @LdapAttribute(name = "oxAuthResponseType")
    private ResponseType[] responseTypes;

    @LdapAttribute(name = "oxAuthGrantType")
    private GrantType[] grantTypes;

    @LdapAttribute(name = "oxAuthLogoURI")
    private String logoUri;

    @LdapAttribute(name = "oxAuthClientURI")
    private String clientUri;

    @LdapAttribute(name = "oxAuthPolicyURI")
    private String policyUri;

    @LdapAttribute(name = "oxAuthTosURI")
    private String tosUri;

    @LdapAttribute(name = "oxAuthJwksURI")
    private String jwksUri;

    @LdapAttribute(name = "oxAuthJwks")
    private String jwks;

    @LdapAttribute(name = "oxAuthSectorIdentifierURI")
    private String sectorIdentifierUri;

    @LdapAttribute(name = "oxAuthSubjectType")
    private OxAuthSubjectType subjectType;

    @LdapAttribute(name = "oxAuthIdTokenSignedResponseAlg")
    private SignatureAlgorithm idTokenSignedResponseAlg;

    @LdapAttribute(name = "oxAuthIdTokenEncryptedResponseAlg")
    private KeyEncryptionAlgorithm idTokenEncryptedResponseAlg;

    @LdapAttribute(name = "oxAuthIdTokenEncryptedResponseEnc")
    private BlockEncryptionAlgorithm idTokenEncryptedResponseEnc;

    @LdapAttribute(name = "oxAuthSignedResponseAlg")
    private SignatureAlgorithm userInfoSignedResponseAlg;

    @LdapAttribute(name = "oxAuthUserInfoEncryptedResponseAlg")
    private KeyEncryptionAlgorithm userInfoEncryptedResponseAlg;

    @LdapAttribute(name = "oxAuthUserInfoEncryptedResponseEnc")
    private BlockEncryptionAlgorithm userInfoEncryptedResponseEnc;

    @LdapAttribute(name = "oxAuthRequestObjectSigningAlg")
    private SignatureAlgorithm requestObjectSigningAlg;

    @LdapAttribute(name = "oxAuthRequestObjectEncryptionAlg")
    private KeyEncryptionAlgorithm requestObjectEncryptionAlg;

    @LdapAttribute(name = "oxAuthRequestObjectEncryptionEnc")
    private BlockEncryptionAlgorithm requestObjectEncryptionEnc;

    @LdapAttribute(name = "oxAuthTokenEndpointAuthMethod")
    private AuthenticationMethod tokenEndpointAuthMethod;

    @LdapAttribute(name = "oxAuthTokenEndpointAuthSigningAlg")
    private SignatureAlgorithm tokenEndpointAuthSigningAlg;

    @LdapAttribute(name = "oxAuthDefaultMaxAge")
    private Integer defaultMaxAge;

    @LdapAttribute(name = "oxAuthRequireAuthTime")
    private GluuBoolean requireAuthTime;

    @LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
    private String[] postLogoutRedirectUris;
    
    @LdapAttribute(name = "oxClaimRedirectURI")
    private String[] claimRedirectURI ;

	@LdapAttribute(name = "oxAuthLogoutURI")
    private List<String> logoutUri;

    @LdapAttribute(name = "oxAuthLogoutSessionRequired")
    private GluuBoolean logoutSessionRequired = GluuBoolean.FALSE;

    @LdapAttribute(name = "oxPersistClientAuthorizations")
    private GluuBoolean oxAuthPersistClientAuthorizations = GluuBoolean.TRUE;

    @LdapAttribute(name = "oxIncludeClaimsInIdToken")
    private GluuBoolean oxIncludeClaimsInIdToken = GluuBoolean.FALSE;

    @LdapAttribute(name = "oxRefreshTokenLifetime")
    private Integer oxRefreshTokenLifetime;

    @LdapAttribute(name = "oxAuthDefaultAcrValues")
    private String[] defaultAcrValues;

    @LdapAttribute(name = "oxAuthInitiateLoginURI")
    private String initiateLoginUri;

    @LdapAttribute(name = "oxAuthClientSecretExpiresAt")
    private Date clientSecretExpiresAt;

    @LdapAttribute(name = "oxAuthRequestURI")
    private String[] requestUris;

    @LdapAttribute(name = "oxAuthAuthorizedOrigins")
    private String[] authorizedOrigins;

    @LdapAttribute(name = "oxDisabled")
    private boolean disabled;

    @LdapAttribute(name = "oxdId")
    private String oxdId;

    private String oxAuthClientSecret;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
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

    public String getIname() {
        return iname;
    }

    public void setIname(String iname) {
        this.iname = iname;
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

    public GluuBoolean getOxAuthTrustedClient() {
        return oxAuthTrustedClient;
    }

    public void setOxAuthTrustedClient(GluuBoolean oxAuthTrustedClient) {
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

    public GluuBoolean getRequireAuthTime() {
        return requireAuthTime;
    }

    public void setRequireAuthTime(GluuBoolean requireAuthTime) {
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

    public GluuBoolean getLogoutSessionRequired() {
        return logoutSessionRequired;
    }

    public void setLogoutSessionRequired(GluuBoolean logoutSessionRequired) {
        this.logoutSessionRequired = logoutSessionRequired;
    }

    public GluuBoolean getOxAuthPersistClientAuthorizations() {
        return oxAuthPersistClientAuthorizations;
    }

    public void setOxAuthPersistClientAuthorizations(GluuBoolean oxAuthPersistClientAuthorizations) {
        this.oxAuthPersistClientAuthorizations = oxAuthPersistClientAuthorizations;
    }

    public GluuBoolean getOxIncludeClaimsInIdToken() {
        return oxIncludeClaimsInIdToken;
    }

    public void setOxIncludeClaimsInIdToken(GluuBoolean oxIncludeClaimsInIdToken) {
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

    public String[] getAuthorizedOrigins() {
        return authorizedOrigins;
    }

    public void setAuthorizedOrigins(String[] authorizedOrigins) {
        this.authorizedOrigins = authorizedOrigins;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setOxAuthClientSecret(String oxAuthClientSecret) throws EncryptionException {
        this.oxAuthClientSecret = oxAuthClientSecret;
        if (StringHelper.isNotEmpty(oxAuthClientSecret)) {
        	EncryptionService encryptionService = CdiUtil.bean(EncryptionService.class);
            setEncodedClientSecret(encryptionService.encrypt(oxAuthClientSecret));
        }
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

    public final String getOxdId() {
        return oxdId;
    }

    public final void setOxdId(String oxdId) {
        this.oxdId = oxdId;
    }

}
