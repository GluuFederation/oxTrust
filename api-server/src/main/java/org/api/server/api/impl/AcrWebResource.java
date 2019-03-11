package org.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.api.server.model.AuthenticationMethod;
import org.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.ACRS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.ACRS, description = "Acrs webservice")
@ApplicationScoped
public class AcrWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ApplianceService applianceService;

	public AcrWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all acrs")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = AuthenticationMethod.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getCurrentAuthentication() {
		try {
			GluuAppliance appliance = applianceService.getAppliance();
			AuthenticationMethod method = new AuthenticationMethod();
			method.setDefaultAcr(appliance.getAuthenticationMode());
			method.setOxtrustAcr(appliance.getOxTrustAuthenticationMode());
			return Response.ok(method).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
