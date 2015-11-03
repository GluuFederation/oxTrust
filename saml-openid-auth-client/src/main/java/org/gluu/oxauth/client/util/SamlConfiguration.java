/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.util;

import org.gluu.oxauth.client.conf.AppConfiguration;
import org.gluu.oxauth.client.conf.Configuration;
import org.gluu.oxauth.client.conf.LdapAppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * oAuth properties and constants
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/20/2013
 */
public final class SamlConfiguration extends Configuration<AppConfiguration, LdapAppConfiguration> {

	private final Logger logger = LoggerFactory.getLogger(SamlConfiguration.class);

	private AppConfiguration appConfiguration;

	private static class ConfigurationSingleton {
		static SamlConfiguration INSTANCE = new SamlConfiguration();
	}

	public static SamlConfiguration instance() {
		return ConfigurationSingleton.INSTANCE;
	}

	@Override
	protected String getLdapConfigurationFileName() {
		return "oxidp-ldap.properties";
	}

	@Override
	protected Class<LdapAppConfiguration> getAppConfigurationType() {
		return LdapAppConfiguration.class;
	}

	@Override
	protected AppConfiguration initAppConfiguration(LdapAppConfiguration ldapAppConfiguration) {
		this.appConfiguration = ldapAppConfiguration.getApplication();
		return this.appConfiguration;
	}

	public AppConfiguration getAppConfiguration() {
		return appConfiguration;
	}

}
