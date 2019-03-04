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

import org.api.server.model.OxAuthJsonConfiguration;
import org.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.OXAUTH_JSONSETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.OXAUTH_JSONSETTINGS, description = "OxAuth json settings web service")
@ApplicationScoped
public class OxAuthJsonSettingWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private String oxAuthDynamicConfigJson;

	@GET
	@ApiOperation(value = "Get json oxauth settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxAuthJsonConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getOxAuthJsonSettings() {
		try {
			log(logger, "Processing oxauth json settings retrieval request");
			this.oxAuthDynamicConfigJson = jsonConfigurationService.getOxAuthDynamicConfigJson();
			OxAuthJsonConfiguration configuration = new ObjectMapper().readValue(this.oxAuthDynamicConfigJson,
					OxAuthJsonConfiguration.class);
			return Response.ok(configuration).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update json oxauth settings")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = OxAuthJsonConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	public Response updateOxauthJsonSetting(OxAuthJsonConfiguration oxAuthJsonSetting) {
		try {
			log(logger, "Processing oxauth json settings update request");
			Preconditions.checkNotNull(oxAuthJsonSetting, "Attempt to update null oxauth json settings");
			String value = new ObjectMapper().writeValueAsString(oxAuthJsonSetting);
			jsonConfigurationService.saveOxAuthDynamicConfigJson(value);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
