package org.gluu.oxtrust.api.rest;

//
// Annotations 
//

// Designate this class as a JAX-RS resource/service
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentSource;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.Error;
import org.gluu.oxtrust.model.Errors;
import org.gluu.oxtrust.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuCustomPersonList;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.model.Person;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.xdi.util.StringHelper;
import org.xml.sax.InputSource;

@Path("/users")
public class UserRestServiceImpl implements UserRestService {

	private static final Logger log = Logger.getLogger(UserRestServiceImpl.class);

	@In
	private PersonService personService;

	@In
	private InumService inumService;

	public Response createUser(@Context HttpServletRequest request, Person person) throws WebApplicationException {

		personService = PersonService.instance();
		inumService = InumService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		AttributeService attributeService = AttributeService.instance();
		List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
		GluuUserRole role = null;
		if (isOwnerOrManager(invoker)) {
			role = GluuUserRole.ADMIN;
			attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		} else {
			role = GluuUserRole.USER;
			attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
		}

		// Return HTTP response with status code 201 Created
		GluuCustomPerson gluuPerson = CopyUtils.copy(person, null, attributes, role, false);
		if (gluuPerson == null) {
			return getErrorResponse("Failed to create user", Response.Status.BAD_REQUEST.getStatusCode());
		}

		try {
			String inum = inumService.generateInums(OxTrustConstants.INUM_TYPE_PEOPLE_SLUG); // personService.generateInumForNewPerson();
			String dn = personService.getDnForPerson(inum);
			String iname = personService.generateInameForNewPerson(person.getUserId());
			person.setIname(iname);
			gluuPerson.setDn(dn);
			gluuPerson.setInum(inum);
			gluuPerson.setIname(iname);
			personService.addPerson(gluuPerson);

			person = CopyUtils.copy(gluuPerson, null, attributes);
			String uri = "/users/" + person.getInum(); // person.getUserId();
			return Response.created(URI.create(uri)).entity(person).build();
		} catch (Exception ex) {
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response getUser(@Context HttpServletRequest request) {
		personService = PersonService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			List<GluuCustomPerson> personList = personService.findAllPersons(null);
			GluuCustomPersonList allPersonList = new GluuCustomPersonList();
			if (personList != null) {
				AttributeService attributeService = AttributeService.instance();
				for (GluuCustomPerson gluuPerson : personList) {

					List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
					if (isOwnerOrManager(invoker)) {
						attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
					} else {
						attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
					}

					Person person = CopyUtils.copy(gluuPerson, null, attributes);
					allPersonList.getPersonList().add(person);
				}
			}
			URI location = new URI("/users/");
			return Response.ok(allPersonList).location(location).build();
		} catch (Exception ex) {
			log.error("Exception: " + ex.getMessage());
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response getUserByUserId(@Context HttpServletRequest request, @PathParam("id") String uid) throws WebApplicationException {
		personService = PersonService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByUid(uid);
			if (gluuPerson == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			AttributeService attributeService = AttributeService.instance();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			if (isOwnerOrManager(invoker)) {
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
			} else {
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
			}

			Person person = CopyUtils.copy(gluuPerson, null, attributes);
			URI location = new URI("/users/" + uid);
			return Response.ok(person).location(location).build();
		} catch (Exception ex) {
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response getUserByInum(@Context HttpServletRequest request, @PathParam("inum") String inum) throws WebApplicationException {
		personService = PersonService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null) { // || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(inum);
			if (gluuPerson == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			AttributeService attributeService = AttributeService.instance();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			if (isOwnerOrManager(invoker)) {
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
			} else {
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
			}

			Person person = CopyUtils.copy(gluuPerson, null, attributes);
			URI location = new URI("/users/" + inum);
			return Response.ok(person).location(location).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response updateUser(@Context HttpServletRequest request, @PathParam("inum") String inum, Person person_update)
			throws WebApplicationException {
		personService = PersonService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(inum);
			if (gluuPerson == null) {
				return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			AttributeService attributeService = AttributeService.instance();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			GluuUserRole role = null;
			if (isOwnerOrManager(invoker)) {
				role = GluuUserRole.ADMIN;
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
			} else {
				role = GluuUserRole.USER;
				attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
			}

			gluuPerson = CopyUtils.copy(person_update, gluuPerson, attributes, role, true);
			personService.updatePerson(gluuPerson);

			person_update = CopyUtils.copy(gluuPerson, null, attributes);
			URI location = new URI("/users/" + inum);
			return Response.ok(person_update).location(location).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			ex.printStackTrace();
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response changePassword(@Context HttpServletRequest request, @PathParam("inum") String inum, String password) {
		try {
			personService = PersonService.instance();
			GluuCustomPerson gluuPerson = personService.getPersonByInum(inum);

			GluuCustomPerson invoker = getAuthorizedUser(request, personService);
			if (invoker == null || (!isOwnerOrManager(invoker) && !invoker.getInum().equals(gluuPerson.getInum()))) {
				return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
			}

			if (gluuPerson == null) {
				return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			gluuPerson = CopyUtils.updatePassword(gluuPerson, password);
			if (gluuPerson == null) {
				return getErrorResponse("Failed to update the password", Response.Status.NOT_MODIFIED.getStatusCode());
			}
			personService.updatePerson(gluuPerson);
			return Response.status(Response.Status.NO_CONTENT.getStatusCode()).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response deleteUser(@Context HttpServletRequest request, @PathParam("inum") String inum) throws WebApplicationException {
		personService = PersonService.instance();

		GluuCustomPerson invoker = getAuthorizedUser(request, personService);
		if (invoker == null || !isOwnerOrManager(invoker)) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			GluuCustomPerson person = personService.getPersonByInum(inum);
			if (person == null) {
				return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
			} else {
				personService.removePerson(person);
			}
			return Response.ok().build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + inum + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public Response createUserTestHelper(@Context HttpServletRequest request, String person_data) throws WebApplicationException {
		Person person = new Person();
		try {
			person = (Person) xmlToObject(person_data);
		} catch (Exception ex) {
			return getErrorResponse("Data parsing error.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return createUser(request, person);
	}

	public Response updateUserTestHelper(@Context HttpServletRequest request, @PathParam("inum") String inum, String person_data)
			throws WebApplicationException {
		Person person_update = new Person();
		try {
			person_update = (Person) xmlToObject(person_data);
		} catch (Exception ex) {
			return getErrorResponse("Data parsing error.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return updateUser(request, inum, person_update);
	}

	private Object xmlToObject(String xml) throws Exception {
		Object obj = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(xml)));
		DOMReader reader = new DOMReader();
		Document doc = reader.read(document);

		JAXBContext context = JAXBContext.newInstance(Person.class);
		DocumentSource source = new DocumentSource(doc);
		context = JAXBContext.newInstance(Person.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		obj = unmarshaller.unmarshal(source);
		return obj;
	}

	public Response changePasswordTestHelper(@Context HttpServletRequest request, @PathParam("inum") String inum, String password) {
		return changePassword(request, inum, password);
	}

	private Response getErrorResponse(String errMsg, int statusCode) {
		Errors errors = new Errors();
		Error error = new Error(errMsg, statusCode, "");
		errors.getError().add(error);
		return Response.status(statusCode).entity(errors).build();
	}

	private GluuCustomPerson getAuthorizedUser(HttpServletRequest request, PersonService personService) {
		log.debug("Checking if user is authenticated with shibboleth");
		GluuCustomPerson person = null;
		try {

			String authType = request.getAuthType();
			String userUid = request.getHeader("REMOTE_USER");

			log.debug("AuthType: " + authType);
			log.debug("UserId: " + userUid);

			if (StringHelper.isEmpty(userUid) || StringHelper.isEmpty(authType) || !authType.equals("shibboleth")) {
				return null;
			}

			// Find user by uid
			person = personService.getPersonByUid(userUid);
			if (person == null) {
				log.error("\n\n\nPerson Not Found: " + userUid + "\n\n\n");
			}

		} catch (Exception ex) {
			log.error("Exception while authorization check: " + ex.getMessage());
		}

		return person;
	}

	private boolean isOwnerOrManager(GluuCustomPerson invoker) {
		SecurityService securityService = SecurityService.instance();
		GluuUserRole[] userRoles = securityService.getUserRoles(invoker);
		for (GluuUserRole userRole : userRoles) {
			if (("manager").equals(userRole.getRoleName()) || ("owner").equals(userRole.getRoleName())) {
				return true;
			}
		}
		return false;
	}
}
