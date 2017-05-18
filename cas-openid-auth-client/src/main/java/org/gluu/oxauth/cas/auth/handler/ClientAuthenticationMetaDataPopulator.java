/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.handler;

import org.gluu.oxauth.cas.auth.principal.ClientCredential;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;

/**
 * This class is a meta data populator for authentication
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public final class ClientAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

	/***
	 * The name of the client used to perform the authentication.
	 */
	public static final String CLIENT_NAME = "clientName";

	/**
	 * {@InheritDoc}
	 */
	@Override
	public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
		if (credential instanceof ClientCredential) {
			final ClientCredential clientCredential = (ClientCredential) credential;
			builder.addAttribute(CLIENT_NAME, clientCredential.getOpenIdCredentials().getClientName());
		}
	}

}
