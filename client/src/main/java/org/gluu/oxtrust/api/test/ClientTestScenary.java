/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.client.OxTrustClient;

/**
 * Typical client lifecycle test requests (registration, document uploading, etc.).
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientTestScenary {
    
    
    private final OxTrustClient client;
    
    public ClientTestScenary(OxTrustClient client) {
        this.client = client;
    }
    
    /**
     * Run tests.
     * 
     * @throws APITestException
     * @throws OxTrustAPIException
     */
    public void run() throws APITestException, OxTrustAPIException {
        AuthenticationMethodTestCase authenticationMethodTestCase = new AuthenticationMethodTestCase(client);
        authenticationMethodTestCase.run();

        OxTrustConfigurationTestCase oxTrustConfigurationTestCase = new OxTrustConfigurationTestCase(client);
        oxTrustConfigurationTestCase.run();

        OxAuthConfigurationTestCase oxAuthConfigurationTestCase = new OxAuthConfigurationTestCase(client);
        oxAuthConfigurationTestCase.run();

        LogsTestCase logsTestCase = new LogsTestCase(client);
        logsTestCase.run();

        SamlTestScenary saml = new SamlTestScenary(client);
        saml.run();
        CertificatesTestScenary certificates = new CertificatesTestScenary(client);
        certificates.run();
    }
}
