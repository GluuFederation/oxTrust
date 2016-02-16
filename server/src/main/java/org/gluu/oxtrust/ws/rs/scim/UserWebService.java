/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

import java.io.IOException;
import java.io.StringReader;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentSource;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuCustomPersonList;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonPatch;
import org.gluu.oxtrust.model.scim.ScimPersonSearch;
import org.gluu.oxtrust.service.UmaAuthenticationService;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xml.sax.InputSource;

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
	private UmaAuthenticationService umaAuthenticationService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response listUsers(@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			log.info(" getting a list of all users from LDAP ");
			List<GluuCustomPerson> personList = personService.findAllPersons(null);
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
			schema.add("urn:scim2:schemas:core:1.0");
			log.info(" setting schema ");
			allPersonList.setSchemas(schema);
			List<ScimPerson> resources = allPersonList.getResources();
			allPersonList.setTotalResults((long) resources.size());

			URI location = new URI("/Users/");
			return Response.ok(allPersonList).location(location).build();
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getUserByUid(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			ScimPerson person = CopyUtils.copy(gluuPerson, null);

			URI location = new URI("/Users/" + uid);

			return Response.ok(person).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createUser(@HeaderParam("Authorization") String authorization, ScimPerson person) throws WebApplicationException,
			JsonGenerationException, JsonMappingException, IOException, Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuperson ");
		GluuCustomPerson gluuPerson = CopyUtils.copy(person, null, false);
		if (gluuPerson == null) {
			return getErrorResponse("Failed to create user", Response.Status.BAD_REQUEST.getStatusCode());
		}

		try {
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
			ScimPerson newPerson = CopyUtils.copy(gluuPerson, null);
			String uri = "/Users/" + newPerson.getId();
			return Response.created(URI.create(uri)).entity(newPerson).build();
		} catch (Exception ex) {
			log.error("Failed to add user", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUser(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid, ScimPerson person) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			GluuCustomPerson newGluuPesron = CopyUtils.copy(person, gluuPerson, true);

			if (person.getGroups().size() > 0) {
				Utils.groupMemebersAdder(newGluuPesron, personService.getDnForPerson(uid));
			}

			personService.updatePerson(newGluuPesron);
			log.debug(" person updated ");
			ScimPerson newPerson = CopyUtils.copy(newGluuPesron, null);

			// person_update = CopyUtils.copy(gluuPerson, null, attributes);
			URI location = new URI("/Users/" + uid);
			return Response.ok(newPerson).location(location).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{uid}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteUser(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
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
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

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

	@Path("{uid}")
	@PATCH
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUserPatch(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid, ScimPersonPatch person) throws Exception {
		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Path("/Search")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response personSearch(@HeaderParam("Authorization") String authorization, ScimPersonSearch searchPattern) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
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
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
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
			schema.add("urn:scim2:schemas:core:1.0");
			log.info(" setting schema ");
			allPersonList.setSchemas(schema);
			List<ScimPerson> resources = allPersonList.getResources();
			allPersonList.setTotalResults((long) resources.size());

			URI location = new URI("/Users/");
			return Response.ok(allPersonList).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

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
