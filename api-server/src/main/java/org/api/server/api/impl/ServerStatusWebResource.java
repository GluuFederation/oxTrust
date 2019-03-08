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
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
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
	private ApplianceService applianceService;

	@GET
	@ApiOperation(value = "Get server status ")
	public Response getServerStatus() {
		log("Get server status");
		try {
			return Response.ok(convert(applianceService.getAppliance())).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

	private GluuServerStatus convert(GluuAppliance appliance) {
		GluuServerStatus status = new GluuServerStatus();
		status.setHostname(appliance.getHostname());
		status.setIpAddress(appliance.getIpAddress());
		status.setUptime(appliance.getSystemUptime());
		status.setPersonCount(appliance.getPersonCount());
		status.setGroupCount(appliance.getGroupCount());
		status.setLastUpdate(appliance.getLastUpdate());
		status.setFreeMemory(appliance.getFreeMemory());
		status.setFreeDiskSpace(appliance.getFreeDiskSpace());
		status.setPollingInterval(appliance.getPollingInterval());
		return status;
	}

}
