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
import org.gluu.oxtrust.api.server.model.MetricConfig;
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

@Path(ApiConstants.CONFIGURATION + ApiConstants.METRIC)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MetricConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;
	
	@GET
	@Operation(summary = "Retrieve metric configuration", description = "Retrieve metric configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MetricConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_METRICCONFIG_READ })
	public Response retrieveMetricConfiguration() {
		try {
			log(logger, "Retrieving metric configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			MetricConfig metricConfig = new MetricConfig();
			metricConfig.setMetricReporterEnabled(oxTrustappConfiguration.getMetricReporterEnabled());
			metricConfig.setMetricReporterInterval(oxTrustappConfiguration.getMetricReporterInterval());
			metricConfig.setMetricReporterKeepDataDays(oxTrustappConfiguration.getMetricReporterKeepDataDays());
			return Response.ok(metricConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update metric configuration", description = "Update metric configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MetricConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_METRICCONFIG_WRITE })
	public Response updatePassportConfiguration(MetricConfig metricConfig) {
		try {
			log(logger, "Processing metric configuration update");
			Preconditions.checkNotNull(metricConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			appConfiguration.setMetricReporterEnabled(metricConfig.getMetricReporterEnabled());
			Integer metricReporterInterval = metricConfig.getMetricReporterInterval();
			Integer metricReporterKeepDataDays = metricConfig.getMetricReporterKeepDataDays();
			if (metricReporterInterval != null && metricReporterInterval != 0) {
				appConfiguration.setMetricReporterInterval(metricReporterInterval);
			}
			if (metricReporterKeepDataDays != null && metricReporterKeepDataDays != 0) {
				appConfiguration.setMetricReporterKeepDataDays(metricReporterKeepDataDays);
			}

			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
