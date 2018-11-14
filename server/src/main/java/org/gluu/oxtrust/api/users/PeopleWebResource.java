package org.gluu.oxtrust.api.users;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.PEOPLE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL +OxTrustApiConstants.PEOPLE, description = "Peoples webservice")
public class PeopleWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private IPersonService personService;

	public PeopleWebResource() {
	}

	@GET
	@ApiOperation(value = "Get people")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuPersonApi[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response listPeople() {
		try {
			List<GluuPersonApi> groups = convert(personService.findAllPersons(null));
			return Response.ok(groups).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search person")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuCustomPerson[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern) {
		try {
			List<GluuCustomPerson> groups = personService.searchPersons(pattern);
			return Response.ok(convert(groups)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a person by inum")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuPersonApi.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getPersonByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Preconditions.checkNotNull(inum, "inum should not be null");
			GluuCustomPerson person = personService.getPersonByInum(inum);
			if (person != null) {
				return Response.ok(convert(Collections.singletonList(person)).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add a person")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuPersonApi.class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response createPerson(GluuPersonApi person) {
		try {
			Preconditions.checkNotNull(person, "Attempt to create null person");
			GluuCustomPerson gluuPerson = copyAttributes(person);
			String inum = personService.generateInumForNewPerson();
			gluuPerson.setDn(personService.getDnForPerson(inum));
			gluuPerson.setInum(inum);
			personService.addPerson(gluuPerson);
			return Response.ok(convert(Collections.singletonList(personService.getPersonByInum(inum))).get(0)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update a person")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = GluuPersonApi.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response updateGroup(GluuPersonApi person) {
		String inum = person.getInum();
		try {
			Preconditions.checkNotNull(inum, "inum should not be null");
			Preconditions.checkNotNull(person, "Attempt to update null person");
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				person.setInum(existingPerson.getInum());
				person.setPassword(existingPerson.getUserPassword());
				GluuCustomPerson personToUpdate = updateValues(existingPerson, person);
				personToUpdate.setDn(personService.getDnForPerson(inum));
				personService.updatePerson(personToUpdate);
				return Response.ok(convert(Collections.singletonList(personService.getPersonByInum(inum))).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete a person")
	@ApiResponses(
			value = {
					@ApiResponse(code = 204, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response deletePerson(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				personService.removePerson(existingPerson);
				return Response.noContent().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private List<GluuPersonApi> convert(List<GluuCustomPerson> persons) {
		List<GluuPersonApi> result = new ArrayList<GluuPersonApi>();
		for (GluuCustomPerson p : persons) {
			result.add(new GluuPersonApi(p));
		}
		return result;
	}

	private GluuCustomPerson copyAttributes(GluuPersonApi person) {
		GluuCustomPerson gluuCustomPerson = new GluuCustomPerson();
		gluuCustomPerson.setUid(person.getUserName());
		gluuCustomPerson.setIname(person.getIname());
		gluuCustomPerson.setInum(person.getInum());
		gluuCustomPerson.setGivenName(person.getGivenName());
		gluuCustomPerson.setMail(person.getEmail());
		gluuCustomPerson.setDisplayName(person.getDisplayName());
		gluuCustomPerson.setSurname(person.getSurName());
		gluuCustomPerson.setCreationDate(person.getCreationDate());
		gluuCustomPerson.setStatus(person.getStatus());
		gluuCustomPerson.setUserPassword(person.getPassword());
		gluuCustomPerson.setAttribute("oxTrustActive", "true");
		return gluuCustomPerson;
	}

	private GluuCustomPerson updateValues(GluuCustomPerson gluuCustomPerson, GluuPersonApi person) {
		gluuCustomPerson.setIname(person.getIname());
		gluuCustomPerson.setUid(person.getUserName());
		gluuCustomPerson.setDisplayName(person.getDisplayName());
		gluuCustomPerson.setCreationDate(person.getCreationDate());
		gluuCustomPerson.setMail(person.getEmail());
		gluuCustomPerson.setGivenName(person.getGivenName());
		gluuCustomPerson.setStatus(person.getStatus());
		gluuCustomPerson.setSurname(person.getSurName());
		gluuCustomPerson.setUserPassword(person.getPassword());
		gluuCustomPerson.setAttribute("oxTrustActive", "true");
		return gluuCustomPerson;
	}

}
