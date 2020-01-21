/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * CAS oxAuth application configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/25/2016
 */
package org.gluu.oxauth.cas.auth.conf;

import org.gluu.conf.model.AppConfiguration;

/**
 * CAS application configuration
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/25/2016
 */
public class CasAppConfiguration extends AppConfiguration {

	private static final long serialVersionUID = 5450226508968717097L;

	private boolean openIdDefaultAuthenticator;

	public boolean isOpenIdDefaultAuthenticator() {
		return openIdDefaultAuthenticator;
	}

	public void setOpenIdDefaultAuthenticator(boolean openIdDefaultAuthenticator) {
		this.openIdDefaultAuthenticator = openIdDefaultAuthenticator;
	}

}
