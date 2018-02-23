/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.saml;

import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
    
    private static final String PATH = "/restv1/api/saml/tr/"; 
    
    public TrustRelationshipClient(String baseURI, SSLContext sslContext, HostnameVerifier verifier) {
        super(GluuSAMLTrustRelationship.class, baseURI, PATH, sslContext, verifier);
    }
    
    public List<SAMLTrustRelationshipShort> list() {
        WebTarget resource = webTarget.path("list");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listAllFederations() {
        WebTarget resource = webTarget.path("list_all_federations");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listAllActiveTrustRelationships() {
        WebTarget resource = webTarget.path("list_all_active_trust_relationships");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listAllOtherFederations() {
        WebTarget resource = webTarget.path("list_all_other_federations");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listAllSAMLTrustRelationships() {
        WebTarget resource = webTarget.path("list_all_saml_trust_relationships");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listDeconstructedTrustRelationships() {
        WebTarget resource = webTarget.path("list_deconstructed_trust_relationships");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> searchTrustRelationships(String pattern, int sizeLimit) {
        WebTarget resource = webTarget.path(java.text.MessageFormat.format("search_trust_relationships/{0}", new Object[]{pattern}));
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
}
