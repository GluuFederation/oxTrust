package org.gluu.oxtrust.api.server.api.impl;

import com.google.common.collect.FluentIterable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.model.ldap.GluuLdapConfiguration;
import org.gluu.oxtrust.api.server.model.ConnectionStatusDTO;
import org.gluu.oxtrust.api.server.model.ExistingLdapConfigurationValidator;
import org.gluu.oxtrust.api.server.model.LdapConfigurationDTO;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.LdapConfigurationDtoAssembly;
import org.gluu.oxtrust.api.server.util.LdapConfigurationDuplicatedException;
import org.gluu.oxtrust.service.LdapConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.ConnectionStatus;
import org.gluu.oxtrust.util.LdapConnectionData;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.LDAP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class LDAPAuthenticationWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private LdapConfigurationService ldapConfigurationService;

	@Inject
	private ConnectionStatus connectionStatus;

	private ExistingLdapConfigurationValidator existingLdapConfigurationValidator = new ExistingLdapConfigurationValidator();

	private LdapConfigurationDtoAssembly ldapConfigurationDtoAssembly = new LdapConfigurationDtoAssembly();
	
	@GET
	@Operation(summary="Get existing configuration",description = "Get the existing configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LdapConfigurationDTO[].class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_READ })
	public Response getLdapConfiguration() {
		log(logger, "Get the existing configuration");
		try {
			List<org.gluu.oxtrust.util.LdapConfigurationDTO> result = FluentIterable
					.from(ldapConfigurationService.findLdapConfigurations())
					.transform(ldapConfigurationDtoAssembly.toDtoAsFunction()).toList();

			return Response.ok(result).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@PUT
	@Operation(summary="Update existing configuration",description = "Update an existing configuration")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LdapConfigurationDTO.class)), description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_WRITE })
	public Response updateLdapConfiguration(@Valid LdapConfigurationDTO ldapConfiguration) {
		log(logger, "Update an existing configuration");
		try {
			GluuLdapConfiguration gluuLdapConfiguration = withVersion(ldapConfiguration);
			ldapConfigurationService.update(gluuLdapConfiguration);
			return Response.ok(read(ldapConfiguration.getConfigId())).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@POST
	@Operation(summary="Create a new configuration",description = "Create a new configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LdapConfigurationDTO.class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_WRITE })
	public Response createLdapConfiguration(@Valid LdapConfigurationDTO ldapConfiguration) {
		log(logger, "Create a new configuration");
		try {
			if (existingLdapConfigurationValidator.isInvalid(ldapConfiguration)) {
				throw new LdapConfigurationDuplicatedException(ldapConfiguration.getConfigId());
			}
			GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationDtoAssembly.fromDto(ldapConfiguration);
			ldapConfigurationService.save(gluuLdapConfiguration);
			return Response.ok(read(ldapConfiguration.getConfigId())).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@DELETE
	@Path(ApiConstants.NAME_PARAM_PATH)
	@Operation(summary="Delete an existing configuration",description = "Delete an existing configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LdapConfigurationDTO[].class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_WRITE })
	public Response deleteLdapConfigurationByName(@PathParam(ApiConstants.NAME) String name) {
		log(logger, "Delete an existing configuration");
		try {
			ldapConfigurationService.remove(name);
			return getLdapConfiguration();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@POST
	@Path(ApiConstants.STATUS)
	@Operation(summary="Check status of a configuration" , description = "Check the status of a configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ConnectionStatusDTO.class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_READ })
	public Response getLdapConfigurationStatus(LdapConnectionData ldapConnectionData) {
		log(logger, "Check the status of a configuration");
		try {
			ConnectionStatusDTO connectionStatus = ConnectionStatusDTO
					.from(this.connectionStatus.isUp(ldapConnectionData));
			return Response.ok(connectionStatus).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@GET
	@Path(ApiConstants.NAME_PARAM_PATH + ApiConstants.STATUS)
	@Operation(summary= "Check the status of an existing configuration", description = "Check the status of an existing configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ConnectionStatusDTO.class)), description = "Success")})
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_LDAPAUTHENTICATION_READ })
	public Response getLdapConfigurationStatusByName(@PathParam("name") String name) {
		log(logger, "Check the status of an existing configuration");
		try {
			GluuLdapConfiguration ldapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);
			org.gluu.oxtrust.util.LdapConnectionData ldapConnectionData = LdapConnectionData.from(ldapConfiguration);
			ConnectionStatusDTO connectionStatus = ConnectionStatusDTO
					.from(this.connectionStatus.isUp(ldapConnectionData));
			return Response.ok(connectionStatus).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	private org.gluu.oxtrust.util.LdapConfigurationDTO read(String name) {
		GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);
		return ldapConfigurationDtoAssembly.toDto(gluuLdapConfiguration);
	}

	private GluuLdapConfiguration withVersion(LdapConfigurationDTO ldapConfiguration) {
		GluuLdapConfiguration result = ldapConfigurationDtoAssembly.fromDto(ldapConfiguration);
		setupVersion(ldapConfiguration, result);
		return result;
	}

	private void setupVersion(LdapConfigurationDTO ldapConfiguration, GluuLdapConfiguration result) {
		final String name = ldapConfiguration.getConfigId();
		GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);
		result.setVersion(gluuLdapConfiguration.getVersion());
	}

}
