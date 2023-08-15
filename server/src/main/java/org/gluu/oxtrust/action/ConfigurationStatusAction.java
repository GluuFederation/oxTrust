/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.security.cert.CertificateException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;
import org.gluu.oxauth.client.service.ClientFactory;
import org.gluu.oxauth.client.service.StatService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.stat.exporter.Months;
import org.gluu.stat.exporter.RegisterRequest;
import org.gluu.stat.exporter.RegisterResponse;
import org.gluu.stat.exporter.StatExporterResponse;
import org.gluu.stat.exporter.TokenResponse;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Action class for health check display
 * 
 * @author Oleksiy Tataryn Date: 11.14.2013
 */
@RequestScoped
@Named("configurationStatusAction")
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ConfigurationStatusAction implements Serializable {

	private static final long serialVersionUID = -7470520478553992898L;

	@Inject
	private Logger log;
	
	@Inject
	ClientService clientService;

	@Inject
	private ConfigurationService configurationService;
	
	@Inject
	protected AppConfiguration appConfiguration;
	
    @Inject
    private OpenIdService openIdService;

	private String health;
	
	private Map<String, Integer> statsData;

	@Inject
	private EncryptionService encryptionService;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private OpenIdConfigurationResponse openIdConfiguration;

	public String init() {
		openIdConfiguration = openIdService.getOpenIdConfiguration();
		if(statsData == null)
			getStats();
		
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String checkHealth() {
		GluuConfiguration configuration = configurationService.getConfiguration();
		Date lastUpdateDateTime = configuration.getLastUpdate();
		long lastUpdate = 0;
		if (lastUpdateDateTime != null) {
			lastUpdate = lastUpdateDateTime.getTime();
		}
		long currentTime = System.currentTimeMillis();
		log.debug("lastUpdate: '{}', currentTime: '{}'", lastUpdate, currentTime);
		long timeSinceLastUpdate = (currentTime - lastUpdate) / 1000;
		if (timeSinceLastUpdate >= 0 && timeSinceLastUpdate < 100) {
			this.setHealth("OK");
		} else {
			this.setHealth("FAIL");
		}
		log.debug("Set status '{}'", this.getHealth());
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}
	
	public Map<String, Integer> getStatsData() {
		return statsData;
	}

	public void setStatsData(Map<String, Integer> statsData) {
		this.statsData = statsData;
	}

	public String getHostName(String hostName) {
		if (hostName == null || StringUtils.isEmpty(hostName)) {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			hostName = context.getRequestServerName();
		}
		return hostName;
	}
	
	private void getStats() {
		try {
			String at = requestToken();
			log.debug("Access Token : 0" + at);
			
			//call Stats Service
			StatService service = ClientFactory.instance().createStatService(openIdConfiguration.getIssuer() + "/restv1/internal/stat");
			JsonNode node = service.stat("Bearer " + at, Months.getLastMonthsAsString(12), null);
			
			StatExporterResponse statExporterResponse  = prepareResponse(node);
			this.setStatsData(covertStatFormat(statExporterResponse.getData()));
			log.debug("Stat Result:" + this.getStatsData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Failed to get stats " + e.getMessage());
		}
        
    }
	
	private StatExporterResponse prepareResponse(JsonNode node) {
        StatExporterResponse response = new StatExporterResponse();
        response.setData(new HashMap<>());

        int totalMau = 42;
        final JsonNode r = node.get("response");
        if (r == null) {
        	log.debug("Unable to parse response");
            return response;
        }

        for (Map.Entry<String, JsonNode> entry : ImmutableList.copyOf(r.fields())) {
            final int mau = entry.getValue().get("monthly_active_users").asInt(-1);
            if (mau == -1) {
                continue;
            }

            response.getData().put(entry.getKey(), mau);
            totalMau += mau;
        }

        response.setMauSignature(DigestUtils.sha256Hex((Integer.toString(totalMau))));
        return response;
    }
	
	private String requestToken() {
		OkHttpClient client = getOkHttpClient();
        String tokenUrl = openIdConfiguration.getTokenEndpoint();
        
        OxAuthClient oxAuthClient = clientService.getClientByDisplayName("stat exporter");
		String clientId;
		String clientSecret;
		if (oxAuthClient != null) {
			clientId = oxAuthClient.getInum();
			clientSecret = oxAuthClient.getEncodedClientSecret();
			try {
				clientSecret = encryptionService.decrypt(clientSecret);
			} catch (EncryptionException e) {
				log.error("Failed to decrypt client secret :" + e.getMessage() );
			}
		} else {
			RegisterResponse registerResponse = registerClient(client, openIdConfiguration.getRegistrationEndpoint());
			clientId = registerResponse.getClientId();
			clientSecret = registerResponse.getClientSecret();
		}
        
		log.debug("Requesting token at " + tokenUrl + " with client_id: " + clientId);
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("username", clientId)
                .add("password", clientSecret)
                .add("scope", "openid jans_stat")
                .build();

        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", Credentials.basic(clientId, clientSecret))
                .build();

        try (Response response = client.newCall(request).execute()) {
            final String asString = response.body().string();
            if (response.isSuccessful()) {

                final TokenResponse tokenResponse = objectMapper.readValue(asString, TokenResponse.class);

                final String token = tokenResponse.getAccessToken();
                if (token != null && !token.isEmpty()) {
                	log.debug("Obtained token successfully with scopes '" + tokenResponse.getScope() + "'");
                    return token;
                }
            } else {
            	log.debug("Failed with response code " + response.code() + ", body: " + asString);
            }
        } catch (Exception e) {
        	log.error("Failed to obtain token for client :" + clientId + " due to " + e.getMessage());
        }

        log.debug("Failed to obtain token using client_credentials grant with client_id: " + clientId);
        return null;
    }
	
	private static OkHttpClient getOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	private RegisterResponse registerClient(OkHttpClient client, String registrationEndpoint) {
		log.debug("Registering client at " + registrationEndpoint);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setScope("openid jans_stat");
        registerRequest.setRedirectUris(Lists.newArrayList("https://stat_exporter"));
        registerRequest.setGrantTypes(Lists.newArrayList("client_credentials"));
        registerRequest.setClientName("stat exporter");

        try {
        	//ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(registerRequest);
            RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(registrationEndpoint)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                final String asString = response.body().string();
                if (response.isSuccessful() || response.code() == 201) {
                    RegisterResponse registerResponse = objectMapper.readValue(asString, RegisterResponse.class);
                    log.debug("Registered client_id " + registerResponse.getClientId());
                    return registerResponse;
                } else {
                	log.debug("Failed with register client, status code " + response.code() + ", body: " + asString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("Failed to dynamically register client.");
        return null;
    }
	
	private Map<String,Integer> covertStatFormat(Map<String, Integer> monthlyStats){
		Map<String,Integer> result = new HashMap<String,Integer>();
		
		for (Map.Entry<String, Integer> entry : monthlyStats.entrySet()) {			
			log.debug(entry.getKey() + ":" + entry.getValue());
	        result.put(convert(entry.getKey()),entry.getValue());
	    }
		
		return result;
	}
	
	private String convert(String monthInSomeYear) {
		DateTimeFormatter inputParser = DateTimeFormatter.ofPattern("uuuuMM");
		YearMonth yearMonth = YearMonth.parse(monthInSomeYear, inputParser);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM, uuuu", Locale.ENGLISH);
		return yearMonth.format(outputFormatter);
	}
		

}
