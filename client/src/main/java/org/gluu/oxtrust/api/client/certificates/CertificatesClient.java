/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gluu.oxtrust.api.client.certificates;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.Certificates;

/**
 * REST webservice for server certificates.
 * 
 * @author Dmitry Ognyannikov
 */
public class CertificatesClient {

    private static final Logger logger = LogManager.getLogger(CertificatesClient.class);
    
    protected WebTarget webTarget;
    protected Client client;
        
    private static final String PATH = "/restv1/api/certificates/";
    
    public CertificatesClient(Client client, String baseURI) {
		this.client = client;

		webTarget = client.target(baseURI).path(PATH);
    }
    
    /**
     * List Gluu Server's certificates. You can get only description of certificates, not keys.
     * 
     * @return Certificates composite object.
     */
    public Certificates list() {
        WebTarget resource = webTarget.path("list");
        
        return resource.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Certificates.class);
    }
}