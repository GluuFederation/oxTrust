package org.gluu.oxtrust.api.rest;

// Annotations 

// Service designator
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.model.Person;

//
// JAX-RS service with relative root URI /users
//
@Path("/users")
public interface UserRestService {

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response createUser(@Context HttpServletRequest request, Person person);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getUser(@Context HttpServletRequest request);

	@Path("{inum}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getUserByInum(@Context HttpServletRequest request, @PathParam("inum") String inum);

	@Path("uid/{id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getUserByUserId(@Context HttpServletRequest request, @PathParam("id") String uid);

	@Path("{inum}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateUser(@Context HttpServletRequest request, @PathParam("inum") String inum, Person person);

	@Path("{inum}/password")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response changePassword(@Context HttpServletRequest request, @PathParam("inum") String inum, String password);

	@Path("{inum}")
	@DELETE
	public Response deleteUser(@Context HttpServletRequest request, @PathParam("inum") String inum);

	// Additional Methods

	@Path("{inum}/passwordpost")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response changePasswordTestHelper(@Context HttpServletRequest request, @PathParam("inum") String inum,
			@FormParam("password") String password);

	@Path("{inum}/updatepost")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateUserTestHelper(@Context HttpServletRequest request, @PathParam("inum") String inum,
			@FormParam("person_data") String person_data);

	@Path("{inum}/createpost")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createUserTestHelper(@Context HttpServletRequest request, @FormParam("person_data") String person);
}
