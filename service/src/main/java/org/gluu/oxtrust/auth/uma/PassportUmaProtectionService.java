/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.auth.uma;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.config.oxtrust.AppConfiguration;

/**
 * Provides service to protect Passport Rest service endpoints
 */
@ApplicationScoped
public class PassportUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5547131971095468865L;

	@Inject
	private AppConfiguration appConfiguration;

	protected String getClientId() {
		return appConfiguration.getPassportUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return appConfiguration.getPassportUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return appConfiguration.getPassportUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return appConfiguration.getPassportUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return appConfiguration.getPassportUmaResourceId();
	}

	public String[] getUmaScope() {
		return new String[] { appConfiguration.getPassportUmaScope() };
	}

}
