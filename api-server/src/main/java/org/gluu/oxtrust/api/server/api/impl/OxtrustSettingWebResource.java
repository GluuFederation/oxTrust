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
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
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
public class OxtrustSettingWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;

	@GET
	@ApiOperation(value = "Get oxtrust settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxtrustSetting.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getOxtrustSettings() {
		try {
			log(logger, "Processing oxtrust settings retrieval request");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			OxtrustSetting setting = new OxtrustSetting();
			setting.setAllowPasswordReset(configurationUpdate.getPasswordResetAllowed().toString());
			setting.setAllowProfileManagement(configurationUpdate.getProfileManagment().toString());
			setting.setEnablePassport(configurationUpdate.getPassportEnabled().toString());
			setting.setEnableScim(configurationUpdate.getScimEnabled().toString());
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
			log(logger, "Processing oxtrust settings update request");
			Preconditions.checkNotNull(oxtrustSetting, "Attempt to update null oxtrust settings");
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			configurationUpdate.setScimEnabled(valueOfName(oxtrustSetting.getEnableScim()));
			configurationUpdate.setPassportEnabled(valueOfName(oxtrustSetting.getEnablePassport()));
			configurationUpdate.setPasswordResetAllowed(valueOfName(oxtrustSetting.getAllowPasswordReset()));
			configurationUpdate.setProfileManagment(valueOfName(oxtrustSetting.getAllowProfileManagement()));
			configurationService.updateConfiguration(configurationUpdate);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	GluuBoolean valueOfName(String name) {
		GluuBoolean result = GluuBoolean.DISABLED;
		for (GluuBoolean value : GluuBoolean.values()) {
			String valueName = value.toString();
			if (valueName.equalsIgnoreCase(name)) {
				result = value;
			}
		}
		return result;
	}

}
