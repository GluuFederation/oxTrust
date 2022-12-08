package org.gluu.oxtrust.api.server.api.impl;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.api.server.model.OxTrustJsonSetting;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXTRUST_JSONSETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OxTrustJsonSettingWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private AppConfiguration oxTrustappConfiguration;
	
	@GET
	@Operation(summary="Get json oxtrust settings",description = "Get json oxtrust settings")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxTrustJsonSetting.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_JSON_SETTING_READ })
	public Response getOxtrustJsonSettings() {
		try {
			log(logger, "Processing oxtrust json settings retrival");
			this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			OxTrustJsonSetting setting = new OxTrustJsonSetting();
			setting.setOrgName(this.oxTrustappConfiguration.getOrganizationName());
			setting.setSupportEmail(this.oxTrustappConfiguration.getOrgSupportEmail());
			setting.setAuthenticationRecaptchaEnabled(this.oxTrustappConfiguration.isAuthenticationRecaptchaEnabled());
			setting.setCleanServiceInterval(this.oxTrustappConfiguration.getCleanServiceInterval());
			setting.setEnforceEmailUniqueness(this.oxTrustappConfiguration.getEnforceEmailUniqueness());
			setting.setPasswordResetRequestExpirationTime(
					this.oxTrustappConfiguration.getPasswordResetRequestExpirationTime());
			setting.setLoggingLevel(this.oxTrustappConfiguration.getLoggingLevel());
			return Response.ok(setting).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary="Update json oxtrust settings",description = "Update json oxtrust settings")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxTrustJsonSetting.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_JSON_SETTING_WRITE })
	public Response updateOxtrustJsonSetting(OxTrustJsonSetting oxtrustJsonSetting) {
		try {
			log(logger, "Processing oxtrust json update request");
			Preconditions.checkNotNull(oxtrustJsonSetting, "Attempt to update null oxtrust json settings");
			this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			this.oxTrustappConfiguration.setOrganizationName(oxtrustJsonSetting.getOrgName());
			this.oxTrustappConfiguration.setOrgSupportEmail(oxtrustJsonSetting.getSupportEmail());
			this.oxTrustappConfiguration
					.setPasswordResetRequestExpirationTime(oxtrustJsonSetting.getPasswordResetRequestExpirationTime());
			this.oxTrustappConfiguration.setEnforceEmailUniqueness(oxtrustJsonSetting.isEnforceEmailUniqueness());
			this.oxTrustappConfiguration.setCleanServiceInterval(oxtrustJsonSetting.getCleanServiceInterval());
			this.oxTrustappConfiguration.setLoggingLevel(oxtrustJsonSetting.getLoggingLevel());
			this.oxTrustappConfiguration
					.setAuthenticationRecaptchaEnabled(oxtrustJsonSetting.isAuthenticationRecaptchaEnabled());
			jsonConfigurationService.saveOxTrustappConfiguration(this.oxTrustappConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
