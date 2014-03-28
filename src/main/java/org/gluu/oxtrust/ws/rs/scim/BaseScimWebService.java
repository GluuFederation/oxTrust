package org.gluu.oxtrust.ws.rs.scim;

import javax.ws.rs.core.Response;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.model.scim.Error;
import org.gluu.oxtrust.model.scim.Errors;
import org.gluu.oxtrust.service.UmaAuthenticationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.util.Pair;

/**
 * Base methods for SCIM web services
 * 
 * @author Yuriy Movchan Date: 08/23/2013
 */
public class BaseScimWebService {

	@Logger
	private Log log;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In
	private UmaAuthenticationService umaAuthenticationService;

	protected Response processAuthorization(String authorization) throws Exception {
		boolean authorized = getAuthorizedUser();
		if (!authorized) {
			if (!umaAuthenticationService.isEnabledUmaAuthentication()) {
				return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
			}
			
			Pair<Boolean, Response> rptTokenValidationResult = umaAuthenticationService.validateRptToken(authorization, applicationConfiguration.getUmaResourceId(), applicationConfiguration.getUmaScope());
			if (rptTokenValidationResult.getFirst()) {
				if (rptTokenValidationResult.getSecond() != null) {
					return rptTokenValidationResult.getSecond();
				}
			} else {
				return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
			}
		}
		
		return null;
	}

	protected boolean getAuthorizedUser() {
		try {
			GluuCustomPerson authUser = (GluuCustomPerson) Contexts.getSessionContext().get(OxTrustConstants.CURRENT_PERSON);
			SecurityService securityService = SecurityService.instance();

			OrganizationService organizationService = OrganizationService.instance();
			GluuOrganization org = organizationService.getOrganization();
			if (!GluuStatus.ACTIVE.equals(org.getScimStatus())) {
				return false;
			}

			if (authUser == null) {
				return false;
			}

			GluuUserRole[] userRoles = securityService.getUserRoles(authUser);
			for (GluuUserRole role : userRoles) {

				if (role.getRoleName().equalsIgnoreCase("MANAGER") || role.getRoleName().equalsIgnoreCase("OWNER")) {
					if (Utils.isScimGroupMemberOrOwner(authUser)) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}

	}

	protected Response getErrorResponse(String errMsg, int statusCode) {
		Errors errors = new Errors();
		Error error = new org.gluu.oxtrust.model.scim.Error(errMsg, statusCode, "");
		errors.getErrors().add(error);
		return Response.status(statusCode).entity(errors).build();
	}

}
