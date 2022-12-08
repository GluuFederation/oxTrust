package org.gluu.oxtrust.api.server.api.impl.radius;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.api.server.api.impl.BaseWebResource;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.radius.GluuRadiusClientService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.ProductInstallationChecker;
import org.gluu.radius.model.RadiusClient;
import org.slf4j.Logger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.BASE_API_URL+ApiConstants.RADIUS_CLIENTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GluuRadiusClientWebResource extends BaseWebResource {
    
    @Inject
    private Logger logger;

    @Inject
    private GluuRadiusClientService gluuRadiusClientService;

    @Inject
    private EncryptionService encryptionService;
    
    @GET
    @Operation(summary = "Get all radius clients", description = "Get all radius clients")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RadiusClient[].class)), description = "Success"),
        @ApiResponse(responseCode = "403", description="Gluu Radius is not installed"),
        @ApiResponse(responseCode = "500", description="Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CLIENT_READ})
    public Response listRadiusClients() {

        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            
            List<RadiusClient> radiusclients = gluuRadiusClientService.getAllClients();
            return Response.ok(radiusclients).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path(ApiConstants.INUM_PARAM_PATH)
    @Operation(summary = "Get radius client by inum", description = "Get radius client by inum")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content= @Content(schema = @Schema(implementation = RadiusClient.class)), description = "Success"),
        @ApiResponse(responseCode = "403", description="Gluu Radius is not installed"),
        @ApiResponse(responseCode = "404", description="Radius client not found"),
        @ApiResponse(responseCode = "500", description="Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CLIENT_READ})
    public Response getRadiusClient(@PathParam(ApiConstants.INUM) @NotNull String inum) {

        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            
            RadiusClient radiusclient = gluuRadiusClientService.getRadiusClientByInum(inum);
            if(radiusclient == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(radiusclient).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Operation(summary = "Add new radius client", description= "Add new radius client")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RadiusClient.class)), description = "Success"),
        @ApiResponse(responseCode = "400", description = "Malformed request. Missing parameter"),
        @ApiResponse(responseCode = "403", description = "Gluu Radius is not installed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ProtectedApi(scopes={ApiScopeConstants.SCOPE_GLUU_RADIUS_CLIENT_WRITE})
    public Response addRadiusClient(RadiusClient client) {

        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            
            Objects.requireNonNull(client);
            if(client.getIpAddress() == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Client ip not specified").build();
            if(client.getName() == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Client name not specified").build();
            if(client.getPriority() == null)
                client.setPriority(1);
            if(client.getSecret() == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Client secret not specified").build();
            
            String inum = gluuRadiusClientService.generateInum();
            client.setInum(inum);
            client.setDn(gluuRadiusClientService.getRadiusClientDn(inum));
            String encryptedsecret = encryptionService.encrypt(client.getSecret());
            client.setSecret(encryptedsecret);
            gluuRadiusClientService.addRadiusClient(client);
            return Response.ok(gluuRadiusClientService.getRadiusClientByInum(inum)).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Operation(summary = "Update existing radius client", description = "Update existing radius client")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RadiusClient.class)), description = "Success"),
        @ApiResponse(responseCode = "400", description = "Malformed Request. Missing parameter"),
        @ApiResponse(responseCode = "403", description = "Gluu Radius is not installed"),
        @ApiResponse(responseCode = "404", description = "Radius client not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CLIENT_WRITE})
    public Response updateRadiusClient(RadiusClient client) {

        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            
            Objects.requireNonNull(client);
            if(client.getInum() == null)
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing radius client inum").build();
            
            String inum = client.getInum();
            RadiusClient existingclient = gluuRadiusClientService.getRadiusClientByInum(inum);
            if(existingclient == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            
            if(client.getIpAddress() != null)
                existingclient.setIpAddress(client.getIpAddress());
            if(client.getName() != null)
                existingclient.setName(client.getName());
            if(client.getPriority() != null)
                existingclient.setPriority(client.getPriority());
            if(client.getSecret() != null && !StringUtils.equals(client.getSecret(),existingclient.getSecret())) {
                String encryptedsecret = encryptionService.encrypt(client.getSecret());
                existingclient.setSecret(encryptedsecret);
            }
            gluuRadiusClientService.updateRadiusClient(existingclient);
            return Response.ok(gluuRadiusClientService.getRadiusClientByInum(inum)).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DELETE
    @Operation(summary = "Delete radius client", description = "Deletes a radius client")
    @Path(ApiConstants.INUM_PARAM_PATH)
    @ApiResponses({
        @ApiResponse(responseCode="204", description="Success"),
        @ApiResponse(responseCode="403", description="Gluu Radius is not installed"),
        @ApiResponse(responseCode="404", description="Radius client not found"),
        @ApiResponse(responseCode="500", description="Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CLIENT_WRITE})
    public Response deleteRadiusClient(@PathParam(ApiConstants.INUM) @NotNull String inum) {

        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            RadiusClient client = gluuRadiusClientService.getRadiusClientByInum(inum);
            if(client == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            gluuRadiusClientService.deleteRadiusClient(client);
            return Response.status(Response.Status.NO_CONTENT).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}