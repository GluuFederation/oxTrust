/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuCustomPersonList;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonPatch;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.service.antlr.scimFilter.util.GluuCustomPersonListSerializer;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;
import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

/**
 * SCIM UserWebService Implementation
 * 
 * @author Reda Zerrad Date: 04.03.2012
 */
@Name("userWebService")
@Path("/scim/v1/Users")
public class UserWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IPersonService personService;

	@In
	private ExternalScimService externalScimService;

	@GET
	@Produces({MediaType.APPLICATION_JSON,  MediaType.APPLICATION_XML})
	@HeaderParam("Accept") @DefaultValue(MediaType.APPLICATION_JSON)
	public Response searchPersons(@HeaderParam("Authorization") String authorization,
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
				return getErrorResponse(detail, Response.Status.BAD_REQUEST.getStatusCode());

			} else {

				log.info(" Searching persons from LDAP ");

				personService = PersonService.instance();

				VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

				List<GluuCustomPerson> gluuCustomPersons = search(personService.getDnForPerson(null), GluuCustomPerson.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);
				// List<GluuCustomPerson> personList = personService.findAllPersons(null);

				GluuCustomPersonList personsList = new GluuCustomPersonList();

				List<String> schema = new ArrayList<String>();
				schema.add(Constants.SCIM1_CORE_SCHEMA_ID);

				log.info(" setting schema");
				personsList.setSchemas(schema);

				// Set total
				personsList.setTotalResults(vlvResponse.getTotalResults());

				if (count > 0 && gluuCustomPersons != null && !gluuCustomPersons.isEmpty()) {

					// log.info(" LDAP person list is not empty ");

					for (GluuCustomPerson gluuPerson : gluuCustomPersons) {

						ScimPerson person = CopyUtils.copy(gluuPerson, null);

						log.info(" person to be added id : " + person.getUserName());

						personsList.getResources().add(person);

						log.info(" person added? : " + personsList.getResources().contains(person));
					}

					// Set the rest of results info
					personsList.setItemsPerPage(vlvResponse.getItemsPerPage());
					personsList.setStartIndex(vlvResponse.getStartIndex());
				}

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v1/Users");

				// Serialize to JSON
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
				SimpleModule customScimFilterModule = new SimpleModule("CustomScim1PersonFilterModule", new Version(1, 0, 0, ""));
				GluuCustomPersonListSerializer serializer = new GluuCustomPersonListSerializer();
				serializer.setAttributesArray(attributesArray);
				customScimFilterModule.addSerializer(ScimPerson.class, serializer);
				mapper.registerModule(customScimFilterModule);
				String json = mapper.writeValueAsString(personsList);

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

			log.error("Error in searchPersons", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
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

			ScimPerson person = CopyUtils.copy(gluuPerson, null);

			URI location = new URI("/Users/" + uid);

			return Response.ok(person).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createUser(@HeaderParam("Authorization") String authorization, ScimPerson person) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		// Return HTTP response with status code 201 Created

		try {

			personService = PersonService.instance();

			log.debug(" copying gluuperson ");
			GluuCustomPerson gluuPerson = CopyUtils.copy(person, null, false);
			if (gluuPerson == null) {
				return getErrorResponse("Failed to create user", Response.Status.BAD_REQUEST.getStatusCode());
			}

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
				log.info(" jumping to groupMembersAdder ");
				log.info("gluuPerson.getDn() : " + gluuPerson.getDn());

				Utils.groupMembersAdder(gluuPerson, gluuPerson.getDn());
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			gluuPerson = Utils.syncEmailForward(gluuPerson, false);

			// For custom script: create user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateUserMethods(gluuPerson);
			}

			log.debug("adding new GluuPerson");
			personService.addPerson(gluuPerson);

			ScimPerson newPerson = CopyUtils.copy(gluuPerson, null);

			String uri = "/Users/" + newPerson.getId();

			return Response.created(URI.create(uri)).entity(newPerson).build();

		} catch (DuplicateEntryException ex) {

			log.error("Failed to create user", ex);
			ex.printStackTrace();
			return getErrorResponse(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());

		} catch (PersonRequiredFieldsException ex) {

			log.error("PersonRequiredFieldsException: ", ex);
			return getErrorResponse(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());

		} catch (Exception ex) {

			log.error("Failed to create user", ex);
			ex.printStackTrace();
			return getErrorResponse(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUser(@HeaderParam("Authorization") String authorization, @PathParam("id") String id, ScimPerson person) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
			if (gluuPerson == null) {

				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

			} else {

				// Validate if attempting to update userName of a different id
				if (person.getUserName() != null) {

					GluuCustomPerson personToFind = new GluuCustomPerson();
					personToFind.setUid(person.getUserName());

					List<GluuCustomPerson> foundPersons = personService.findPersons(personToFind, 2);
					if (foundPersons != null && foundPersons.size() > 0) {
						for (GluuCustomPerson foundPerson : foundPersons) {
							if (foundPerson != null && !foundPerson.getInum().equalsIgnoreCase(gluuPerson.getInum())) {
								throw new DuplicateEntryException("Cannot update userName of a different id: " + person.getUserName());
							}
						}
					}
				}
			}

			GluuCustomPerson newGluuPerson = CopyUtils.copy(person, gluuPerson, true);

			if (person.getGroups().size() > 0) {
				Utils.groupMembersAdder(newGluuPerson, personService.getDnForPerson(id));
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			newGluuPerson = Utils.syncEmailForward(newGluuPerson, false);

			// For custom script: update user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateUserMethods(newGluuPerson);
			}

			personService.updatePerson(newGluuPerson);
			log.debug(" person updated ");

			ScimPerson newPerson = CopyUtils.copy(newGluuPerson, null);

			// person_update = CopyUtils.copy(gluuPerson, null, attributes);
			URI location = new URI("/Users/" + id);

			return Response.ok(newPerson).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (DuplicateEntryException ex) {

			log.error("Failed to update user", ex);
			ex.printStackTrace();
			return getErrorResponse(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());

		} catch (Exception ex) {

			log.error("Failed to update user", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteUser(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(id);

			if (gluuPerson == null) {

				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

			} else {

				// For custom script: delete user
				if (externalScimService.isEnabled()) {
					externalScimService.executeScimDeleteUserMethods(gluuPerson);
				}

				log.info("person.getMemberOf().size() : " + gluuPerson.getMemberOf().size());
				if (gluuPerson.getMemberOf() != null) {

					if (gluuPerson.getMemberOf().size() > 0) {

						String dn = personService.getDnForPerson(id);
						log.info("DN : " + dn);

						Utils.deleteUserFromGroup(gluuPerson, dn);
					}
				}

				personService.removePerson(gluuPerson);
			}

			return Response.ok().build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	/*
	public Response createUserTestHelper(@HeaderParam("Authorization") String authorization, String person_data) throws Exception {
		try {
			ScimPerson person = (ScimPerson) xmlToObject(person_data);
			return createUser(authorization, person);
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse("Data parsing error.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response updateUserTestHelper(@HeaderParam("Authorization") String authorization, @PathParam("inum") String inum, String person_data)
			throws Exception {
		try {
			ScimPerson person_update = (ScimPerson) xmlToObject(person_data);
			return updateUser(authorization, inum, person_update);
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Data parsing error.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	private Object xmlToObject(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(xml)));
		DOMReader reader = new DOMReader();
		Document doc = reader.read(document);

		JAXBContext context = JAXBContext.newInstance(ScimPerson.class);
		DocumentSource source = new DocumentSource(doc);
		context = JAXBContext.newInstance(ScimPerson.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller.unmarshal(source);
	}
	*/

	@Path("{id}")
	@PATCH
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUserPatch(@HeaderParam("Authorization") String authorization, @PathParam("id") String id, ScimPersonPatch person) throws Exception {
		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		// TODO Auto-generated method stub
		return null;
	}

	/*
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

			ScimPerson person = CopyUtils.copy(gluuPerson, null);

			URI location = new URI("/Users/" + gluuPerson.getInum());

			return Response.ok(person).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
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
			
			GluuCustomPersonList allPersonList = new GluuCustomPersonList();
			if (personList != null) {
				log.info(" LDAP person list is not empty ");
				for (GluuCustomPerson gluuPerson : personList) {
					log.info(" copying person from GluuPerson to ScimPerson ");
					ScimPerson person = CopyUtils.copy(gluuPerson, null);
					log.info(" adding ScimPerson to the AllPersonList ");
					log.info(" person to be added userid : " + person.getUserName());
					allPersonList.getResources().add(person);
					log.info(" person added? : " + allPersonList.getResources().contains(person));
				}

			}

			List<String> schema = new ArrayList<String>();
			schema.add("urn:scim:schemas:core:1.0");
			log.info(" setting schema ");
			allPersonList.setSchemas(schema);
			List<ScimPerson> resources = allPersonList.getResources();
			allPersonList.setTotalResults((long) resources.size());

			URI location = new URI("/Users/");

			return Response.ok(allPersonList).location(location).build();

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
	*/

	// todo - Update Password is OPTIONAL and should use PATCH method
	// (http://www.simplecloud.info/specs/draft-scim-api-00.html, 3.3.2.
	// Modifying with PATCH)
	// @Path("{inum}/password")
	// @PUT
	// @Consumes(MediaType.APPLICATION_XML)
	// public Response changePassword(@HeaderParam("Authorization") String authorization,
	// @PathParam("inum") String inum, String password);
	//
	// @PUT
	// @Consumes(MediaType.APPLICATION_XML)
	// @Produces(MediaType.APPLICATION_XML)
	// @Path("{id}/updatePassword")
	// Response changeLoginPassword(@PathParam("id") String uid, String
	// accessToken);
	//
	// ///////////
	// // Additional Methods
	// @Path("{inum}/passwordpost")
	// @POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// public Response changePasswordTestHelper(@Context HttpServletRequest
	// request, @PathParam("inum") String inum, @FormParam("password") String
	// password);
	//
	// @Path("{inum}/updatepost")
	// @POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// public Response updateUserTestHelper(@HeaderParam("Authorization") String authorization,
	// @PathParam("inum") String inum, @FormParam("person_data") String
	// person_data);
	//
	// @Path("{inum}/createpost")
	// @POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// public Response createUserTestHelper(@HeaderParam("Authorization") String authorization,
	// @FormParam("person_data") String person);
	// /////////
}
