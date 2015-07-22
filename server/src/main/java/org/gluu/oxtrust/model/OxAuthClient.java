/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * oxAuthClient
 * 
 * @author Reda Zerrad Date: 06.08.2012
 * @author Yuriy Movchan Date: 05/22/2013
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxAuthClient" })
@EqualsAndHashCode(callSuper=false)
public @Data class OxAuthClient extends Entry implements Serializable {

	private static final long serialVersionUID = -2310140703735705346L;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String iname;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@NotNull
	@LdapAttribute(name = "oxAuthAppType")
	private OxAuthApplicationType oxAuthAppType;

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

	@LdapAttribute(name = "oxAuthIdTokenSignedResponseAlg")
	private TokenResponseAlgs oxAuthIdTokenSignedResponseAlg;

	@LdapAttribute(name = "associatedPerson")
	private List<String> associatedPersons;

	@LdapAttribute(name = "oxAuthTrustedClient")
	private OxAuthTrustedClientBox oxAuthTrustedClient;

	@LdapAttribute(name = "oxAuthResponseType")
	private ResponseType[] responseTypes;

    @LdapAttribute(name = "oxAuthTokenEndpointAuthMethod")
    private OxAuthAuthenticationMethod tokenEndpointAuthMethod;
	
    @LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
    private String[] postLogoutRedirectUris;

	private String oxAuthClientSecret;

	public void setOxAuthClientSecret(String oxAuthClientSecret) throws EncryptionException {
		this.oxAuthClientSecret = oxAuthClientSecret;
		if (StringHelper.isNotEmpty(oxAuthClientSecret)) {
			setEncodedClientSecret(StringEncrypter.defaultInstance().encrypt(oxAuthClientSecret, OxTrustConfiguration.instance().getCryptoConfiguration().getEncodeSalt()));
		}
	}

    public List<String> getAssociatedPersons() {
        return associatedPersons;
    }

    public void setAssociatedPersons(List<String> associatedPersons) {
        this.associatedPersons = associatedPersons;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEncodedClientSecret() {
        return encodedClientSecret;
    }

    public void setEncodedClientSecret(String encodedClientSecret) {
        this.encodedClientSecret = encodedClientSecret;
    }

    public String getIname() {
        return iname;
    }

    public void setIname(String iname) {
        this.iname = iname;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public OxAuthApplicationType getOxAuthAppType() {
        return oxAuthAppType;
    }

    public void setOxAuthAppType(OxAuthApplicationType oxAuthAppType) {
        this.oxAuthAppType = oxAuthAppType;
    }

    public TokenResponseAlgs getOxAuthIdTokenSignedResponseAlg() {
        return oxAuthIdTokenSignedResponseAlg;
    }

    public void setOxAuthIdTokenSignedResponseAlg(TokenResponseAlgs oxAuthIdTokenSignedResponseAlg) {
        this.oxAuthIdTokenSignedResponseAlg = oxAuthIdTokenSignedResponseAlg;
    }

    public List<String> getOxAuthPostLogoutRedirectURIs() {
        return oxAuthPostLogoutRedirectURIs;
    }

    public void setOxAuthPostLogoutRedirectURIs(List<String> oxAuthPostLogoutRedirectURIs) {
        this.oxAuthPostLogoutRedirectURIs = oxAuthPostLogoutRedirectURIs;
    }

    public List<String> getOxAuthRedirectURIs() {
        return oxAuthRedirectURIs;
    }

    public void setOxAuthRedirectURIs(List<String> oxAuthRedirectURIs) {
        this.oxAuthRedirectURIs = oxAuthRedirectURIs;
    }

    public List<String> getOxAuthScopes() {
        return oxAuthScopes;
    }

    public void setOxAuthScopes(List<String> oxAuthScopes) {
        this.oxAuthScopes = oxAuthScopes;
    }

    public OxAuthTrustedClientBox getOxAuthTrustedClient() {
        return oxAuthTrustedClient;
    }

    public void setOxAuthTrustedClient(OxAuthTrustedClientBox oxAuthTrustedClient) {
        this.oxAuthTrustedClient = oxAuthTrustedClient;
    }

    public String[] getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(String[] postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public ResponseType[] getResponseTypes() {
        return responseTypes;
    }

    public void setResponseTypes(ResponseType[] responseTypes) {
        this.responseTypes = responseTypes;
    }

    public OxAuthAuthenticationMethod getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(OxAuthAuthenticationMethod tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
