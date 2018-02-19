/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.client.Client;
import org.gluu.oxtrust.api.test.saml.SamlTestScenary;

/**
 * Typical client lifecycle test requests (registration, document uploading, etc.).
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientTestScenary {
    
    private Client client;
    
    public ClientTestScenary(Client client) {
        this.client = client;
    }
    
    /**
     * Run tests.
     */
    public void run() {
        SamlTestScenary saml = new SamlTestScenary(client);
        saml.run();
    }
}
