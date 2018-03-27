/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

import org.gluu.oxtrust.api.client.util.ClientResponseLoggingFilter;
import org.gluu.oxtrust.api.client.util.ClientRequestLoggingFilter;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.gluu.oxtrust.api.client.group.GroupClient;
import org.gluu.oxtrust.api.client.people.PeopleClient;
import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * oxTrust REST webservice client general class.
 * 
 * @author Dmitry Ognyannikov
 */
public class OxTrustClient {

	private final String baseURI;

	private final TrustRelationshipClient trustRelationshipClient;

	private final GroupClient groupClient;
        
	private final PeopleClient peopleClient;

	private final SSLContext sslContext;

	private final HostnameVerifier verifier;

	private final ResteasyClient client;

	public OxTrustClient(String baseURI, String user, String password)
			throws NoSuchAlgorithmException, KeyManagementException {
		this.baseURI = baseURI;
		sslContext = initSSLContext();
		verifier = initHostnameVerifier();
                client = new ResteasyClientBuilder()
                        .sslContext(sslContext)
                        .hostnameVerifier(verifier)
                        .register(JacksonJsonProvider.class)
                        .register(new ClientRequestLoggingFilter())
                        .register(new ClientResponseLoggingFilter())
                        .build();
                
		// TODO: login

		trustRelationshipClient = new TrustRelationshipClient(client, baseURI);
		groupClient = new GroupClient(client, baseURI);
		peopleClient = new PeopleClient(client, baseURI);

	}

	private SSLContext initSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} }, new java.security.SecureRandom());
		return context;
	}

	private HostnameVerifier initHostnameVerifier() {
		return new HostnameVerifier() {
			@Override
			public boolean verify(String string, SSLSession ssls) {
				return true;
			}
		};
	}

	/**
	 * @return the baseURI
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @return the trustRelationshipClient
	 */
	public TrustRelationshipClient getTrustRelationshipClient() {
		return trustRelationshipClient;
	}

	public GroupClient getGroupClient() {
		return groupClient;
	}

	public PeopleClient getPeopleClient() {
		return peopleClient;
	}

	public void close() {
		client.close();
	}
}
