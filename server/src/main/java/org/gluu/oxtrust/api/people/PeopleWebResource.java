package org.gluu.oxtrust.api.people;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.PEOPLE)
@Consumes(MediaType.APPLICATION_JSON)
public class PeopleWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private PersonService personService;

	public PeopleWebResource() {
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@ApiOperation(value = "Get people")
	public String listPeople(@Context HttpServletResponse response) {
		try {
			List<GluuCustomPerson> groups = personService.findAllPersons(null);
			response.setStatus(HttpServletResponse.SC_OK);
			return mapper.writeValueAsString(groups);
		} catch (Exception e) {
			logger.error("Exception when getting people", e);
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
			} catch (Exception ex) {
			}
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a specific person")
	public String getPersonByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			GluuCustomPerson person = personService.getPersonByInum(inum);
			if (person != null) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return mapper.writeValueAsString(person);
		} catch (Exception e) {
			return handleError(logger, e, "Exception when retrieving person " + inum, response);
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Add a person")
	public String createPerson(GluuCustomPerson person, @Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(person, "Attempt to create null person");
			String inum = personService.generateInumForNewPerson();
			person.setInum(inum);
			personService.addPerson(person);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return inum;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during custome person insertion", response);
		}
	}

	@PUT
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Update custom person")
	public String updatePerson(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, GluuCustomPerson person,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(person, "Attempt to update null person");
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				person.setInum(existingPerson.getInum());
				personService.updatePerson(person);
				response.setStatus(HttpServletResponse.SC_OK);
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return OxTrustConstants.RESULT_FAILURE;
			}
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during person update", response);
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Delete a person")
	public String deletePerson(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				personService.removePerson(existingPerson);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when deleting person " + inum, response);
		}
	}

}
