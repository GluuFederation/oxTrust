/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.saml;

import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.AbstractClient;
import static org.gluu.oxtrust.api.client.AbstractClient.HTTP_OK;
import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.saml.SAMLTrustRelationshipShort;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.xdi.model.TrustContact;

/**
 * REST webservice CRUD for TrustRelationships.
 * 
 * @author Dmitry Ognyannikov
 */
public class TrustRelationshipClient extends AbstractClient<GluuSAMLTrustRelationship> {

    private static final Logger logger = LogManager.getLogger(TrustRelationshipClient.class);
    
    private static final String PATH = "/restv1/api/saml/tr/";
    
    public TrustRelationshipClient(Client client, String baseURI) {
        super(GluuSAMLTrustRelationship.class, client, baseURI, PATH);
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
    
    public List<SAMLTrustRelationshipShort> listAllOtherFederations(String trustRelationshipInum) {
        WebTarget resource = webTarget.path("list_all_other_federations/{inum}").resolveTemplate("inum", trustRelationshipInum);
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listAllSAMLTrustRelationships(int sizeLimit) {
        WebTarget resource = webTarget.path("list_all_saml_trust_relationships").queryParam("size_limit", sizeLimit);;
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> listDeconstructedTrustRelationships() {
        WebTarget resource = webTarget.path("list_deconstructed_trust_relationships/{inum}");
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<SAMLTrustRelationshipShort> searchTrustRelationships(String pattern, int sizeLimit) {
        WebTarget resource = webTarget.path("search_trust_relationships").queryParam("pattern", pattern).queryParam("size_limit", sizeLimit);
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List<TrustContact> getContacts(String trustRelationshipInum) {
        WebTarget resource = webTarget.path("/get_contacts/{inum}").resolveTemplate("inum", trustRelationshipInum);
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public void setContacts(String trustRelationshipInum, List<TrustContact> contacts) throws OxTrustAPIException {
        Response response = webTarget.path("set_contacts/{inum}").resolveTemplate("inum", trustRelationshipInum).request().post(Entity.entity(contacts, MediaType.APPLICATION_JSON));
        if (response.getStatus() != HTTP_OK) {
            throw new OxTrustAPIException("Response error. HTTP code: " + response.getStatus() + ", reason phrase: " + response.getStatusInfo().getReasonPhrase(), response.getStatus());
        }
    }
    
    public void addMetadata(String trustRelationshipInum, String metadata) throws OxTrustAPIException {
        Response response = webTarget.path("add_metadata/{inum}").resolveTemplate("inum", trustRelationshipInum).request().post(Entity.entity(metadata, MediaType.APPLICATION_XML));
        if (response.getStatus() != HTTP_OK) {
            throw new OxTrustAPIException("Response error. HTTP code: " + response.getStatus() + ", reason phrase: " + response.getStatusInfo().getReasonPhrase(), response.getStatus());
        }
    }
    
    public void setCertificate(String trustRelationshipInum, String certificate) throws OxTrustAPIException {
        Response response = webTarget.path("set_certificate/{inum}").resolveTemplate("inum", trustRelationshipInum).request().post(Entity.entity(certificate, MediaType.TEXT_PLAIN));
        if (response.getStatus() != HTTP_OK) {
            throw new OxTrustAPIException("Response error. HTTP code: " + response.getStatus() + ", reason phrase: " + response.getStatusInfo().getReasonPhrase(), response.getStatus());
        }
    } 
    
}