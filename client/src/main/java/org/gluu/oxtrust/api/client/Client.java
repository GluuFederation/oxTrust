/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;

/**
 * oxTrust REST webservice client general class.
 * 
 * @author Dmitry Ognyannikov
 */
public class Client {
    
    private final String baseURI;
    
    private TrustRelationshipClient trustRelationshipClient;
    
    public Client(String baseURI, String user, String password) {
        this.baseURI = baseURI;
        
        //TODO: login
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
}
