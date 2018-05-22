package org.gluu.oxtrust.api.proxy;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.xdi.oxauth.model.uma.persistence.UmaResource;

public interface UmaResourceProxy {

	public static final String PATH_ID = "/{id}";
	public static final String ID = "id";

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<UmaResource> getAllUmaResources();

	@GET
	@Path(PATH_ID)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaResource getUmaResource(@PathParam(ID) String id);

	@GET
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<UmaResource> searchUmaResources(@QueryParam(value = "pattern") @NotNull String pattern,
			@QueryParam(value = "size") int size);

	@DELETE
	@Path(PATH_ID)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void deleteUmaResource(@PathParam(ID) String id);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaResource createUmaResource(UmaResource umaResource);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaResource updateUmaResource(UmaResource umaResource);

}
