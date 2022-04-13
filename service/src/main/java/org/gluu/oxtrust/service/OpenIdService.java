/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.util.StringHelper;
import org.gluu.util.exception.ConfigurationException;
import org.gluu.util.init.Initializable;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.slf4j.Logger;
import org.gluu.oxauth.client.OpenIdConfigurationClient;
import org.gluu.oxauth.client.OpenIdConfigurationResponse;

/**
 * Provides OpenId configuration
 *
 * @author Yuriy Movchan Date: 12/28/2016
 */
@ApplicationScoped
@Named("openIdService")
public class OpenIdService extends Initializable implements Serializable {

    private static final long serialVersionUID = 7875838160379126796L;

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    private OpenIdConfigurationResponse openIdConfiguration;

    @Override
    protected void initInternal() {
        try {
            loadOpenIdConfiguration();
        } catch (IOException ex) {
            throw new ConfigurationException("Failed to load oxAuth configuration");
        }
    }

    private void loadOpenIdConfiguration() throws IOException {
        String openIdProvider = appConfiguration.getOxAuthIssuer();
        if (StringHelper.isEmpty(openIdProvider)) {
            throw new ConfigurationException("OpenIdProvider Url is invalid");
        }

        openIdProvider = openIdProvider + "/.well-known/openid-configuration";

        final OpenIdConfigurationClient openIdConfigurationClient = new OpenIdConfigurationClient(openIdProvider);
        ApacheHttpClient43Engine httpEngine = new ApacheHttpClient43Engine();
        httpEngine.setFollowRedirects(true);
        openIdConfigurationClient.setExecutor(httpEngine);
        final OpenIdConfigurationResponse response = openIdConfigurationClient.execOpenIdConfiguration();
        if ((response == null) || (response.getStatus() != 200)) {
            log.info("Failed to load oxAuth configuration. Http code ( {} ). Body: {}", response.getStatus(),response.getEntity());
            throw new ConfigurationException("Failed to load oxAuth configuration");
        }

        log.info("Successfully loaded oxAuth configuration");

        this.openIdConfiguration = response;
    }

    public OpenIdConfigurationResponse getOpenIdConfiguration() {
        // Call each time to allows retry
        init();

        return openIdConfiguration;
    }

}
