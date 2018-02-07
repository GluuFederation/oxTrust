/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.saml;

import org.gluu.oxtrust.api.client.AbstractClient;

/**
 * REST webservice CRUD for TrustRelationships.
 * 
 * @author Dmitry Ognyannikov
 */
public class TrustRelationshipClient extends AbstractClient<String> {
    private static final String PATH = "/apis/saml/tr"; 
    
    public TrustRelationshipClient(String baseURI) {
        super(String.class, baseURI, PATH);
    }
    
    
}
