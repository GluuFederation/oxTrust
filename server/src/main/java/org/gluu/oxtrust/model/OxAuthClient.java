/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * oxAuthClient
 * 
 * @author Reda Zerrad Date: 06.08.2012
 * @author Yuriy Movchan Date: 05/22/2013
 * @author Javier Rojas Blum
 * @version October 21, 2015
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxAuthClient" })
public class OxAuthClient extends Entry implements Serializable {

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

	@LdapAttribute(name = "associatedPerson")
	private List<String> associatedPersons;

	@LdapAttribute(name = "oxAuthTrustedClient")
	private OxAuthTrustedClientBox oxAuthTrustedClient;

	@LdapAttribute(name = "oxAuthResponseType")
	private ResponseType[] responseTypes;

    @LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
    private String[] postLogoutRedirectUris;

    @LdapAttribute(name = "oxPersistClientAuthorizations")
    private GluuBoolean oxAuthPersistClientAuthorizations;

    @LdapAttribute(name = "oxAuthJwksURI")
    private String jwksUri;

    @LdapAttribute(name = "oxAuthJwks")
    private String jwks;

	private String oxAuthClientSecret;

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

	public OxAuthApplicationType getOxAuthAppType() {
		return oxAuthAppType;
	}

	public void setOxAuthAppType(OxAuthApplicationType oxAuthAppType) {
		this.oxAuthAppType = oxAuthAppType;
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

	public OxAuthTrustedClientBox getOxAuthTrustedClient() {
		return oxAuthTrustedClient;
	}

	public void setOxAuthTrustedClient(OxAuthTrustedClientBox oxAuthTrustedClient) {
		this.oxAuthTrustedClient = oxAuthTrustedClient;
	}

	public ResponseType[] getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(ResponseType[] responseTypes) {
		this.responseTypes = responseTypes;
	}

	public String[] getPostLogoutRedirectUris() {
		return postLogoutRedirectUris;
	}

	public void setPostLogoutRedirectUris(String[] postLogoutRedirectUris) {
		this.postLogoutRedirectUris = postLogoutRedirectUris;
	}

	public GluuBoolean getOxAuthPersistClientAuthorizations() {
		return oxAuthPersistClientAuthorizations;
	}

	public void setOxAuthPersistClientAuthorizations(GluuBoolean oxAuthPersistClientAuthorizations) {
		this.oxAuthPersistClientAuthorizations = oxAuthPersistClientAuthorizations;
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

	public void setOxAuthClientSecret(String oxAuthClientSecret) throws EncryptionException {
		this.oxAuthClientSecret = oxAuthClientSecret;
		if (StringHelper.isNotEmpty(oxAuthClientSecret)) {
			setEncodedClientSecret(StringEncrypter.defaultInstance().encrypt(oxAuthClientSecret, OxTrustConfiguration.instance().getCryptoConfigurationSalt()));
		}
	}

	public String getOxAuthClientSecret() {
		return oxAuthClientSecret;
	}

}
