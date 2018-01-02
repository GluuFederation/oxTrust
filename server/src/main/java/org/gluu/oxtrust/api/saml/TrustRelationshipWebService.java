/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.saml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.StringWriter;
import java.util.List;
import javax.inject.Inject;
import javax.annotation.security.DeclareRoles;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.model.TrustContact;

/**
 * WS endpoint for TrustRelationship actions.
 * 
 * @author Dmitry Ognyannikov
 */
@Path("/saml/tr")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
@DeclareRoles("administrator")
public class TrustRelationshipWebService {
    
    @Inject
    private Logger logger;
    
    @Inject
    private TrustService trustService;
    
    
    @GET
    @Path("/read/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    public String read(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            //convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            StringWriter result = new StringWriter();
            objectMapper.writeValue(result, trustRelationship);
            return result.toString();
        } catch (Exception e) {
            logger.error("read() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        }
    }
    
    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public String create(GluuSAMLTrustRelationship trustRelationship, @Context HttpServletResponse response) {
        try {
            String inum = trustService.generateInumForNewTrustRelationship();
            trustRelationship.setInum(inum);
            trustService.addTrustRelationship(trustRelationship);
            return inum;
        } catch (Exception e) {
            logger.error("create() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        }
    }
    
    @PUT
    @Path("/update/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    public String update(@PathParam("inum") String inum, GluuSAMLTrustRelationship trustRelationship, @Context HttpServletResponse response) {
        try {
            String dn = trustService.getDnForTrustRelationShip(inum);
            trustRelationship.setDn(dn);
            trustService.updateTrustRelationship(trustRelationship);
            
            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception e) {
            logger.error("update() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @DELETE
    @Path("/delete/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            trustService.removeTrustRelationship(trustRelationship);
            
            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list(@Context HttpServletResponse response) {
        try {
            List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllTrustRelationships();
            //convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            StringWriter result = new StringWriter();
            objectMapper.writeValue(result, trustRelationships);
            return result.toString();
        } catch (Exception e) {
            logger.error("list() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    
    
    @POST
    @Path("/add_metadata")
    @Produces(MediaType.TEXT_PLAIN)
    public void addMetadata(@PathParam("inum") String trustRelationshipInum, String metadata, @Context HttpServletResponse response) {
        try {
            //TODO
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @POST
    @Path("/add_attribute")
    @Produces(MediaType.TEXT_PLAIN)
    public void addAttribute(@PathParam("inum") String trustRelationshipInum, String attribute, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            //TODO
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @GET
    @Path("/generate_inum_for_new_trust_relationship")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateInumForNewTrustRelationship(@Context HttpServletResponse response) {
        try {
            return trustService.generateInumForNewTrustRelationship();
        } catch (Exception e) {
            logger.error("generateInumForNewTrustRelationship() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/get_contacts")
    @Produces(MediaType.TEXT_PLAIN)
    public String getContacts(@PathParam("inum") String trustRelationshipInum, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            List<TrustContact> list = trustService.getContacts(trustRelationship);
            //convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            StringWriter result = new StringWriter();
            objectMapper.writeValue(result, list);
            return result.toString();
        } catch (Exception e) {
            logger.error("getContacts() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @POST
    @Path("/add_certificate")
    @Produces(MediaType.TEXT_PLAIN)
    public void addCertificate(@PathParam("inum") String inum, String certificate, @Context HttpServletResponse response) {
        try {
            //TODO
        } catch (Exception e) {
            logger.error("addCertificate() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @GET
    @Path("/get_cert_for_generated_sp")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCertForGeneratedSP(@Context HttpServletResponse response) {
        try {
            String cert = "";
            //TODO
            return cert;
        } catch (Exception e) {
            logger.error("getCertForGeneratedSP() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    
}
