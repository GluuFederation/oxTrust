/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.HttpClientError;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.EasySSLProtocolSocketFactory;
import org.xdi.util.EasyX509TrustManager;

public class EasyCASSLProtocolSocketFactory extends EasySSLProtocolSocketFactory {

	@Inject
	private Logger log;
	
	@Inject
	private EncryptionService encryptionService;
	
	@Inject
	private AppConfiguration appConfiguration;
	
	protected SSLContext createEasySSLContext(AppConfiguration appConfiguration) {
		try {

			String password = appConfiguration.getCaCertsPassphrase();
			char[] passphrase = null;
			if (password != null) {
				passphrase = encryptionService.decrypt(password).toCharArray();
			}
			KeyStore cacerts = null;
			String cacertsFN = appConfiguration.getCaCertsLocation();
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
			log.error(e.getMessage(), e);
			throw new HttpClientError(e.toString());
		}
	}
}
