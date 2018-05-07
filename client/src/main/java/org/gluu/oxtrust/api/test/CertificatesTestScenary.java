/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.Certificates;
import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.client.certificates.CertificatesClient;

/**
 * Certificates-related test requests.
 * 
 * @author Dmitry Ognyannikov
 */
public class CertificatesTestScenary {
    
    private static final Logger logger = LogManager.getLogger(CertificatesTestScenary.class);
    
    private final OxTrustClient client;
    
    public CertificatesTestScenary(OxTrustClient client) {
        this.client = client;
    }
    
    /**
     * Run tests.
     * 
     * @throws APITestException
     * @throws OxTrustAPIException
     */
    public void run() throws APITestException, OxTrustAPIException {
        CertificatesClient certificatesClient = client.getCertificatesClient();
        
        Certificates certificates = certificatesClient.list();
    }
    
}
