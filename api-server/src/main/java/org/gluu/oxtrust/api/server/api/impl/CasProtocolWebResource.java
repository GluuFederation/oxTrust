package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.gluu.oxtrust.api.server.model.CasProtocolDTO;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.CASProtocolConfigurationProvider;
import org.gluu.oxtrust.api.server.util.CasProtocolDtoAssembly;
import org.gluu.oxtrust.service.CASService;
import org.gluu.oxtrust.service.ShibbolethService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.CASProtocolConfiguration;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.CAS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CasProtocolWebResource extends BaseWebResource {

	private CASProtocolConfigurationProvider casProtocolConfigurationProvider = new CASProtocolConfigurationProvider();

	private CasProtocolDtoAssembly casProtocolDtoAssembly = new CasProtocolDtoAssembly();

	@Inject
	private Logger logger;

	@Inject
	private CASService casService;

	@Inject
	private ShibbolethService shibbolethService;

	@GET
	@Operation(summary="Get existing configuration",description = "Get the existing configuration",
	security = @SecurityRequirement(name = "oauth2", scopes = {
	   		ApiScopeConstants.SCOPE_CAS_PROTOCOL_READ }))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CasProtocolDTO.class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CAS_PROTOCOL_READ })
	public Response getCasConfig() {
		log(logger, "Get the existing cas configuration");
		try {
			CASProtocolConfiguration casProtocolConfiguration = casProtocolConfigurationProvider.get();
			CasProtocolDTO casProtocolDto = casProtocolDtoAssembly.toDto(casProtocolConfiguration);
			return Response.ok(casProtocolDto).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@PUT
	@Operation(summary="Update the configuration",description = "Update the configuration",
	security = @SecurityRequirement(name = "oauth2", scopes = {
	   		ApiScopeConstants.SCOPE_CAS_PROTOCOL_WRITE }))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CasProtocolDTO.class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CAS_PROTOCOL_WRITE })
	public Response update(@Valid CasProtocolDTO casProtocol) {
		log(logger, "Update the configuration");
		try {
			CASProtocolConfiguration casProtocolConfiguration = casProtocolDtoAssembly.fromDto(casProtocol);
			casProtocolConfiguration.save(casService);
			shibbolethService.update(casProtocolConfiguration);
			return getCasConfig();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

}
