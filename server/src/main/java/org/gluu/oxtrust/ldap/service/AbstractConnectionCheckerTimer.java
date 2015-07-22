/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.site.ldap.LDAPConnectionProvider;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

/**
 * Check periodically if application lost connection to LDAP server and make
 * attempt to reconnect to LDAP server if needed.
 * 
 * @author Yuriy Movchan Date: 04.13.2011
 */
public class AbstractConnectionCheckerTimer {

	@Logger
	private Log log;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	

	protected void processImpl(FileConfiguration configuration, LDAPConnectionProvider connectionProvider) {
		if ((configuration == null) || (connectionProvider == null)) {
			return;
		}

		// Check if application has connection to LDAP server
		boolean isConnected = connectionProvider.isConnected();
		if (!isConnected) {
			log.error("Gluu IDP can't connect to LDAP server. Check host, port, credentials in the properties file.");
			// Reload configuration
			configuration.reloadProperties();

			try {
				// Make attempt to reconnect to LDAP server
				connectionProvider.init(PropertiesDecrypter.decryptProperties(configuration.getProperties(),cryptoConfiguration.getEncodeSalt()));
				isConnected = connectionProvider.isConnected();
				if (isConnected) {
					log.info("Connection to LDAP server was restored");
				} else {
					log.info("Connection to LDAP server wasn't restored");
				}
			} catch (Exception ex) {
				log.error("Failed to reconnect to the LDAP server", ex);
			}
		}
	}

}
