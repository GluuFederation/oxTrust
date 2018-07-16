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

import org.gluu.oxtrust.api.GluuAttributeApi;

@Path("/api/attributes")
public interface AttributeProxy {
	
	public static final String PATH_INUM = "/{inum}";
	public static final String INUM = "inum";

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuAttributeApi> getAllAttributes();
	
	@GET
	@Path("/active")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuAttributeApi> getAllActiveAttributes();
	
	@GET
	@Path("/inactive")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuAttributeApi> getAllInActiveAttributes();

	@GET
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuAttributeApi getAttribute(@PathParam(INUM) String inum);

	@GET
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuAttributeApi> searchAttributes(@QueryParam(value = "pattern") @NotNull String pattern,
			@QueryParam(value = "size") int size);

	
	@DELETE
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void deleteAttribute(@PathParam(INUM) String inum);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuAttributeApi createAttribute(GluuAttributeApi attribute);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuAttributeApi updateAttribute(GluuAttributeApi attribute);

}
