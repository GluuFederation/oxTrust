package org.gluu.oxtrust.api.server.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.server.model.TrustedIDPApi;
import org.gluu.oxtrust.api.server.model.RemoteIdp;
import org.gluu.oxtrust.api.server.model.SingleSignOnServices;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.model.OxTrustedIdp;
import org.gluu.oxtrust.service.TrustedIDPService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.service.JsonService;
import org.slf4j.Logger;
import javax.enterprise.context.ApplicationScoped;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Named("TrustedIDPEndPoint")
@Path(ApiConstants.BASE_API_URL + ApiConstants.INBOUNDSAML + ApiConstants.TRUSTEDIDP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TrustedIDPWebResource  extends BaseWebResource{
	
	@Inject
	private Logger logger;
	
	@Inject
	private JsonService jsonService;

	@Inject
	private TrustedIDPService trustedIDPService;
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Retrieve all trusted-idps", description = "Retrieve all trusted-idps")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TrustedIDPApi[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_READ })
	public Response gluuTrustedIdps() {
		log(logger, "get all trusted-idps ");
		try {
			log(logger, " Retrieving all trusted-idps");
			List<OxTrustedIdp> oxTrustedIdpList = trustedIDPService.getAllTrustedIDP();
			List<TrustedIDPApi> trustedIDPApiList = new ArrayList<TrustedIDPApi>();
			for(OxTrustedIdp oxTrustedIdp : oxTrustedIdpList) {
				trustedIDPApiList.add(copyParameters(oxTrustedIdp));
			}
			
			return Response.ok(trustedIDPApiList).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM + ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get TrustedIDP by inum", description = "Get a TrustedIDP by inum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TrustedIDPApi.class)), description = "Success"),
			@ApiResponse(responseCode = "404", description = "Resource not Found"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_READ })
	public Response gluuTrustedIdp( @PathParam("inum") String inum) {
		log(logger, "get trusted-idp by inum ");
		try {
			log(logger, "get trusted-idps by inum");
			OxTrustedIdp oxTrustedIdp = trustedIDPService.getTrustedIDPByInumCustom(inum);
			TrustedIDPApi trustedIDPApi = null;
			if(oxTrustedIdp == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("{\r\n" + 
						"  \"message\": \"The requested ressource was not found\"\r\n" + 
						"}").build();
			}
			
			trustedIDPApi = copyParameters(oxTrustedIdp);
			return Response.ok(trustedIDPApi).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path(ApiConstants.REMOTEIDPHOST)
	@Operation(summary = "Get TrustedIDP by remote idp host", description = "Get a TrustedIDP by remote idp host")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TrustedIDPApi.class)), description = "Success"),
			@ApiResponse(responseCode = "404", description = "Resource not Found"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_READ })
	public Response gluuTrustedIdpByRemoteIdpHost( @PathParam("remoteIdpHost") String remoteIdpHost) {
		log(logger, "get  trusted-idps by remote idp host ");
		try {
			log(logger, " Retrieving  trusted-idps by remote idp host");
			OxTrustedIdp oxTrustedIdp = trustedIDPService.getTrustedIDPByRemoteIdpHost(remoteIdpHost);
			TrustedIDPApi trustedIDPApi = null;
			if(oxTrustedIdp == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("{\r\n" + 
						"  \"message\": \"The requested ressource was not found\"\r\n" + 
						"}").build();
			}
			
			trustedIDPApi = copyParameters(oxTrustedIdp);
			return Response.ok(trustedIDPApi).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@POST
	@Operation(summary = "Add TrustedIDP", description = "Add an TrustedIDP")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = TrustedIDPApi.class)), description = "Success"),
			@ApiResponse(responseCode = "403", description = "Trust Relation already exists"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_WRITE })
	public Response createGluuTrustedIdp(TrustedIDPApi trustedIDPApi) {
		log(logger, "Add new remote idp ");
		try {
			Objects.requireNonNull(trustedIDPApi, "Attempt to create null TrustedIDP");
			if(trustedIDPApi.getRemoteIdp().getHost() == null || trustedIDPApi.getRemoteIdp().getHost().isEmpty()) {
				return Response.status(400).entity("{\r\n" + 
						"  \"message\": \"Invalid request error, please send valid remote idp host.\"\r\n" + 
						"}").build();			
			}
			OxTrustedIdp existingoxTrustedIdp = trustedIDPService.getTrustedIDPByRemoteIdpHost(trustedIDPApi.getRemoteIdp().getHost());
			if(existingoxTrustedIdp != null) {
				return Response.status(403).entity("{\r\n" + 
						"  \"message\": \"A Trust relation with remote idp host "+trustedIDPApi.getRemoteIdp().getHost()+" already exists.\"\r\n" + 
						"}").build();			
			}
			
			String inum = trustedIDPService.generateInumForTrustedIDP();
			
			//trustedIDPApi.setInum(inum);
			OxTrustedIdp oxTrustedIdp = copyAttributes(trustedIDPApi);
			oxTrustedIdp.setInum(inum);
			oxTrustedIdp.setDn(trustedIDPService.getDnForTrustedIDP(inum));
			trustedIDPService.addTrustedIDP(oxTrustedIdp);
			return Response.status(Response.Status.CREATED).entity("{\r\n" + 
					"  \"message\": \"Created\"\r\n" + 
					"}").build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PUT
	@Path(ApiConstants.REMOTEIDPHOST)
	@Operation(summary = "Update TrustedIDP", description = "Update a TrustedIDP")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TrustedIDPApi.class)), description = "Success"),
			@ApiResponse(responseCode = "404", description = "Resource not Found"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_WRITE })
	public Response updateTrustedIdp(@PathParam("remoteIdpHost") String remoteIdpHost, TrustedIDPApi trustedIDPApi)
			throws Exception {
		log(logger,"update TrustedIDP ");
		try {
			Objects.requireNonNull(trustedIDPApi, "Attempt to create null TrustedIDP");
			//get inum for remoteIDP
			OxTrustedIdp existingTrustedIDP = trustedIDPService.getTrustedIDPByRemoteIdpHost(remoteIdpHost);
			if(existingTrustedIDP == null) {
				return Response.status(Response.Status.NOT_FOUND)
						.entity("{\r\n" + 
								"  \"message\": \"The requested ressource was not found\"\r\n" + 
								"}").build();
			
			}
			OxTrustedIdp oxTrustedIdp = copyAttributes(trustedIDPApi);
			oxTrustedIdp.setInum(existingTrustedIDP.getInum());
			oxTrustedIdp.setDn(trustedIDPService.getDnForTrustedIDP(existingTrustedIDP.getInum()));
			trustedIDPService.updateTrustedIDP(oxTrustedIdp);
			
			TrustedIDPApi returnTrustedIDPApi = copyParameters(trustedIDPService.getTrustedIDPByInum(existingTrustedIDP.getInum()));
			return Response.status(Response.Status.NO_CONTENT)
					.entity(returnTrustedIDPApi).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@DELETE
	@Path(ApiConstants.REMOTEIDPHOST)
	@Operation(summary = "Delete TrustedIDP", description = "Delete a TrustedIDP")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_TRUSTED_IDP_WRITE })
	public Response deleteTrustedIdps(@PathParam("remoteIdpHost") String remoteIdpHost) throws Exception {
		log(logger,"delete TrustedIDP by host");
		try {
			OxTrustedIdp oxTrustedIdp = trustedIDPService.getTrustedIDPByRemoteIdpHost(remoteIdpHost);
			if(oxTrustedIdp != null)
				trustedIDPService.removeTrustedIDP(oxTrustedIdp);

			return Response.status(Response.Status.OK).entity("{\r\n" + 
					"  \"message\": \"OK\"\r\n" + 
					"}").build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	private OxTrustedIdp copyAttributes(TrustedIDPApi trustedIDPApi) throws IOException {
		OxTrustedIdp oxTrustedIdp = new OxTrustedIdp();
		oxTrustedIdp.setRemoteIdpHost(trustedIDPApi.getRemoteIdp().getHost());
		oxTrustedIdp.setRemoteIdpName(trustedIDPApi.getRemoteIdp().getName());
		oxTrustedIdp.setSigningCertificates(jsonService.objectToJson(trustedIDPApi.getRemoteIdp().getSigningCertificates()));
		oxTrustedIdp.setSelectedSingleSignOnService(jsonService.objectToJson(trustedIDPApi.getSelectedSingleSignOnService()));
		oxTrustedIdp.setSupportedSingleSignOnServices(jsonService.objectToJson(trustedIDPApi.getRemoteIdp().getSupportedSingleSignOnServices()));
		//oxTrustedIdp.setInum(trustedIDPApi.getInum());
		return oxTrustedIdp;
	}
	
	private TrustedIDPApi copyParameters(OxTrustedIdp oxTrustedIdp) throws IOException {
		TrustedIDPApi trustedIDPApi = new TrustedIDPApi();
		RemoteIdp remoteIdp = new RemoteIdp();
		remoteIdp.setHost(oxTrustedIdp.getRemoteIdpHost());
		remoteIdp.setName(oxTrustedIdp.getRemoteIdpName());
		remoteIdp.setSigningCertificates((List<String>)jsonService.jsonToObject(oxTrustedIdp.getSigningCertificates(),List.class));
		trustedIDPApi.setSelectedSingleSignOnService(jsonService.jsonToObject(oxTrustedIdp.getSelectedSingleSignOnService(),
				SingleSignOnServices.class)); 
		
		remoteIdp.setSupportedSingleSignOnServices((List<SingleSignOnServices>) jsonService.jsonToObject(oxTrustedIdp.getSupportedSingleSignOnServices(),
				List.class));
		remoteIdp.setId(oxTrustedIdp.getInum());
		trustedIDPApi.setRemoteIdp(remoteIdp);
		
		return trustedIDPApi;
	}

}
