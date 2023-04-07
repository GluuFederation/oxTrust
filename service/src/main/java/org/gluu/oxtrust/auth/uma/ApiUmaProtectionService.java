/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.auth.uma;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.util.ArrayHelper;

/**
 * Provides service to protect APIs Rest service endpoints with UMA scope.
 */
@ApplicationScoped
@Named("apiUmaProtectionService")
public class ApiUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = 362749692619005003L;

	@Inject
	private AppConfiguration appConfiguration;

	@Override
	protected String getClientId() {
		return appConfiguration.getApiUmaClientId();
	}

	@Override
	protected String getClientKeyStorePassword() {
		return appConfiguration.getApiUmaClientKeyStorePassword();
	}

	@Override
	protected String getClientKeyStoreFile() {
		return appConfiguration.getApiUmaClientKeyStoreFile();
	}

	@Override
	protected String getClientKeyId() {
		return appConfiguration.getApiUmaClientKeyId();
	}

	@Override
	public String getUmaResourceId() {
		return appConfiguration.getApiUmaResourceId();
	}

	@Override
	public String[] getUmaScope() {
		return new String[] { appConfiguration.getApiUmaScope() };
	}

}
