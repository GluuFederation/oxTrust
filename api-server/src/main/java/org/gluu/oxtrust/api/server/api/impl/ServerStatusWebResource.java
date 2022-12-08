package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.STATUS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ServerStatusWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private ConfigurationService configurationService;
	
	@GET
	@Operation(summary = "Get server status", description = "Get server status")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuServerStatus.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SERVER_STATUS_READ })
	public Response getServerStatus() {
		log("Get server status");
		try {
			return Response.ok(convert(configurationService.getOxtrustStat(), configurationService.getConfiguration()))
					.build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

	private GluuServerStatus convert(GluuOxTrustStat configuration, GluuConfiguration conf) {
		GluuServerStatus status = new GluuServerStatus();
		status.setHostname(conf.getHostname());
		status.setIpAddress(configuration.getIpAddress());
		status.setUptime(configuration.getSystemUptime());
		status.setPersonCount(configuration.getPersonCount());
		status.setGroupCount(configuration.getGroupCount());
		status.setLastUpdate(conf.getLastUpdate());
		status.setFreeMemory(configuration.getFreeMemory());
		status.setFreeDiskSpace(configuration.getFreeDiskSpace());
		status.setPollingInterval(conf.getPollingInterval());
		return status;
	}

}
