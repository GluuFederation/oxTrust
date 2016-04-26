/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.*;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim.ScimPersonPatch;
import org.gluu.oxtrust.model.scim.ScimPersonSearch;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseUserSerializer;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.oxtrust.ws.rs.scim.PATCH;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * scim2UserEndpoint Implementation
 * 
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("scim2UserEndpoint")
@Path("/scim/v2/Users")
@Api(value = "/v2/Users", description = "SCIM 2.0 User Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)", authorizations = { @Authorization(value = "Authorization", type = "oauth2") })
@Produces({ "application/json", "application/xml" })
public class UserWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IPersonService personService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List Users", notes = "Returns a list of users (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
	public Response listUsers(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			if (count > MAX_COUNT) {

				String detail = "Too many results (=" + count + ") would be returned; max is " + MAX_COUNT + " only.";
				return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, detail);

			} else {

				log.info(" Searching users from LDAP ");

				personService = PersonService.instance();

				VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

				List<GluuCustomPerson> personList = search(personService.getDnForPerson(null), GluuCustomPerson.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);
				// List<GluuCustomPerson> personList = personService.findAllPersons(null);

				ListResponse personsListResponse = new ListResponse();

				List<String> schema = new ArrayList<String>();
				schema.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");

				log.info(" setting schema");
				personsListResponse.setSchemas(schema);

				// Set total
				personsListResponse.setTotalResults(vlvResponse.getTotalResults());

				if (count > 0 && personList != null && !personList.isEmpty()) {

					// log.info(" LDAP person list is not empty ");

					for (GluuCustomPerson gluuPerson : personList) {

						// log.info(" copying person from GluuPerson to ScimPerson ");
						User person = CopyUtils2.copy(gluuPerson, null);

						// log.info(" adding ScimPerson to the AllPersonList ");
						log.info(" person to be added userid : " + person.getUserName());

						personsListResponse.getResources().add(person);

						log.info(" person added? : " + personsListResponse.getResources().contains(person));
					}

					// Set the rest of results info
					personsListResponse.setItemsPerPage(vlvResponse.getItemsPerPage());
					personsListResponse.setStartIndex(vlvResponse.getStartIndex());
				}

				URI location = new URI("/v2/Users/");

				// Serialize to JSON
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
				SimpleModule customScimFilterModule = new SimpleModule("CustomScimFilterModule", new Version(1, 0, 0, ""));
				ListResponseUserSerializer serializer = new ListResponseUserSerializer();
				serializer.setAttributesArray(attributesArray);
				customScimFilterModule.addSerializer(User.class, serializer);
				mapper.registerModule(customScimFilterModule);
				String json = mapper.writeValueAsString(personsListResponse);

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

			log.error("Exception: ", ex);

			String detail = "Unexpected processing error; please check the input parameters.";
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ErrorScimType.INVALID_SYNTAX, detail);
		}
	}

	@Path("{uid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find User by id", notes = "Returns a Users on the basis of provided uid as path param (https://tools.ietf.org/html/rfc7644#section-3.4.1)", response = User.class)
	public Response getUserByUid(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);

			if (gluuPerson == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			User person = CopyUtils2.copy(gluuPerson, null);

			URI location = new URI("/v2/Users/" + uid);

			return Response.ok(person).location(location).build();

		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			System.out.println("UserWebService Ex: " + ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create User", notes = "Create User (https://tools.ietf.org/html/rfc7644#section-3.3)", response = User.class)
	public Response createUser(@HeaderParam("Authorization") String authorization, @ApiParam(value = "User", required = true) User person) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuperson ");
		GluuCustomPerson gluuPerson = CopyUtils2.copy(person, null, false);
		if (gluuPerson == null) {
			return getErrorResponse("Failed to create user", Response.Status.BAD_REQUEST.getStatusCode());
		}

		try {

			personService = PersonService.instance();

			log.debug(" generating inum ");
			String inum = personService.generateInumForNewPerson(); // inumService.generateInums(Configuration.INUM_TYPE_PEOPLE_SLUG);
																	// //personService.generateInumForNewPerson();
			log.debug(" getting DN ");
			String dn = personService.getDnForPerson(inum);
			log.debug(" getting iname ");
			String iname = personService.generateInameForNewPerson(person.getUserName());
			log.debug(" setting dn ");
			gluuPerson.setDn(dn);
			log.debug(" setting inum ");
			gluuPerson.setInum(inum);
			log.debug(" setting iname ");
			gluuPerson.setIname(iname);
			log.debug(" setting commonName ");
			gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());
			log.info("gluuPerson.getMemberOf().size() : " + gluuPerson.getMemberOf().size());
			if (person.getGroups().size() > 0) {
				log.info(" jumping to groupMemebersAdder ");
				log.info("gluuPerson.getDn() : " + gluuPerson.getDn());

				Utils.groupMemebersAdder(gluuPerson, gluuPerson.getDn());
			}

			log.debug("adding new GluuPerson");

			personService.addPerson(gluuPerson);

			User newPerson = CopyUtils2.copy(gluuPerson, null);
			// newPerson.setCustomAttributes(person.getCustomAttributes());

			String uri = "/v2/Users/" + newPerson.getId();
			
			return Response.created(URI.create(uri)).entity(newPerson).build();

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to add user", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update User", notes = "Update User (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = User.class)
	public Response updateUser(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid, @ApiParam(value = "User", required = true) User person) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			GluuCustomPerson newGluuPesron = CopyUtils2.copy(person, gluuPerson, true);

			if (person.getGroups().size() > 0) {
				Utils.groupMemebersAdder(newGluuPesron, personService.getDnForPerson(uid));
			}

			personService.updatePerson(newGluuPesron);

			log.debug(" person updated ");

			User newPerson = CopyUtils2.copy(newGluuPesron, null);
			// person_update = CopyUtils.copy(gluuPerson, null, attributes);

			URI location = new URI("/Users/" + uid);

			return Response.ok(newPerson).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Delete User", notes = "Delete User (https://tools.ietf.org/html/rfc7644#section-3.6)")
	public Response deleteUser(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson person = personService.getPersonByInum(uid);
			if (person == null) {
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				log.info("person.getMemberOf().size() : " + person.getMemberOf().size());
				if (person.getMemberOf() != null) {
					if (person.getMemberOf().size() > 0) {
						String dn = personService.getDnForPerson(uid);
						log.info("DN : " + dn);

						Utils.deleteUserFromGroup(person, dn);
					}
				}
				personService.removePerson(person);
			}
			return Response.ok().build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@PATCH
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUserPatch(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid, ScimPersonPatch person) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		return null;
	}

	@Path("/Search")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response personSearch(@HeaderParam("Authorization") String authorization, ScimPersonSearch searchPattern) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByAttribute(searchPattern.getAttribute(), searchPattern.getValue());
			if (gluuPerson == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("No result found for search pattern '" + searchPattern.getAttribute() + " = " + searchPattern.getValue()
						+ "' please try again or use another pattern.", Response.Status.NOT_FOUND.getStatusCode());
			}
			// ScimPerson person = CopyUtils.copy(gluuPerson, null);
			User user = CopyUtils2.copy(gluuPerson, null);
			URI location = new URI("/Users/" + gluuPerson.getInum());
			return Response.ok(user).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	
	
	@Path("/SearchPersons")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response searchPersons(@HeaderParam("Authorization") String authorization, ScimPersonSearch searchPattern) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			List<GluuCustomPerson> personList = personService.getPersonsByAttribute(searchPattern.getAttribute(), searchPattern.getValue());
			ListResponse personsListResponse = new ListResponse();
			if (personList != null) {
				log.info(" LDAP person list is not empty ");
				for (GluuCustomPerson gluuPerson : personList) {
					log.info(" copying person from GluuPerson to ScimPerson ");
					User person = CopyUtils2.copy(gluuPerson, null);

					log.info(" adding ScimPerson to the AllPersonList ");
					log.info(" person to be added userid : " + person.getUserName());
					personsListResponse.getResources().add(person);
					log.info(" person added? : " + personsListResponse.getResources().contains(person));
				}

			}
			List<String> schema = new ArrayList<String>();
			schema.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
			log.info("setting schema");
			personsListResponse.setSchemas(schema);
			personsListResponse.setTotalResults(personsListResponse.getResources().size());
			URI location = new URI("/Users/");
			return Response.ok(personsListResponse).location(location).build();
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
}
