/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.client;

import org.gluu.oxauth.cas.auth.conf.CasAppConfiguration;
import org.gluu.oxauth.cas.auth.conf.CasConfigurationFactory;
import org.gluu.oxauth.cas.auth.conf.CasLdapAppConfiguration;
import org.gluu.oxauth.cas.auth.login.flow.ClientAction;
import org.gluu.oxauth.client.OpenIdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the oxAuth client to authenticate users and retrieve user profile
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public class AuthClient extends OpenIdClient<CasAppConfiguration, CasLdapAppConfiguration> {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(ClientAction.class);

	public AuthClient() {
		super(CasConfigurationFactory.instance());
	}

	public boolean isOpenIdDefaultAuthenticator() {
		return getAppConfiguration().isOpenIdDefaultAuthenticator();
	}

}
