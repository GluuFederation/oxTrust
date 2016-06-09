/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.*;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim.ScimPersonPatch;
import org.gluu.oxtrust.model.scim.ScimPersonSearch;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseUserSerializer;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.oxtrust.ws.rs.scim.PATCH;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * scim2UserEndpoint Implementation
 * 
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("scim2UserEndpoint")
@Path("/scim/v2/Users")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/v2/Users", description = "SCIM 2.0 User Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)", authorizations = { @Authorization(value = "Authorization", type = "uma") })
public class UserWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IPersonService personService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List users", notes = "Returns a list of users (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
	public Response listUsers(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
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
				schema.add(Constants.LIST_RESPONSE_SCHEMA_ID);

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

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Users");

				// Serialize to JSON
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
				mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
				SimpleModule customScimFilterModule = new SimpleModule("CustomScimUserFilterModule", new Version(1, 0, 0, ""));
				ListResponseUserSerializer serializer = new ListResponseUserSerializer();
				serializer.setAttributesArray(attributesArray);
				customScimFilterModule.addSerializer(User.class, serializer);
				mapper.registerModule(customScimFilterModule);
				String json = mapper.writeValueAsString(personsListResponse);

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

			ex.printStackTrace();
			String detail = "Unexpected processing error; please check the input parameters";
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, detail);
		}
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Find user by id", notes = "Returns a user by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.1)", response = User.class)
	public Response getUserByUid(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			String filterString = "id eq \"" + id + "\"";
			VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

			List<GluuCustomPerson> personList = search(personService.getDnForPerson(null), GluuCustomPerson.class, filterString, 1, 1, "id", SortOrder.ASCENDING.getValue(), vlvResponse, attributesArray);
			// GluuCustomPerson gluuPerson = personService.getPersonByInum(id);

			if (personList == null || personList.isEmpty() || vlvResponse.getTotalResults() == 0) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				log.info(" Resource " + id + " found ");
			}

			GluuCustomPerson gluuPerson = personList.get(0);

			User user = CopyUtils2.copy(gluuPerson, null);

			URI location = new URI(user.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimUserFilterModule", new Version(1, 0, 0, ""));
			ListResponseUserSerializer serializer = new ListResponseUserSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(User.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(user);

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Create user", notes = "Create user (https://tools.ietf.org/html/rfc7644#section-3.3)", response = User.class)
	public Response createUser(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@ApiParam(value = "User", required = true) User user,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			log.debug(" copying gluuperson ");
			GluuCustomPerson gluuPerson = CopyUtils2.copy(user, null, false);
			if (gluuPerson == null) {
				return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create user");
			}

			personService = PersonService.instance();

			log.debug(" generating inum ");
			String inum = personService.generateInumForNewPerson(); // inumService.generateInums(Configuration.INUM_TYPE_PEOPLE_SLUG);
			// //personService.generateInumForNewPerson();
			log.debug(" getting DN ");
			String dn = personService.getDnForPerson(inum);

			log.debug(" getting iname ");
			String iname = personService.generateInameForNewPerson(user.getUserName());

			log.debug(" setting dn ");
			gluuPerson.setDn(dn);

			log.debug(" setting inum ");
			gluuPerson.setInum(inum);

			log.debug(" setting iname ");
			gluuPerson.setIname(iname);

			log.debug(" setting commonName ");
			gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

			log.info("gluuPerson.getMemberOf().size() : " + gluuPerson.getMemberOf().size());
			if (user.getGroups().size() > 0) {
				log.info(" jumping to groupMembersAdder ");
				log.info("gluuPerson.getDn() : " + gluuPerson.getDn());
				Utils.groupMembersAdder(gluuPerson, gluuPerson.getDn());
			}

			// As per spec, the SP must be the one to assign the meta attributes
			log.info(" Setting meta: create user ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateCreated = DateTime.now().toDate();
			String relativeLocation = "/scim/v2/Users/" + inum;
			gluuPerson.setAttribute("oxTrustMetaCreated", dateTimeFormatter.print(dateCreated.getTime()));
			gluuPerson.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateCreated.getTime()));
			gluuPerson.setAttribute("oxTrustMetaLocation", relativeLocation);

			// Sync email, forward ("oxTrustEmail" -> "mail")
			gluuPerson = Utils.syncEmailForward(gluuPerson, true);

			log.debug("adding new GluuPerson");
			personService.addPerson(gluuPerson);

			User createdUser = CopyUtils2.copy(gluuPerson, null);
			// newPerson.setCustomAttributes(person.getCustomAttributes());

			URI location = new URI(createdUser.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimUserFilterModule", new Version(1, 0, 0, ""));
			ListResponseUserSerializer serializer = new ListResponseUserSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(User.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(createdUser);

			// Return HTTP response with status code 201 Created
			return Response.created(location).entity(json).build();

		} catch (DuplicateEntryException ex) {

			log.error("Failed to create user", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (PersonRequiredFieldsException ex) {

			log.error("PersonRequiredFieldsException: ", ex);
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to create user", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update user", notes = "Update user (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = User.class)
	public Response updateUser(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@ApiParam(value = "User", required = true) User user,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
			if (gluuPerson == null) {

				return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

			} else {

				// Validate if attempting to update userName of a different id
				if (user.getUserName() != null) {

					GluuCustomPerson personToFind = new GluuCustomPerson();
					personToFind.setUid(user.getUserName());

					List<GluuCustomPerson> foundPersons = personService.findPersons(personToFind, 2);
					if (foundPersons != null && foundPersons.size() > 0) {
						for (GluuCustomPerson foundPerson : foundPersons) {
							if (foundPerson != null && !foundPerson.getInum().equalsIgnoreCase(gluuPerson.getInum())) {
								throw new DuplicateEntryException("Cannot update userName of a different id: " + user.getUserName());
							}
						}
					}
				}
			}

			GluuCustomPerson updatedGluuPerson = CopyUtils2.copy(user, gluuPerson, true);

			if (user.getGroups().size() > 0) {
				Utils.groupMembersAdder(updatedGluuPerson, personService.getDnForPerson(id));
			}

			log.info(" Setting meta: update user ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateLastModified = DateTime.now().toDate();
			updatedGluuPerson.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
			if (updatedGluuPerson.getAttribute("oxTrustMetaLocation") == null || (("oxTrustMetaLocation") != null && updatedGluuPerson.getAttribute("oxTrustMetaLocation").isEmpty())) {
				String relativeLocation = "/scim/v2/Users/" + id;
				updatedGluuPerson.setAttribute("oxTrustMetaLocation", relativeLocation);
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			updatedGluuPerson = Utils.syncEmailForward(updatedGluuPerson, true);

			personService.updatePerson(updatedGluuPerson);

			log.debug(" person updated ");

			User updatedUser = CopyUtils2.copy(updatedGluuPerson, null);
			// person_update = CopyUtils.copy(gluuPerson, null, attributes);

			URI location = new URI(updatedUser.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimUserFilterModule", new Version(1, 0, 0, ""));
			ListResponseUserSerializer serializer = new ListResponseUserSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(User.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(updatedUser);

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (DuplicateEntryException ex) {

			log.error("Failed to update user", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to update user", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON})
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Delete User", notes = "Delete User (https://tools.ietf.org/html/rfc7644#section-3.6)")
	public Response deleteUser(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson person = personService.getPersonByInum(id);
			if (person == null) {
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				log.info("person.getMemberOf().size() : " + person.getMemberOf().size());
				if (person.getMemberOf() != null) {
					if (person.getMemberOf().size() > 0) {
						String dn = personService.getDnForPerson(id);
						log.info("DN : " + dn);

						Utils.deleteUserFromGroup(person, dn);
					}
				}
				personService.removePerson(person);
			}

			return Response.ok().build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}

	@Path("{id}")
	@PATCH
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUserPatch(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id, ScimPersonPatch person) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		return null;
	}

	@Path("/Search")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response personSearch(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		ScimPersonSearch searchPattern) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
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

			URI location = new URI(user.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimUserFilterModule", new Version(1, 0, 0, ""));
			ListResponseUserSerializer serializer = new ListResponseUserSerializer();
			// serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(User.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(user);

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource not found");

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}
	
	@Path("/SearchPersons")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response searchPersons(
			@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
			ScimPersonSearch searchPattern) throws Exception {

		Response authorizationResponse = null;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
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

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}
}
