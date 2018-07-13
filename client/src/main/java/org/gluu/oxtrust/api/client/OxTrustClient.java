/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

import java.io.UnsupportedEncodingException;

import org.gluu.oxtrust.api.client.authentication.defaultAuthenticationMethod.DefaultAuthenticationClient;
import org.gluu.oxtrust.api.client.authentication.ldap.LdapClient;
import org.gluu.oxtrust.api.client.configuration.OxAuthConfClient;
import org.gluu.oxtrust.api.client.configuration.OxTrustConfClient;
import org.gluu.oxtrust.api.client.logs.LogClient;
import org.gluu.oxtrust.api.client.logs.LogsDefClient;
import org.gluu.oxtrust.api.client.util.ClientResponseLoggingFilter;
import org.gluu.oxtrust.api.client.util.ClientRequestLoggingFilter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.gluu.oxtrust.api.client.certificates.CertificatesClient;
import org.gluu.oxtrust.api.client.group.GroupClient;
import org.gluu.oxtrust.api.client.people.PeopleClient;
import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;
import org.gluu.oxtrust.api.client.util.ClientRequestAuthorizationFilter;
import org.gluu.oxtrust.api.client.util.ClientRequestBASICAuthorizationFilter;
import org.gluu.oxtrust.api.client.util.UmaAuthorizationClient;
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

	private final LogsDefClient logsDefClient;
	private final LogClient logClient;
	private final OxAuthConfClient oxAuthConfClient;
	private final OxTrustConfClient oxTrustConfClient;
	private final LdapClient ldapClient;
	private final DefaultAuthenticationClient defaultAuthenticationClient;

	private final CertificatesClient certificatesClient;

	private final SSLContext sslContext;

	private final HostnameVerifier verifier;

	private final ResteasyClient client;

        /**
         * Production oxTrust API client constructor.
         * 
         * @param baseURI  Example: "https://your.gluu-server.com/identity/"
         * @param domain   Example: "https://your.gluu-server.com/identity/restv1/api"
         * @param umaAatClientId
         * @param umaAatClientJksPath
         * @param umaAatClientJksPassword
         * @param umaAatClientKeyId
         * @throws NoSuchAlgorithmException
         * @throws KeyManagementException 
         */
	public OxTrustClient(String baseURI, String domain, String umaAatClientId, String umaAatClientJksPath, String umaAatClientJksPassword, String umaAatClientKeyId)
			throws NoSuchAlgorithmException, KeyManagementException {
		this.baseURI = baseURI;
                
                // Authorization
                UmaAuthorizationClient umaAuthorizationClient = new UmaAuthorizationClient(domain, umaAatClientId, umaAatClientJksPath, umaAatClientJksPassword, umaAatClientKeyId);
                String authenticationToken = umaAuthorizationClient.getAuthenticationHeader();
                
                // create REST client
		sslContext = initSSLContext();
		verifier = initHostnameVerifier();
                client = new ResteasyClientBuilder()
                        .sslContext(sslContext)
                        .hostnameVerifier(verifier)
                        .register(JacksonJsonProvider.class)
                        .register(new ClientRequestAuthorizationFilter(authenticationToken))
                        .register(new ClientRequestLoggingFilter())
                        .register(new ClientResponseLoggingFilter())
                        .build();

		trustRelationshipClient = new TrustRelationshipClient(client, baseURI);
		groupClient = new GroupClient(client, baseURI);
		logsDefClient = new LogsDefClient(client, baseURI);
		logClient = new LogClient(client, baseURI);
		oxAuthConfClient = new OxAuthConfClient(client, baseURI);
		oxTrustConfClient = new OxTrustConfClient(client, baseURI);
		ldapClient = new LdapClient(client, baseURI);
		defaultAuthenticationClient = new DefaultAuthenticationClient(client, baseURI);
		peopleClient = new PeopleClient(client, baseURI);
                certificatesClient = new CertificatesClient(client, baseURI);
	}

        /**
         * Test constructor.
         * 
         * @param baseURI Example: "https://localhost/identity/"
         * @param user - test user
         * @param password - test user password
         * @throws NoSuchAlgorithmException
         * @throws KeyManagementException 
         */
	public OxTrustClient(String baseURI, String user, String password)
			throws NoSuchAlgorithmException, KeyManagementException, UnsupportedEncodingException {
		this.baseURI = baseURI;
		sslContext = initSSLContext();
		verifier = initHostnameVerifier();
                client = new ResteasyClientBuilder()
                        .sslContext(sslContext)
                        .hostnameVerifier(verifier)
                        .register(JacksonJsonProvider.class)
                        .register(new ClientRequestBASICAuthorizationFilter(user, password))
                        .register(new ClientRequestLoggingFilter())
                        .register(new ClientResponseLoggingFilter())
                        .build();
                
		// TODO: test login

		trustRelationshipClient = new TrustRelationshipClient(client, baseURI);
		groupClient = new GroupClient(client, baseURI);
		logsDefClient = new LogsDefClient(client, baseURI);
		logClient = new LogClient(client, baseURI);
		oxAuthConfClient = new OxAuthConfClient(client, baseURI);
		oxTrustConfClient = new OxTrustConfClient(client, baseURI);
		ldapClient = new LdapClient(client, baseURI);
		defaultAuthenticationClient = new DefaultAuthenticationClient(client, baseURI);
		peopleClient = new PeopleClient(client, baseURI);
                certificatesClient = new CertificatesClient(client, baseURI);
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

	public String getBaseURI() {
		return baseURI;
	}

	public TrustRelationshipClient getTrustRelationshipClient() {
		return trustRelationshipClient;
	}

	public GroupClient getGroupClient() {
		return groupClient;
	}

	public PeopleClient getPeopleClient() {
		return peopleClient;
	}

        public CertificatesClient getCertificatesClient() {
		return certificatesClient;
        }

	public void close() {
		client.close();
	}

	public LogClient getLogClient() {
		return logClient;
	}

	public LogsDefClient getLogsDefClient() {
		return logsDefClient;
	}

	public OxAuthConfClient getOxAuthConfClient() {
		return oxAuthConfClient;
	}

	public OxTrustConfClient getOxTrustConfClient() {
		return oxTrustConfClient;
	}

	public LdapClient getLdapClient() {
		return ldapClient;
	}

	public DefaultAuthenticationClient getDefaultAuthenticationClient() {
		return defaultAuthenticationClient;
	}
}
