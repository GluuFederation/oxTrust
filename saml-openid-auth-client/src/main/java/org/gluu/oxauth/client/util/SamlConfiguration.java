/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.util;

import org.gluu.conf.model.AppConfiguration;
import org.gluu.conf.model.AppConfigurationEntry;
import org.gluu.conf.service.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * oAuth properties and constants
 * 
 * @author Yuriy Movchan
 * @version 0.1, 03/20/2013
 */
public final class SamlConfiguration extends ConfigurationFactory<AppConfiguration, AppConfigurationEntry> {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(SamlConfiguration.class);

	private static class ConfigurationSingleton {
		static SamlConfiguration INSTANCE = new SamlConfiguration();
	}

	public static SamlConfiguration instance() {
		return ConfigurationSingleton.INSTANCE;
	}

	@Override
	protected String getDefaultConfigurationFileName() {
		return "oxidp.properties";
	}

	@Override
	protected Class<AppConfigurationEntry> getAppConfigurationType() {
		return AppConfigurationEntry.class;
	}

	@Override
	protected String getApplicationConfigurationPropertyName() {
		return "oxidp_ConfigurationEntryDN";
	}

}
