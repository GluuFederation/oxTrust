package org.gluu.oxtrust.api.server.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxtrust.api.server.model.OxAuthJsonConfiguration;
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

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXAUTH_JSONSETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OxAuthJsonSettingWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private String oxAuthDynamicConfigJson;
	
	@GET
	@Operation(summary = "Get json oxauth settings", description = "Gets oxAuth configuration in JSON format",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthJsonConfiguration.class)), description=Constants.RESULT_SUCCESS),
                    @ApiResponse(responseCode = "500", description = "Server error")
            }
    )
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXAUTH_JSONS_ETTING_READ })
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
	@Operation(summary = "Update json oxauth settings", description="Updates the oxAuth JSON configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = OxAuthJsonConfiguration.class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXAUTH_JSON_SETTING_WRITE })
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
