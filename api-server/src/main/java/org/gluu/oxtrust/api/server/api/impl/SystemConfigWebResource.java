package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.server.model.SystemConfig;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.CONFIGURATION + ApiConstants.SYSTEM)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SystemConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;
	
	@GET
	@Operation(summary = "Retrieve system configuration", description = "Retrieve system configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SYSTEM_CONFIG_READ })
	public Response retrieveSystemConfiguration() {
		try {
			log(logger, "Retrieving system configuration");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			SystemConfig systemConfig = new SystemConfig();
			systemConfig.setAllowPasswordReset(String.valueOf(configurationUpdate.isPasswordResetAllowed()));
			systemConfig.setAllowProfileManagement(String.valueOf(configurationUpdate.isProfileManagment()));
			systemConfig.setEnablePassport(String.valueOf(configurationUpdate.isPassportEnabled()));
			systemConfig.setEnableScim(String.valueOf(configurationUpdate.isScimEnabled()));
			systemConfig.setEnableSaml(String.valueOf(configurationUpdate.isSamlEnabled()));
			systemConfig.setEnableRadius(String.valueOf(configurationUpdate.isRadiusEnabled()));
			return Response.ok(systemConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update system configuration", description = "Update system configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SYSTEM_CONFIG_WRITE })
	public Response updateSystemConfiguration(SystemConfig systemConfig) {
		try {
			log(logger, "Updating system configuration");
			Preconditions.checkNotNull(systemConfig, "Attempt to update with null");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			configurationUpdate.setScimEnabled(Boolean.valueOf(systemConfig.getEnableScim()));
			configurationUpdate.setPassportEnabled(Boolean.valueOf(systemConfig.getEnablePassport()));
			configurationUpdate.setSamlEnabled(Boolean.valueOf(systemConfig.getEnableSaml()));
			configurationUpdate.setRadiusEnabled(Boolean.valueOf(systemConfig.getEnableRadius()));
			configurationUpdate.setPasswordResetAllowed(Boolean.valueOf(systemConfig.getAllowPasswordReset()));
			configurationUpdate.setProfileManagment(Boolean.valueOf(systemConfig.getAllowProfileManagement()));
			configurationService.updateConfiguration(configurationUpdate);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
