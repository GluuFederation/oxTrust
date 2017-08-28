package org.gluu.oxtrust.ws.rs.passport;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.model.passport.PassportConfigResponse;
import org.gluu.oxtrust.service.uma.PassportUmaProtectionService;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.passport.FieldSet;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.service.JsonService;
import org.xdi.util.Pair;

/**
 * PassportConfigurationEndPoint Implementation
 * 
 * @author Shekhar L.
 * @author Yuriy Movchan Date: 12/06/2016
 */

@Named("PassportConfigurationEndPoint")
@Path("/passport/config")
public class PassportRestWebService {

	@Inject
	private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private PassportUmaProtectionService passportUmaProtectionService;

	@Inject
	private UmaPermissionService umaPermissionService;

	@Inject
	private JsonService jsonService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getPassportConfig(@HeaderParam("Authorization") String authorization) {
		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		PassportConfigResponse passportConfigResponse = new PassportConfigResponse();
		
		Map <String,Map> strategies = new HashMap <String,Map>();

		LdapOxPassportConfiguration ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		if(ldapOxPassportConfiguration != null ){
			for (org.xdi.model.passport.PassportConfiguration passportConfiguration : ldapOxPassportConfiguration.getPassportConfigurations()) {
				if(passportConfiguration != null){
					Map<String, String> map = new HashMap();
					List<FieldSet>  passList = passportConfiguration.getFieldset();
					for( FieldSet fieldset :  passList ){
						map.put(fieldset.getKey(), fieldset.getValue());
					}		
					
					strategies.put(passportConfiguration.getStrategy(),map);
				}
			}
		}
		passportConfigResponse.setPassportStrategies(strategies);

		String passportConfigResponseJson;
		try {
			passportConfigResponseJson = jsonService.objectToPerttyJson(passportConfigResponse);
		} catch (IOException ex) {
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to prepare configuration");
		}

		return Response.status(Response.Status.OK).entity(passportConfigResponseJson).build();
	}

	protected Response processAuthorization(String authorization) {
		if (!passportUmaProtectionService.isEnabled()) {
			log.info("UMA passport authentication is disabled");
			return getErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "Passport configuration was disabled");
		}

		Token patToken;
		try {
			patToken = passportUmaProtectionService.getPatToken();
		} catch (UmaProtectionException ex) {
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to obtain PAT token");
		}

		Pair<Boolean, Response> rptTokenValidationResult = umaPermissionService.validateRptToken(patToken,
				authorization, passportUmaProtectionService.getUmaResourceId(),
				passportUmaProtectionService.getUmaScope());
		if (rptTokenValidationResult.getFirst()) {
			if (rptTokenValidationResult.getSecond() != null) {
				return rptTokenValidationResult.getSecond();
			}
		} else {
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Invalid GAT/RPT token");
		}

		return null;
	}

	protected Response getErrorResponse(Response.Status status, String detail) {
		return Response.status(status).entity(detail).build();
	}

}
