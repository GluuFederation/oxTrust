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

import org.gluu.oxtrust.api.server.model.OxTrustJsonSetting;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.slf4j.Logger;
import org.gluu.config.oxtrust.AppConfiguration;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXTRUST_JSONSETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
		+ ApiConstants.OXTRUST_JSONSETTINGS, description = "Oxtrust json settings web service")
@ApplicationScoped
public class OxTrustJsonSettingWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private AppConfiguration oxTrustappConfiguration;

	@GET
	@ApiOperation(value = "Get json oxtrust settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxTrustJsonSetting.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
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
			setting.setScimTestMode(this.oxTrustappConfiguration.isScimTestMode());
			return Response.ok(setting).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update json oxtrust settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxTrustJsonSetting.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateOxtrustJsonSetting(OxTrustJsonSetting oxtrustJsonSetting) {
		try {
			log(logger, "Processing oxtrust json update request");
			Preconditions.checkNotNull(oxtrustJsonSetting, "Attempt to update null oxtrust json settings");
			this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			this.oxTrustappConfiguration.setOrganizationName(oxtrustJsonSetting.getOrgName());
			this.oxTrustappConfiguration.setOrgSupportEmail(oxtrustJsonSetting.getSupportEmail());
			this.oxTrustappConfiguration.setScimTestMode(oxtrustJsonSetting.isScimTestMode());
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
