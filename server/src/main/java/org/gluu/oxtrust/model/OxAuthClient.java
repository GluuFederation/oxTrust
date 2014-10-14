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

	@LdapAttribute(name = "oxAuthClientUserGroup")
	private List<String> oxAuthClientUserGroups;

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

}
