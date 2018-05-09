package org.gluu.oxtrust.api.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
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
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.GluuCustomAttributeApi;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.PEOPLE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeopleWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private IPersonService personService;

	public PeopleWebResource() {
	}

	@GET
	@ApiOperation(value = "Get people")
	public Response listPeople() {
		log("Get people");
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
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern) {
		log("Search person with pattern= " + pattern);
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
	public Response getPersonByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Get person " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuCustomPerson person = personService.getPersonByInum(inum);
			if (person != null) {
				return Response.ok(convert(Arrays.asList(person)).get(0)).build();
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
	public Response createPerson(GluuPersonApi person) {
		log("Adding person " + person.getDisplayName());
		try {
			Objects.requireNonNull(person, "Attempt to create null person");
			GluuCustomPerson gluuPerson = copyAttributes(person);
			String inum = personService.generateInumForNewPerson();
			gluuPerson.setDn(personService.getDnForPerson(inum));
			gluuPerson.setInum(inum);
			personService.addPerson(gluuPerson);
			return Response.ok(convert(Arrays.asList(personService.getPersonByInum(inum))).get(0)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@ApiOperation(value = "List person custom attributes")
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.ATTRIBUTES)
	public Response getPersonCustomAttributes(@PathParam(OxTrustApiConstants.INUM) @NotNull String userInum) {
		log("List person " + userInum + " custom attributes ");
		try {
			Objects.requireNonNull(userInum, "User inum should not be null");
			GluuCustomPerson person = personService.getPersonByInum(userInum);
			if (person != null) {
				return Response.ok(convertTo(person.getCustomAttributes())).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add custom attribute to user")
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.ATTRIBUTES)
	public Response addCustomAttribute(@PathParam(OxTrustApiConstants.INUM) @NotNull String userInum,
			GluuCustomAttribute customAttribute) {
		Objects.requireNonNull(customAttribute, "Custom attribute should not be null");
		log("Add custom attribute to " + customAttribute.toString());
		try {
			Objects.requireNonNull(userInum, "User inum should not be null");
			GluuCustomPerson person = personService.getPersonByInum(userInum);
			if (person != null) {
				List<GluuCustomAttribute> attributes = new ArrayList<GluuCustomAttribute>(person.getCustomAttributes());
				attributes.add(customAttribute);
				person.setCustomAttributes(attributes);
				personService.updatePerson(person);
				return Response.ok(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@ApiOperation(value = "Remove custom attribute to user")
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.ATTRIBUTES)
	public Response removeCustomAttribute(@PathParam(OxTrustApiConstants.INUM) @NotNull String userInum,
			GluuCustomAttribute customAttribute) {
		Objects.requireNonNull(customAttribute, "Custom attribute should not be null");
		log("Remove custom attribute to " + customAttribute.toString());
		try {
			Objects.requireNonNull(userInum, "User inum should not be null");
			GluuCustomPerson person = personService.getPersonByInum(userInum);
			customAttribute = person.getGluuCustomAttribute(customAttribute.getName());
			List<GluuCustomAttribute> customAttributes = person.getCustomAttributes();
			if (person != null && customAttribute != null) {
				for (GluuCustomAttribute gluuCustomAttribute : customAttributes) {
					if (customAttribute.getName().equalsIgnoreCase(gluuCustomAttribute.getName())) {
						customAttributes.remove(gluuCustomAttribute);
						break;
					}
				}
				person.setCustomAttributes(customAttributes);
				personService.updatePerson(person);
				return Response.ok(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update a person")
	public Response updateGroup(GluuPersonApi person) {
		String inum = person.getInum();
		log("Update group " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Objects.requireNonNull(person, "Attempt to update null person");
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				person.setInum(existingPerson.getInum());
				person.setPassword(existingPerson.getUserPassword());
				GluuCustomPerson personToUpdate = updateValues(existingPerson, person);
				personToUpdate.setDn(personService.getDnForPerson(inum));
				personService.updatePerson(personToUpdate);
				return Response.ok(convert(Arrays.asList(personService.getPersonByInum(inum))).get(0)).build();
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
	public Response deletePerson(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Delete person having inum " + inum);
		try {
			GluuCustomPerson existingPerson = personService.getPersonByInum(inum);
			if (existingPerson != null) {
				personService.removePerson(existingPerson);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	public Response deletePeople() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	private List<GluuPersonApi> convert(List<GluuCustomPerson> persons) {
		List<GluuPersonApi> result = new ArrayList<GluuPersonApi>();
		for (GluuCustomPerson p : persons) {
			result.add(new GluuPersonApi(p));
		}
		return result;
	}

	private void log(String message) {
		logger.debug("#################Request: " + message);
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

	private List<GluuCustomAttributeApi> convertTo(List<GluuCustomAttribute> gluuCustomAttributes) {
		List<GluuCustomAttributeApi> results = new ArrayList<GluuCustomAttributeApi>();
		for (GluuCustomAttribute gluuCustomAttribute : gluuCustomAttributes) {
			results.add(new GluuCustomAttributeApi(gluuCustomAttribute.getName(), gluuCustomAttribute.getValue()));
		}
		return results;
	}

}
