/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;

/**
 * Provides operations with Linktrack API
 * @author Oleksiy Tataryn Date: 06.04.2014
 * 
 */
@Stateless
@Named("linktrackService")
public class LinktrackService implements Serializable {

	private static final long serialVersionUID = -8345266501234892594L;

	private static final String CREATE_LINK_URL_PATTERN = 
			"https://linktrack.info/api/v1_0/makeLink?login=%s&pass=%s&external_url=%s";
	@Inject
	private Logger log;

	public String newLink(@NotNull String login,@NotNull String password,@NotNull String link) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust managers
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(String.format(CREATE_LINK_URL_PATTERN, login, password, link));
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
		} catch (Exception e) {
			log.error(String.format("Exception happened during linktrack link "
					+ "creation with username: %s, password: %s,"
					+ " link: %s.", login, password, link), e);
			return null;
		}
		
		String trackedLink = null;
		if(response.getStatusLine().getStatusCode() == 201){
			try {
				trackedLink = IOUtils.toString(response.getEntity().getContent());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return trackedLink;
	}


}
