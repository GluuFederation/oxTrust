package org.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.api.server.model.OxtrustSetting;
import org.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.persist.model.base.GluuBoolean;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.OXTRUST_SETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.OXTRUST_SETTINGS, description = "Oxtrust settings web service")
@ApplicationScoped
public class OxtrustConfigurationWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private ApplianceService applianceService;

	@GET
	@ApiOperation(value = "Get oxtrust settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxtrustSetting.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getOxtrustSettings() {
		try {
			GluuAppliance applianceUpdate = applianceService.getAppliance();
			OxtrustSetting setting = new OxtrustSetting();
			setting.setAllowPasswordReset(applianceUpdate.getPasswordResetAllowed().name());
			setting.setAllowProfileManagement(applianceUpdate.getProfileManagment().name());
			setting.setEnablePassport(applianceUpdate.getPassportEnabled().name());
			setting.setEnableScim(applianceUpdate.getScimEnabled().name());
			return Response.ok(setting).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update oxtrust settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxtrustSetting.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	public Response updateOxtrustSetting(OxtrustSetting oxtrustSetting) {
		try {
			Preconditions.checkNotNull(oxtrustSetting, "Attempt to update null oxtrust settings");
			GluuAppliance applianceUpdate = applianceService.getAppliance();
			applianceUpdate.setScimEnabled(GluuBoolean.valueOf(oxtrustSetting.getEnableScim()));
			applianceUpdate.setPassportEnabled(GluuBoolean.valueOf(oxtrustSetting.getEnablePassport()));
			applianceUpdate.setPasswordResetAllowed(GluuBoolean.valueOf(oxtrustSetting.getAllowPasswordReset()));
			applianceUpdate.setProfileManagment(GluuBoolean.valueOf(oxtrustSetting.getAllowProfileManagement()));
			applianceService.updateAppliance(applianceUpdate);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
