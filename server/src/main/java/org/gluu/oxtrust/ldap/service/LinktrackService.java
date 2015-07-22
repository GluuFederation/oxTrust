/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Provides operations with Linktrack API
 * @author Oleksiy Tataryn Date: 06.04.2014
 * 
 */
@Scope(ScopeType.STATELESS)
@Name("linktrackService")
@AutoCreate
public class LinktrackService {

	private static final String CREATE_LINK_URL_PATTERN = 
			"https://linktrack.info/api/v1_0/makeLink?login=%s&pass=%s&external_url=%s";
	@Logger
	private Log log;

	public String newLink(@NotEmpty String login,@NotEmpty String password,@NotEmpty String link) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
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
