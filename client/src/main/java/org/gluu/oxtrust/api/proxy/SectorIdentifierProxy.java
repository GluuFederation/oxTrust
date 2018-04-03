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

import org.gluu.oxtrust.model.OxAuthSectorIdentifier;

public interface SectorIdentifierProxy {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<OxAuthSectorIdentifier> getAllSectorIdentifiers();

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	OxAuthSectorIdentifier getSectorIdentifier(@PathParam("id") String id);

	@GET
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<OxAuthSectorIdentifier> searchSectorIdentifiers(@QueryParam(value = "pattern") @NotNull String pattern);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	OxAuthSectorIdentifier createSectorIdentifier(OxAuthSectorIdentifier person);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	OxAuthSectorIdentifier updateSectorIdentifier(OxAuthSectorIdentifier person);

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void deleteSectorIdentifier(@PathParam("id") String id);

}
