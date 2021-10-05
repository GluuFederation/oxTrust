package org.gluu.oxtrust.service;


import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

@ApplicationScoped
public class ShibbolethReloadService implements Serializable {

    private static final String SHIBBOLETH_SERVICE_PREFIX        = "shibboleth";

    private static final int  HTTP_DEFAULT_MAX_TOTAL_CONNECTIONS = 1000;
    private static final int  HTTP_DEFAULT_MAX_CONN_PER_ROUTE    = 20;

    private static final String RELYING_PARTY_SERVICE_NAME       = SHIBBOLETH_SERVICE_PREFIX + ".RelyingPartyResolverService";
    private static final String METADATA_RESOLVER_SERVICE_NAME   = SHIBBOLETH_SERVICE_PREFIX + ".MetadataResolverService";
    private static final String ATTRIBUTE_REGISTRY_SERVICE_NAME  = SHIBBOLETH_SERVICE_PREFIX + ".AttributeRegistryService";
    private static final String ATTRIBUTE_RESOLVER_SERVICE_NAME  = SHIBBOLETH_SERVICE_PREFIX + ".AttributeResolverService";
    private static final String ATTRIBUTE_FILTER_SERVICE_NAME    = SHIBBOLETH_SERVICE_PREFIX + ".AttributeFilterService";
    private static final String NAMEID_GENERATION_SERVICE_NAME   = SHIBBOLETH_SERVICE_PREFIX + ".NameIdentifierGenerationService";

    private static final String CN_IDP_HOST = "CN_IDP_HOST";


    @Inject
    private Logger log;

    private HttpClientConnectionManager connectionManager;

    @PostConstruct
    public void create() {

        try {
            log.info(">>>>> Initializing ShibbolethReloadService");
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslConnSocketFactory = new SSLConnectionSocketFactory(sslContext,hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("https",sslConnSocketFactory)
                .register("http", new PlainConnectionSocketFactory()).build();
            PoolingHttpClientConnectionManager poolingHttpConnMgr  = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            poolingHttpConnMgr.setMaxTotal(HTTP_DEFAULT_MAX_TOTAL_CONNECTIONS);
            poolingHttpConnMgr.setDefaultMaxPerRoute(HTTP_DEFAULT_MAX_CONN_PER_ROUTE);
            this.connectionManager = poolingHttpConnMgr;
            log.info(">>>> ShibbolethReloadService Initialization complete");
        }catch(Exception e) {
            this.connectionManager = null;
            log.info("Could not initialize ShibbolethReloadService",e);
        }
    }

    public boolean reloadRelyingPartyService() {

        return reloadService(RELYING_PARTY_SERVICE_NAME);
    }

    public boolean reloadMetadataResolverService() {

        return reloadService(METADATA_RESOLVER_SERVICE_NAME);
    }

    public boolean reloadAttributeRegistryService() {

        return reloadService(ATTRIBUTE_REGISTRY_SERVICE_NAME);
    }

    public boolean reloadAttributeResolverService() {

        return reloadService(ATTRIBUTE_RESOLVER_SERVICE_NAME);
    }

    public boolean reloadAttributeFilterService() {

        return reloadService(ATTRIBUTE_FILTER_SERVICE_NAME);
    }

    public boolean reloadNameIdGenerationService() {

        return reloadService(NAMEID_GENERATION_SERVICE_NAME);
    }

    public boolean reloadService(String serviceName) {

        boolean ret = false;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String urlTemplate = getIdpHost().orElse("https://localhost")+"/idp/profile/admin/reload-service?id=%s";
        String url = String.format(urlTemplate,serviceName);
        try {
            HttpGet httpGet = new HttpGet(url);
            httpClient = getHttpClient();
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String responseData = null;
            if(entity != null) {
                responseData  = EntityUtils.toString(entity);
            }
            log.debug("Service reload response for url {} is {}",url,responseData);
            if(httpResponse.getStatusLine().getStatusCode() == 200) {
                log.debug("Service reload operation for url {} succeeded",url);
                ret = true;
            }else {
                log.debug("Service reload operation for url {} failed",url);
                ret = false;
            }
        }catch(Exception e) {
            log.debug("Service reload for url " + url + "failed",e);
            ret = false;
        }finally {
            
            try {
                if(httpResponse != null)
                    httpResponse.close();
            }catch(IOException ioe) {

            }
        }
        return ret;
    }

    private CloseableHttpClient getHttpClient() {

        final CookieStore cookieStore = new BasicCookieStore();
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.DEFAULT)
            .setExpectContinueEnabled(true)
            .build();
        
        return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .build();
    }

    private Optional<String> getIdpHost() {

        return Optional.ofNullable(System.getProperty(CN_IDP_HOST));
    }
}
