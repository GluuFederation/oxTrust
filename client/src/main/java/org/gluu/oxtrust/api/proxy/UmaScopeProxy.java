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

import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;

public interface UmaScopeProxy {

	public static final String PATH_INUM = "/{inum}";
	public static final String INUM = "inum";

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<UmaScopeDescription> getAllUmaScopes();

	@GET
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaScopeDescription getUmaScope(@PathParam(INUM) String inum);

	@GET
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<UmaScopeDescription> searchUmaScopes(@QueryParam(value = "pattern") @NotNull String pattern,
			@QueryParam(value = "size") int size);

	@DELETE
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void deleteUmaScope(@PathParam(INUM) String inum);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaScopeDescription createUmaScope(UmaScopeDescription umaScopeDescription);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	UmaScopeDescription updateUmaScopeDescription(UmaScopeDescription umaScopeDescription);

}
