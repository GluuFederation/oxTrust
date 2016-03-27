/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.principal;

import org.gluu.oxauth.client.auth.principal.OpenIdCredentials;
import org.jasig.cas.authentication.AbstractCredential;

/**
 * This class represents CAS client credentials
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public final class ClientCredential extends AbstractCredential {

	private static final long serialVersionUID = -7368677422769694487L;

	private OpenIdCredentials openIdCredentials;

	public ClientCredential(final OpenIdCredentials openIdCredentials) {
		this.openIdCredentials = openIdCredentials;
	}

	public OpenIdCredentials getOpenIdCredentials() {
		return openIdCredentials;
	}

	@Override
	public String getId() {
		return openIdCredentials.getId();
	}

}
