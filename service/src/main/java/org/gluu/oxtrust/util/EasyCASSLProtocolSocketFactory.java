/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.HttpClientError;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.util.EasySSLProtocolSocketFactory;
import org.gluu.util.EasyX509TrustManager;
import org.slf4j.Logger;

@ApplicationScoped
public class EasyCASSLProtocolSocketFactory extends EasySSLProtocolSocketFactory {

	@Inject
	private Logger log;
	
	@Inject
	private EncryptionService encryptionService;
	
	@Inject
	private AppConfiguration appConfiguration;
	
	protected SSLContext createEasySSLContext(AppConfiguration appConfiguration) {
		try {

			KeyStore cacerts = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] { new EasyX509TrustManager(cacerts) }, null);
			return context;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new HttpClientError(e.toString());
		}
	}
}
