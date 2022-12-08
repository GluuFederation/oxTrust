package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.api.server.model.RptConfig;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.CONFIGURATION + ApiConstants.RPT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RptConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;
	
	@GET
	@Operation(summary = "Retrieve rpt configuration", description = "Retrieve rpt configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RptConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_RPT_CONFIG_READ })
	public Response retrieveRptConfiguration() {
		try {
			log(logger, "Retrieving rpt configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			RptConfig rptConfig = new RptConfig();
			rptConfig.setRptConnectionPoolCustomKeepAliveTimeout(
					oxTrustappConfiguration.getRptConnectionPoolCustomKeepAliveTimeout());
			rptConfig.setRptConnectionPoolDefaultMaxPerRoute(
					oxTrustappConfiguration.getRptConnectionPoolDefaultMaxPerRoute());
			rptConfig.setRptConnectionPoolMaxTotal(oxTrustappConfiguration.getRptConnectionPoolMaxTotal());
			rptConfig.setRptConnectionPoolUseConnectionPooling(
					oxTrustappConfiguration.isRptConnectionPoolUseConnectionPooling());
			rptConfig.setRptConnectionPoolValidateAfterInactivity(
					oxTrustappConfiguration.getRptConnectionPoolValidateAfterInactivity());
			return Response.ok(rptConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update rpt configuration", description = "Update rpt configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RptConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_RPT_CONFIG_WRITE })
	public Response updateRptConfiguration(RptConfig rptConfig) {
		try {
			log(logger, "Processing rpt configuration update");
			Preconditions.checkNotNull(rptConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (rptConfig.getRptConnectionPoolCustomKeepAliveTimeout() != null) {
				appConfiguration.setRptConnectionPoolCustomKeepAliveTimeout(
						rptConfig.getRptConnectionPoolCustomKeepAliveTimeout());
			}
			if (rptConfig.getRptConnectionPoolDefaultMaxPerRoute() != null) {
				appConfiguration
						.setRptConnectionPoolDefaultMaxPerRoute(rptConfig.getRptConnectionPoolDefaultMaxPerRoute());
			}
			if (rptConfig.getRptConnectionPoolMaxTotal() != null) {
				appConfiguration.setRptConnectionPoolMaxTotal(rptConfig.getRptConnectionPoolMaxTotal());
			}
			if (rptConfig.getRptConnectionPoolValidateAfterInactivity() != null) {
				appConfiguration.setRptConnectionPoolValidateAfterInactivity(
						rptConfig.getRptConnectionPoolValidateAfterInactivity());
			}
			appConfiguration
					.setRptConnectionPoolUseConnectionPooling(rptConfig.getRptConnectionPoolUseConnectionPooling());
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
