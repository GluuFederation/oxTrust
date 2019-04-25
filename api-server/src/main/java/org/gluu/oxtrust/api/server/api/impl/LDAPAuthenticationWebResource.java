package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
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

import org.gluu.model.ldap.GluuLdapConfiguration;
import org.gluu.oxtrust.api.server.model.ConnectionStatusDTO;
import org.gluu.oxtrust.api.server.model.ExistingLdapConfigurationValidator;
import org.gluu.oxtrust.api.server.model.LdapConfigurationDTO;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.LdapConfigurationDtoAssembly;
import org.gluu.oxtrust.api.server.util.LdapConfigurationDuplicatedException;
import org.gluu.oxtrust.ldap.service.LdapConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.ConnectionStatus;
import org.gluu.oxtrust.util.LdapConnectionData;
import org.slf4j.Logger;

import com.google.common.collect.FluentIterable;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.LDAP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
		+ ApiConstants.LDAP, description = "LDAP web service")
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
	@ApiOperation("Get the existing configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = LdapConfigurationDTO[].class, message = "Success") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response read() {
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
	@ApiOperation("Update an existing configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = LdapConfigurationDTO.class, message = "Success"),
			@ApiResponse(code = 404, message = "Not found") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response update(@Valid LdapConfigurationDTO ldapConfiguration) {
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
	@ApiOperation("Create a new configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = LdapConfigurationDTO.class, message = "Success") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response create(@Valid LdapConfigurationDTO ldapConfiguration) {
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
	@ApiOperation("Delete an existing configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = LdapConfigurationDTO[].class, message = "Success") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response delete(@PathParam(ApiConstants.NAME) String name) {
		log(logger, "Delete an existing configuration");
		try {
			ldapConfigurationService.remove(name);
			return read();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@POST
	@Path(ApiConstants.STATUS)
	@ApiOperation("Check the status of a configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = ConnectionStatusDTO.class, message = "Success") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response status(LdapConnectionData ldapConnectionData) {
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
	@ApiOperation("Check the status of an existing configuration")
	@ApiResponses(value = { @ApiResponse(code = 200, response = ConnectionStatusDTO.class, message = "Success") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response status(@PathParam("name") String name) {
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
