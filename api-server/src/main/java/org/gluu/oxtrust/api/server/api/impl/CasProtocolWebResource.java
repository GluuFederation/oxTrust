package org.gluu.oxtrust.api.server.api.impl;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.gluu.oxtrust.api.server.model.CasProtocolDTO;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.CASProtocolConfigurationProvider;
import org.gluu.oxtrust.api.server.util.CasProtocolDtoAssembly;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.ldap.service.ShibbolethService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.CASProtocolConfiguration;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.CAS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
		+ ApiConstants.CAS, description = "Cas protocal web service")
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
	@ApiOperation("Get the existing configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = CasProtocolDTO.class, message = "Success") })
	@ProtectedApi(scopes = { READ_ACCESS })
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
	@ApiOperation("Update the configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = CasProtocolDTO.class, message = "Success") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
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
