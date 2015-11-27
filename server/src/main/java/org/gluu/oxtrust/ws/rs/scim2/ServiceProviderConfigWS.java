/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.model.scim2.provider.ServiceProviderConfig;
import org.gluu.oxtrust.ws.rs.scim.BaseScimWebService;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("serviceProviderConfig")
@Path("/scim/v2/ServiceProviderConfig")
public class ServiceProviderConfigWS extends BaseScimWebService {

	@Logger
	private Log log;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response listGroups(@HeaderParam("Authorization") String authorization) throws Exception {
		ServiceProviderConfig providerConfig = new ServiceProviderConfig();
		URI location = new URI("/v2/ServiceProviderConfig");
		return Response.ok(providerConfig).location(location).build();

	}

}
