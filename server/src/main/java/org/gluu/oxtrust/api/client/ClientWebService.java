/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.client;

import com.wordnik.swagger.annotations.*;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * WS endpoint for Client actions.
 *
 * @author Shekhar L.
 */
@Path(OxTrustApiConstants.BASE_API_URL + "/client")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + "/client", description = "Clients webservice")
public class ClientWebService {

    @Inject
    private Logger logger;

    @Inject
    private ClientService clientService;

    @GET
    @ApiOperation("Get all clients")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient[].class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response list() {
        try {
            List<OxAuthClient> clients = clientService.getAllClients();
            return Response.ok(clients).build();
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{inum}")
    @ApiOperation("Get a client by inum")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response read(@PathParam("inum") String inum) {
        try {
            OxAuthClient client = clientService.getClientByInum(inum);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(client).build();
        } catch (Exception e) {
            logger.error("read() Exception", e);
            return Response.serverError().build();
        }
    }

    @POST
    @ApiOperation("Create a new client")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, responseHeaders = {
                            @ResponseHeader(name = "Location", description = "url of the created client")
                    }, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response create(OxAuthClient client, @Context UriInfo uriInfo) {
        try {
            String inum = clientService.generateInumForNewClient();
            client.setInum(inum);
            clientService.addClient(client);

            return Response.created(uriInfo.getAbsolutePathBuilder()
                    .path(ClientWebService.class)
                    .path(String.format("/%s", inum))
                    .build()).build();
        } catch (Exception e) {
            logger.error("create() Exception", e);
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/{inum}")
    @ApiOperation("Update a client by inum")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response update(@PathParam("inum") String inum, OxAuthClient client) {
        try {
            clientService.updateClient(client);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("update() Exception", e);
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{inum}")
    @ApiOperation("Delete a client by inum")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response delete(@PathParam("inum") String inum) {
        try {
            OxAuthClient client = clientService.getClientByInum(inum);
            if (client == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            clientService.removeClient(client);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return Response.serverError().build();
        }
    }

}
