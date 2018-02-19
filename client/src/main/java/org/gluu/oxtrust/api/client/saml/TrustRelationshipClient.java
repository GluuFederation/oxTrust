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
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;

/**
 * REST webservice CRUD for TrustRelationships.
 * 
 * @author Dmitry Ognyannikov
 */
public class TrustRelationshipClient extends AbstractClient<GluuSAMLTrustRelationship> {

    private static final Logger logger = LogManager.getLogger(TrustRelationshipClient.class);
    
    private static final String PATH = "/api/saml/tr"; 
    
    public TrustRelationshipClient(String baseURI) {
        super(GluuSAMLTrustRelationship.class, baseURI, PATH);
    }
    
    public List<SAMLTrustRelationshipShort> list() {
        WebTarget resource = webTarget.path("list");
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
}
