package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ConfigurationWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;

	public ConfigurationWebResource() {
	}
	
	@GET
	@Operation(summary="Get gluu configuration",description = "Retrieve gluu configuration",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CONFIGURATION_READ }))
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuConfiguration.class)), description = Constants.RESULT_SUCCESS),
            @ApiResponse(responseCode = "500", description = "Server error")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CONFIGURATION_READ })
	public Response getConfiguration() {
		log(logger, "Processing get gluu configuration");
		try {
			return Response.ok(configurationService.getConfiguration()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
