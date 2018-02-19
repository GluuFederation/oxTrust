/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.saml;

import java.util.List;
import javax.ws.rs.client.WebTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.AbstractClient;
import org.gluu.oxtrust.api.saml.SAMLTrustRelationshipShort;

/**
 * REST webservice CRUD for TrustRelationships.
 * 
 * @author Dmitry Ognyannikov
 */
public class TrustRelationshipClient extends AbstractClient<String> {

    private final Logger logger = LogManager.getLogger(getClass());
    
    private static final String PATH = "/api/saml/tr"; 
    
    public TrustRelationshipClient(String baseURI) {
        super(String.class, baseURI, PATH);
    }
    
    public List<SAMLTrustRelationshipShort> list() {
        WebTarget resource = webTarget.path("list");
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
}
