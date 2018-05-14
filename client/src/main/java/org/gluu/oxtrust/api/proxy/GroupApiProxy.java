package org.gluu.oxtrust.api.proxy;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.GluuPersonApi;

public interface GroupApiProxy {
	

	public static final String PATH_INUM = "/{inum}";
	public static final String INUM = "inum";

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuGroupApi> getGroups(@DefaultValue("0") @QueryParam("size") int size);

	@GET
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuGroupApi getGroup(@PathParam(INUM) String inum);

	@GET
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuGroupApi> getSearchGroups(@QueryParam(value = "pattern") @NotNull String pattern,
			@QueryParam(value = "size") int size);

	@GET
	@Path("/{inum}/members")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	List<GluuPersonApi> getGroupMembers(@PathParam(INUM) String inum);

	@POST
	@Path("/{inum}/members/{minum}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Response addGroupMember(@PathParam(INUM) String inum, @PathParam("minum") String minum);

	@DELETE
	@Path("/{inum}/members/{minum}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	Response removeGroupMember(@PathParam(INUM) String inum, @PathParam("minum") String minum);

	@DELETE
	@Path(PATH_INUM)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void deleteGroup(@PathParam(INUM) String inum);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuGroupApi createGroup(GluuGroupApi group);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	GluuGroupApi updateGroup(GluuGroupApi group);
}
