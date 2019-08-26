/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.conf;

import org.gluu.oxauth.client.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * oxAuth properties and constants
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/25/2016
 */
public final class CasConfiguration extends Configuration<CasAppConfiguration, CasLdapAppConfiguration> {

	private final Logger logger = LoggerFactory.getLogger(CasConfiguration.class);

	private static class ConfigurationSingleton {
		static CasConfiguration INSTANCE = new CasConfiguration();
	}

	public static CasConfiguration instance() {
		return ConfigurationSingleton.INSTANCE;
	}

	@Override
	protected String getDefaultConfigurationFileName() {
		return "oxcas.properties";
	}

	@Override
	protected Class<CasLdapAppConfiguration> getAppConfigurationType() {
		return CasLdapAppConfiguration.class;
	}

	@Override
	protected String getApplicationConfigurationPropertyName() {
		return "oxcas_ConfigurationEntryDN";
	}

}
