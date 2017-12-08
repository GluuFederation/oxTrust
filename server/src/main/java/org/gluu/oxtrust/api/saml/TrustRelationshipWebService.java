/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.saml;

import javax.inject.Inject;
import javax.annotation.security.DeclareRoles;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
    public String read(@PathParam("inum") String inum) {
        try {
            String result = null;
            //TODO
            return result;
        } catch (Exception e) {
            logger.error("read() Exception", e);
            return null;
        }
    }
    
    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public String create() {
        try {
            String inum = null;
            //TODO
            return inum;
        } catch (Exception e) {
            logger.error("create() Exception", e);
            return null;
        }
    }
    
    @PUT
    @Path("/update/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    public String update(@PathParam("inum") String inum) {
        try {
            //TODO
            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception e) {
            logger.error("update() Exception", e);
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @DELETE
    @Path("/delete/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@PathParam("inum") String inum) {
        try {
            //TODO
            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {
        try {
            String list = null;
            //TODO
            return list;
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    
}
