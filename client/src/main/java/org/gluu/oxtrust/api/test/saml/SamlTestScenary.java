/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test.saml;

import org.gluu.oxtrust.api.client.Client;
import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;

/**
 * SAML-relater test requests.
 * 
 * @author Dmitry Ognyannikov
 */
public class SamlTestScenary {
    
    private Client client;
    
    public SamlTestScenary(Client client) {
        this.client = client;
    }
    
    /**
     * Run tests.
     */
    public void run() {
        TrustRelationshipClient samlClient = client.getTrustRelationshipClient();
        
        //TODO
    }
}
