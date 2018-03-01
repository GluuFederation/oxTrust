/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.client;

import javax.inject.Inject;

import java.util.List;

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

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WS endpoint for Client actions.
 * 
 * @author Shekhar L.
 */
@Path("/client")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
@DeclareRoles("administrator")
public class ClientWebService {
    
    @Inject
    private Logger logger;    

	@Inject
	private ClientService clientService;
    
    //TODO
    
    @GET
    @Path("/read/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    public String read(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            String result = null;
            OxAuthClient client = clientService.getClientByInum(inum);
            //TODO
            ObjectMapper mapper = new ObjectMapper();
            String clientJson = mapper.writeValueAsString(client);
            response.setStatus(HttpServletResponse.SC_OK);
            return clientJson;
        } catch (Exception e) {
            logger.error("read() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        }
    }
    
    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public String create(OxAuthClient client , @Context HttpServletResponse response) {
        try {
            String inum = clientService.generateInumForNewClient();
            client.setInum(inum);
            clientService.addClient(client);
            response.setStatus(HttpServletResponse.SC_CREATED);
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
    public String update(@PathParam("inum") String inum, OxAuthClient client, @Context HttpServletResponse response) {
        try {
            //TODO
        	clientService.updateClient(client);
        	OxAuthClient updatedClient = clientService.getClientByInum(inum);

            ObjectMapper mapper = new ObjectMapper();
            String clientJson = mapper.writeValueAsString(updatedClient);
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
        	
            List<OxAuthClient> clientList = clientService.getAllClients();
            ObjectMapper mapper = new ObjectMapper();
            String clientListJson = mapper.writeValueAsString(clientList);
            response.setStatus(HttpServletResponse.SC_OK);
            return clientListJson;
            
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    
}
