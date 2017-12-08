/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.saml;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.gluu.oxtrust.util.OxTrustConstants;

/**
 * WS endpoint for TrustRelationship actions.
 * 
 * @author Dmitry Ognyannikov
 */
@Path("/saml/tr")
public class TrustRelationshipWebService {
    //TODO
    
    @GET
    @Path("/read/{inum}/")
    @Produces(MediaType.APPLICATION_JSON)
    public String read(@PathParam("inum") String inum) {
        String result = null;
        //TODO
        return result;
    }
    
    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public String create() {
        String inum = null;
        //TODO
        return inum;
    }
    
    @PUT
    @Path("/update/{inum}/")
    @Produces(MediaType.TEXT_PLAIN)
    public String update(@PathParam("inum") String inum) {
        //TODO
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @DELETE
    @Path("/delete/{inum}/")
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@PathParam("inum") String inum) {
        //TODO
        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    @GET
    @Path("/list/")
    @Produces(MediaType.APPLICATION_JSON)
    public String list() {
        String list = null;
        //TODO
        return list;
    }
    
    
}
