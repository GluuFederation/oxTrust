/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.util.StringHelper;
import org.xdi.util.exception.ConfigurationException;
import org.xdi.util.init.Initializable;

/**
 * Provides OpenId configuration
 *
 * @author Yuriy Movchan Date: 12/28/2016
 */
@ApplicationScoped
@Named("openIdService")
public class OpenIdService extends Initializable implements Serializable {

	private static final long serialVersionUID = 7875838160379126796L;

	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;

	private OpenIdConfigurationResponse openIdConfiguration;

	@Override
	protected void initInternal() {
		loadOpenIdConfiguration();
	}

	private void loadOpenIdConfiguration() {
		String openIdProvider = appConfiguration.getOxAuthIssuer();
		if (StringHelper.isEmpty(openIdProvider)) {
			throw new ConfigurationException("OpenIdProvider Url is invalid");
		}
		
		openIdProvider = openIdProvider + "/.well-known/openid-configuration";

		final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(openIdProvider);
		final OpenIdConfigurationResponse response = openIdConfigurationClient.execOpenIdConfiguration();
		if ((response == null) || (response.getStatus() != 200)) {
			throw new ConfigurationException("Failed to load oxAuth configuration");
		}

		log.info("Successfully loaded oxAuth configuration");

		this.openIdConfiguration = response;
	}

	public OpenIdConfigurationResponse getOpenIdConfiguration() {
		// Call each time to allows retry
		init();

		return openIdConfiguration;
	}

}
