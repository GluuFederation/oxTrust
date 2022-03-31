package org.api.server;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.gluu.oxauth.client.TokenRequest;
import org.gluu.oxauth.client.uma.UmaClientFactory;
import org.gluu.oxauth.client.uma.UmaTokenService;
import org.gluu.oxauth.model.common.AuthenticationMethod;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.crypto.OxAuthCryptoProvider;
import org.gluu.oxauth.model.token.ClientAssertionType;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxauth.model.uma.UmaTokenResponse;
import org.gluu.util.StringHelper;
import org.gluu.util.security.SecurityProviderUtility;
import org.junit.Assert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseApiTest {
	protected String rpt = "";
	private String ticket;
	private String asUri;
	private HttpClient client;
	private String umaAatClientId = "800-b526-43a0-b5e5-e39c7a970386";
	private String umaAatClientKeyId = "";
	private String umaAatClientJksPath = "/home/gasmyr/Desktop/api-rp.jks";
	private String umaAatClientJksPassword = "secret";
	protected String CONTENT_TYPE = "Content-Type";
	protected static final String BASE_URL = "https://gluu.gasmyr.com/identity/restv1";
	protected ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	static {
		SecurityProviderUtility.installBCProvider();
	}

	private void init() {
		if (client == null) {
			try {
				client = createAcceptSelfSignedCertificateClient();
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				e.printStackTrace();
			}
		}
	}

	private TokenRequest getAuthorizationTokenRequest(UmaMetadata umaMetadata) {
		try {
			if (StringHelper.isEmpty(umaAatClientJksPath) || StringHelper.isEmpty(umaAatClientJksPassword)) {
				throw new IllegalArgumentException("UMA JKS keystore path or password is empty");
			}
			OxAuthCryptoProvider cryptoProvider;
			try {
				cryptoProvider = new OxAuthCryptoProvider(umaAatClientJksPath, umaAatClientJksPassword, null);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to initialize crypto provider");
			}
			String keyId = umaAatClientKeyId;
			System.out.println("UMA KEY ID:" + keyId);
			if (StringHelper.isEmpty(keyId)) {
				List<String> aliases = cryptoProvider.getKeys();
				System.out.println("KEY ALIASES : " + aliases.size());
				if (aliases.size() > 0) {
					keyId = aliases.get(0);
				}
			}
			if (StringHelper.isEmpty(keyId)) {
				System.out.println("UMA KEY ID IS EMPTY");
				throw new IllegalArgumentException("UMA keyId is empty");
			}
			TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
			tokenRequest.setAuthenticationMethod(AuthenticationMethod.PRIVATE_KEY_JWT);
			tokenRequest.setAuthUsername(umaAatClientId);
			tokenRequest.setCryptoProvider(cryptoProvider);
			tokenRequest.setAlgorithm(cryptoProvider.getSignatureAlgorithm(keyId));
			tokenRequest.setKeyId(keyId);
			tokenRequest.setAudience(umaMetadata.getTokenEndpoint());
			return tokenRequest;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to get client token", ex);
		}

	}

	protected HttpResponse handle(HttpUriRequest request) {
		init();
		if (client != null) {
			try {
				HttpResponse response = client.execute(request);
				request.setHeader("Authorization", getAuthenticationHeader());
				System.out.println("Header: " + request.getFirstHeader("Authorization").getValue());
				if (String.valueOf(response.getStatusLine().getStatusCode())
						.equals(String.valueOf(HttpStatus.SC_UNAUTHORIZED))) {
					extractTicketAndAsUri(response);
					System.out.println("ticket: " + ticket);
					System.out.println("asUri: " + asUri);
					getAuthorizedRpt(asUri, ticket);
					if (rpt != null) {
						System.out.println("Running actual request: " + request.getURI());
						String authenticationHeader = getAuthenticationHeader();
						System.out.println("Header value:" + authenticationHeader);
						request.setHeader("Authorization", authenticationHeader);
						System.out.println("Header: " + request.getFirstHeader("Authorization").getValue());
						return client.execute(request);
					} else {
						throw new IllegalArgumentException("Error getting RPT");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		}
		return null;
	}

	private void extractTicketAndAsUri(HttpResponse response) {
		Header header = response.getFirstHeader("WWW-Authenticate");
		String[] elements = header.getValue().split(",");
		ticket = elements[elements.length - 1].split("=")[1];
		asUri = elements[elements.length - 2].split("=")[1];
		Assert.assertNotNull(ticket);
		Assert.assertNotNull(asUri);
	}

	private void getAuthorizedRpt(String asUri, String ticket) {
		try {
			UmaMetadata umaMetadata = UmaClientFactory.instance().createMetadataService(asUri).getMetadata();
			if (umaMetadata == null) {
				throw new IllegalArgumentException(
						String.format("Failed to load valid UMA metadata configuration from: %s", asUri));
			}
			TokenRequest tokenRequest = getAuthorizationTokenRequest(umaMetadata);
			UmaTokenService tokenService = UmaClientFactory.instance().createTokenService(umaMetadata);
			UmaTokenResponse rptResponse = tokenService.requestJwtAuthorizationRpt(
					ClientAssertionType.JWT_BEARER.toString(), tokenRequest.getClientAssertion(),
					GrantType.OXAUTH_UMA_TICKET.getValue(), ticket, null, null, null, null, null);
			if (rptResponse == null) {
				throw new IllegalArgumentException("UMA RPT token response is invalid");
			}
			if (StringUtils.isBlank(rptResponse.getAccessToken())) {
				throw new IllegalArgumentException("UMA RPT is invalid");
			}
			this.rpt = rptResponse.getAccessToken();
			System.out.println("RPT IS:" + this.rpt);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
		HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
		SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
		return HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setSSLSocketFactory(connectionFactory).build();
	}

	public String getRpt() {
		return rpt;
	}

	public void setRpt(String rpt) {
		this.rpt = rpt;
	}

	protected String getAuthenticationHeader() {
		return StringHelper.isEmpty(rpt) ? null : "Bearer " + rpt;
	}
}
