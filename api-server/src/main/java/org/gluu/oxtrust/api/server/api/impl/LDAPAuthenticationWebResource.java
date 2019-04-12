package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.api.server.util.ApiConstants;

import com.wordnik.swagger.annotations.Api;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.LDAP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
		+ ApiConstants.LDAP, description = "LDAP web service")
@ApplicationScoped
public class LDAPAuthenticationWebResource extends BaseWebResource {
	


}
