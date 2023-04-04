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
 * Provides service to protect SCIM UMA Rest service endpoints
 */
@ApplicationScoped
public class ScimUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5447131971095468865L;

	@Inject
	private AppConfiguration appConfiguration;

	protected String getClientId() {
		return appConfiguration.getScimUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return appConfiguration.getScimUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return appConfiguration.getScimUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return appConfiguration.getScimUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return appConfiguration.getScimUmaResourceId();
	}

	public String[] getUmaScope() {
		return new String[] { appConfiguration.getScimUmaScope() };
	}

}
