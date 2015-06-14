/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import org.gluu.oxtrust.exception.UmaProtectionException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;
import org.xdi.util.StringHelper;

import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * Authentication methods for UMA protection services
 * 
 * @author Yuriy Movchan Date: 08/20/2013
 */
@Scope(ScopeType.APPLICATION)
@Name("umaAuthenticationService")
@AutoCreate
public class UmaAuthenticationService implements Serializable {

	private static final long serialVersionUID = -2222131971095468865L;

	@Logger
	private Log log;

	@In
	private UmaProtectionService umaProtectionService;
	
	private final Pair<Boolean, Response> authenticationFailure = new Pair<Boolean, Response>(false, null);
	private final Pair<Boolean, Response> authenticationSuccess = new Pair<Boolean, Response>(true, null);
	
	public Pair<Boolean, Response> validateRptToken(String authorization, String resourceSetId, String scopeId) {
		if (!isEnabledUmaAuthentication() || (authorization == null) || !authorization.startsWith("Bearer ")) {
			return authenticationFailure;
		}

		String rptToken = authorization.substring(7);
		
		Token patToken;
		try {
			patToken = umaProtectionService.getPatToken();
		} catch (UmaProtectionException ex) {
			log.error("Failed to verify RPT token: '{0}'", ex, rptToken);
			return authenticationFailure;
		}

        RptIntrospectionResponse rptStatusResponse = umaProtectionService.getStatusResponse(patToken, rptToken);
		if (rptStatusResponse == null) {
			log.error("Status response for RPT token: '{0}' is invalid", rptToken);
			return authenticationFailure;
		}
		
		boolean rptHasPermissions = umaProtectionService.isRptHasPermissions(rptStatusResponse);
		if (rptHasPermissions) {
			return authenticationSuccess;
		}

		// If the RPT is valid but has insufficient authorization data for the type of access sought,
        // the resource server SHOULD register a requested permission with the authorization server
        // that would suffice for that scope of access (see Section 3.2),
        // and then respond with the HTTP 403 (Forbidden) status code,
        // along with providing the authorization server's URI in an "as_uri" property in the header,
        // and the permission ticket it just received from the AM in the body in a JSON-encoded "ticket" property.
		
		//TODO: START: Check if next blok do the same
        final String ticket = umaProtectionService.registerUmaPermissions(patToken, resourceSetId, scopeId);
        if (StringHelper.isEmpty(ticket)) {
        	return authenticationFailure;
        }
		//TODO: END: Check if next blok do the same
        
        Response registerUmaPermissionsResponse = umaProtectionService.prepareRegisterUmaPermissionsResponse(patToken, resourceSetId, scopeId);
        if (registerUmaPermissionsResponse == null) {
        	return authenticationFailure;
        }

		return new Pair<Boolean, Response>(true, registerUmaPermissionsResponse);
	}

	public boolean isEnabledUmaAuthentication() {
		return umaProtectionService.isEnabledUmaAuthentication();
	}

}
