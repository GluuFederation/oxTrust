package org.gluu.oxtrust.ws.rs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * InumGenerationWebService
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
@Path("/InumGenerator")
public interface InumGenerationWebService {

	@POST
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getInum(@Context HttpServletRequest request, @QueryParam("entityPrefix") final String prefix) throws Exception;

}
