/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.saml;

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
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

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
    
    //TODO
    
    @GET
    @Path("/read/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    public String read(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            String result = null;
            //TODO
            return result;
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
            String inum = null;
            //TODO
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
            //TODO
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
            //TODO
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
            String list = null;
            //TODO
            return list;
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    
}
