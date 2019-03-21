package org.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.STATUS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ServerStatusWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private ConfigurationService configurationService;

	@GET
	@ApiOperation(value = "Get server status ")
	public Response getServerStatus() {
		log("Get server status");
		try {
			return Response.ok(convert(configurationService.getConfiguration())).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

	private GluuServerStatus convert(GluuConfiguration configuration) {
		GluuServerStatus status = new GluuServerStatus();
		status.setHostname(configuration.getHostname());
		status.setIpAddress(configuration.getIpAddress());
		status.setUptime(configuration.getSystemUptime());
		status.setPersonCount(configuration.getPersonCount());
		status.setGroupCount(configuration.getGroupCount());
		status.setLastUpdate(configuration.getLastUpdate());
		status.setFreeMemory(configuration.getFreeMemory());
		status.setFreeDiskSpace(configuration.getFreeDiskSpace());
		status.setPollingInterval(configuration.getPollingInterval());
		return status;
	}

}
