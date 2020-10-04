/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.cas.auth.conf;

import org.gluu.conf.service.ConfigurationFactory;

/**
 * oxAuth properties and constants
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/25/2016
 */
public final class CasConfigurationFactory extends ConfigurationFactory<CasAppConfiguration, CasLdapAppConfiguration> {

	private static class ConfigurationSingleton {
		static CasConfigurationFactory INSTANCE = new CasConfigurationFactory();
	}

	public static CasConfigurationFactory instance() {
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
