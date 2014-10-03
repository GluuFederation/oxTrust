/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.EasySSLProtocolSocketFactory;
import org.xdi.util.EasyX509TrustManager;
import org.xdi.util.security.StringEncrypter;

public class EasyCASSLProtocolSocketFactory extends EasySSLProtocolSocketFactory {
	private static final Log LOG = LogFactory.getLog(EasyCASSLProtocolSocketFactory.class);
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	protected SSLContext createEasySSLContext(ApplicationConfiguration applicationConfiguration) {
		try {

			String password = applicationConfiguration.getCaCertsPassphrase();
			char[] passphrase = null;
			if (password != null) {
				passphrase = StringEncrypter.defaultInstance().decrypt(password, cryptoConfiguration.getEncodeSalt()).toCharArray();
			}
			KeyStore cacerts = null;
			String cacertsFN = applicationConfiguration.getCaCertsLocation();
			if (cacertsFN != null) {
				cacerts = KeyStore.getInstance(KeyStore.getDefaultType());
				FileInputStream cacertsFile = new FileInputStream(cacertsFN);
				cacerts.load(cacertsFile, passphrase);
				cacertsFile.close();
			}

			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] { new EasyX509TrustManager(cacerts) }, null);
			return context;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new HttpClientError(e.toString());
		}
	}
}
