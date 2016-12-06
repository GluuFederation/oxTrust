package org.gluu.oxtrust.api.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OxPassportService;
import org.gluu.oxtrust.model.passport.PassportConfigResponse;
import org.gluu.oxtrust.model.passport.PassportStrategy;
import org.gluu.oxtrust.service.uma.PassportUmaProtectionService;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;

/**
 * PassportConfigurationEndPoint Implementation
 * 
 * @author Shekhar L.
 * @author Yuriy Movchan Date: 12/06/2016
 */

@Name("PassportConfigurationEndPoint")
@Path("/passport/config")
public class PassportRestWebService {

	@Logger
	private Log log;

	@In
	private OxPassportService passportService;
	
	@In
	private PassportUmaProtectionService pasportUmaProtectionService;
	
	@In
	private UmaPermissionService umaPermissionService;

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public Response getPassportConfig(@HeaderParam("Authorization") String authorization) {
		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		PassportConfigResponse passportConfigResponse = null;
		passportConfigResponse = new PassportConfigResponse();
		LdapOxPassportConfiguration ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		List<org.xdi.config.oxtrust.PassportConfiguration>  passportConfigurations  =ldapOxPassportConfiguration.getPassportConfigurations();
		Map  <String ,PassportStrategy> PassportConfigurationsMap = new HashMap<String, PassportStrategy>();
		for(org.xdi.config.oxtrust.PassportConfiguration passportConfiguration : passportConfigurations){			
			if(passportConfiguration.getProvider().equalsIgnoreCase("passport")){
				passportConfigResponse.setApplicationEndpoint((passportConfiguration.getApplicationEndpoint()==null) ? "" : passportConfiguration.getApplicationEndpoint() );	
				passportConfigResponse.setAuthenticationUrl((passportConfiguration.getServerURI()==null) ? "" : passportConfiguration.getServerURI());
				passportConfigResponse.setApplicationStartpoint((passportConfiguration.getApplicationStartpoint()==null) ? "" : passportConfiguration.getApplicationStartpoint());
				
			}else{
				PassportStrategy passportStrategy = new PassportStrategy();							
				passportStrategy.setClientID((passportConfiguration.getClientID()==null) ? "" : passportConfiguration.getClientID());
				passportStrategy.setClientSecret((passportConfiguration.getClientSecret()==null) ? "" : passportConfiguration.getClientSecret());
				PassportConfigurationsMap.put((passportConfiguration.getProvider()==null) ? "" : passportConfiguration.getProvider(), passportStrategy);
			}					
		}	
		passportConfigResponse.setPassportStrategies(PassportConfigurationsMap);
		return Response.status(Response.Status.OK).entity(passportConfigResponse).build();
	}

	protected Response processAuthorization(String authorization) {
		if (!pasportUmaProtectionService.isEnabled()) {
			log.info("UMA authentication is disabled");
			return getErrorResponse(Response.Status.FORBIDDEN, "Passport configuration was disabled");
		}

		Token patToken;
		try {
			patToken = pasportUmaProtectionService.getPatToken();
		} catch (UmaProtectionException ex) {
			return getErrorResponse(Response.Status.FORBIDDEN, "Failed to obtain PAT token");
		}

		Pair<Boolean, Response> rptTokenValidationResult = umaPermissionService.validateRptToken(patToken, authorization, pasportUmaProtectionService.getUmaResourceId(), pasportUmaProtectionService.getUmaScope());
		if (rptTokenValidationResult.getFirst()) {
			if (rptTokenValidationResult.getSecond() != null) {
				return rptTokenValidationResult.getSecond();
			}
		} else {
			return getErrorResponse(Response.Status.FORBIDDEN, "Invalid GAT/RPT token");
		}

		return null;
	}

	protected Response getErrorResponse(Response.Status status, String detail) {
		return Response.status(status).entity(detail).build();
	}

}
