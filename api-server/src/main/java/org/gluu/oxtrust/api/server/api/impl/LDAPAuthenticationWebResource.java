package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.util.OxTrustApiConstants;

import com.wordnik.swagger.annotations.Api;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.LDAP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.LDAP, description = "LDAP web service")
@ApplicationScoped
public class LDAPAuthenticationWebResource extends BaseWebResource {
	


}
