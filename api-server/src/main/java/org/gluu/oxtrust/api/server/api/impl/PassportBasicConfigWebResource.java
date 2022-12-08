package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.config.Configuration;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

@Path(ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.CONFIG)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PassportBasicConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private PassportService passportService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	
	@GET
	@Operation(summary="Get passport basic configuration",description = "Get passport basic configuration")
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_BASIC_CONFIG_READ })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PassportConfiguration[].class)), description = "Success"),
            @ApiResponse(responseCode = "500", description = "Server error")})
	public Response getPassportBasicConfig() {
		log(logger, "Get passport basic configuration");
		try {
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			return Response.ok(this.passportConfiguration.getConf()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary="Update passport basic configuration",description = "Update passport basic configuration")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PassportConfiguration[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_BASIC_CONFIG_WRITE })
	public Response updatePassportBasicConfig(Configuration configuration) {
		log(logger, "Update passport basic configuration");
		try {
			Objects.requireNonNull(configuration, "config should not be null");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.passportConfiguration.setConf(configuration);
			this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
			this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
			return Response.ok(passportService.loadConfigurationFromLdap().getPassportConfiguration().getConf())
					.build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
