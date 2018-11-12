package org.gluu.oxtrust.api.openidconnect;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.STATUS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.STATUS,
		description = "Server status webservice")
public class ServerStatusWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private ApplianceService applianceService;

	@GET
	@ApiOperation(value = "Get server status ")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuServerStatus.class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getServerStatus() {
		try {
			return Response.ok(convert(applianceService.getAppliance())).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
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
