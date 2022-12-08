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

import org.gluu.oxtrust.api.server.model.OxtrustSetting;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXTRUST_SETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OxtrustSettingWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;
	
	@GET
	@Operation(summary="Get oxtrust settings",description = "Get oxtrust settings")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxtrustSetting.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SETTING_READ })
	public Response getOxtrustSettings() {
		try {
			log(logger, "Processing oxtrust settings retrieval request");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			OxtrustSetting setting = new OxtrustSetting();
			setting.setAllowPasswordReset(String.valueOf(configurationUpdate.isPasswordResetAllowed()));
			setting.setAllowProfileManagement(String.valueOf(configurationUpdate.isProfileManagment()));
			setting.setEnablePassport(String.valueOf(configurationUpdate.isPassportEnabled()));
			setting.setEnableScim(String.valueOf(configurationUpdate.isScimEnabled()));
			return Response.ok(setting).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary="Update oxtrust settings",description = "Update oxtrust settings")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxtrustSetting.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SETTING_WRITE })
	public Response updateOxtrustSetting(OxtrustSetting oxtrustSetting) {
		try {
			log(logger, "Processing oxtrust settings update request");
			Preconditions.checkNotNull(oxtrustSetting, "Attempt to update null oxtrust settings");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			configurationUpdate.setScimEnabled(Boolean.valueOf(oxtrustSetting.getEnableScim()));
			configurationUpdate.setPassportEnabled(Boolean.valueOf(oxtrustSetting.getEnablePassport()));
			configurationUpdate.setPasswordResetAllowed(Boolean.valueOf(oxtrustSetting.getAllowPasswordReset()));
			configurationUpdate.setProfileManagment(Boolean.valueOf(oxtrustSetting.getAllowProfileManagement()));
			configurationService.updateConfiguration(configurationUpdate);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
