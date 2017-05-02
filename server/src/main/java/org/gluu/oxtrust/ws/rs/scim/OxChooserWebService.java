/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.oxchooser.ForwardedRequest;
import org.gluu.oxtrust.model.oxchooser.IdentityRequest;
import org.gluu.oxtrust.model.oxchooser.IdentityResponse;
import org.gluu.oxtrust.model.oxchooser.InitialID;
import org.gluu.oxtrust.model.oxchooser.OxChooserError;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.jboss.seam.security.Identity;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.xdi.model.GluuUserRole;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

@Named("oxChooserWebService")
@Path("/scim/v1/Chooser")
public class OxChooserWebService extends BaseScimWebService {

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private SecurityService securityService;

	@Inject
	Identity identity;

	private static ConsumerManager manager = new ConsumerManager();

	@Path("/Request")
	@GET
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response requestHandler(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@QueryParam("idRequest") String idReq) throws Exception {

		try {
			byte[] decodedIdreq = Base64.decodeBase64(idReq);
			IdentityRequest idRequest = (IdentityRequest) jsonToObject(decodedIdreq, IdentityRequest.class);
			log.debug("openid_identifier_operation : ", idRequest.getIdentifier());
			log.debug("instantiating manager");

			log.debug("manager instantiated ");

			String returnToUrl = idRequest.getReturnToUrl();
			log.debug("getting list of discoveries");
			List discoveries = manager.discover(idRequest.getIdentifier());
			log.debug("retrieving descovered");
			DiscoveryInformation discovered = manager.associate(discoveries);
			log.debug("saving request");
			request.getSession().setAttribute("openid-disc", discovered);
			log.debug("instantiating AuthRequest");
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl, idRequest.getRealm());

			FetchRequest fetch = FetchRequest.createFetchRequest();
			if (idRequest.getAxschema().contains("axschema")) {

				fetch.addAttribute("nickname", "http://axschema.org/namePerson/friendly", true);
				fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
				fetch.addAttribute("email", "http://axschema.org/contact/email", true);
				fetch.addAttribute("gender", "http://axschema.org/person/gender", true);
				fetch.addAttribute("language", "http://axschema.org/pref/language", true);
				fetch.addAttribute("timezone", "http://axschema.org/pref/timezone", true);
				fetch.addAttribute("image", "http://axschema.org/media/image/default", true);

			} else {
				fetch.addAttribute("firstname", "http://schema.openid.net/namePerson/first", true);
				fetch.addAttribute("lastname", "http://schema.openid.net/namePerson/last", true);
				fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
				fetch.addAttribute("country", "http://axschema.org/contact/country/home", true);
				fetch.addAttribute("language", "http://axschema.org/pref/language", true);

			}

			log.debug("adding fetch data");
			authReq.addExtension(fetch);
			log.debug("redirecting");
			response.sendRedirect(authReq.getDestinationUrl(true));
			log.debug("reterning build");
			return Response.ok().build();

		} catch (ConsumerException e) {
			log.debug("Error occured : ", e.getMessage(), " ", e.getCause());
			OxChooserError error = new OxChooserError();
			error.setDescription("An Error occured , request didnt go through.");
			return Response.status(400).entity(error).build();
		} finally {
			identity.logout();
		}

	}

	@Path("/Response")
	@GET
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response responseHandler(@Context HttpServletRequest httpReq, @Context HttpServletResponse httpRes, ForwardedRequest frequest)
			throws ConsumerException {

		try {
			log.debug("instantiating a ParameterList ");
			ParameterList response = new ParameterList(frequest.getParameterMap());
			log.debug("getting DiscoveryInformation ");
			DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute("openid-disc");

			log.debug("getting StringBuffer ");
			StringBuffer receivingURL = frequest.getRequestURL();

			log.debug("getting QueryString ");
			String queryString = frequest.getQueryString();

			if (queryString != null && queryString.length() > 0)
				log.debug("getting receivingURL ");
			receivingURL.append("?").append(frequest.getQueryString());
			log.debug("getting VerificationResult ");
			VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);
			log.debug("getting VerificationResult ");
			Identifier verified = verification.getVerifiedId();
			log.debug(" VerificationResult retrieved ");

			if (verified != null) {
				log.debug("verified != null");
				AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					log.debug("getting FetchResponse");
					FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
					log.debug("getting emails");
					List emails = fetchResp.getAttributeValues("email");
					log.debug("getting FirstName");
					String firstName = fetchResp.getAttributeValue("firstname");
					log.debug("getting LastName");
					String lastName = fetchResp.getAttributeValue("lastname");
					log.debug("getting one Email");
					String email = (String) emails.get(0);
					log.debug("email : ", email);
					String nickName = fetchResp.getAttributeValue("nickname");
					String Image = fetchResp.getAttributeValue("image");
					String Language = fetchResp.getAttributeValue("language");
					String Country = fetchResp.getAttributeValue("country");
					String Timezone = fetchResp.getAttributeValue("timezone");
					String Gender = fetchResp.getAttributeValue("gender");
					String Fullname = fetchResp.getAttributeValue("fullname");

					IdentityResponse idResponse = new IdentityResponse();
					idResponse.setFirstname(firstName);
					idResponse.setLastname(lastName);
					idResponse.setEmail(email);
					idResponse.setNickname(nickName);
					idResponse.setImage(Image);
					idResponse.setLanguage(Language);
					idResponse.setCountry(Country);
					idResponse.setTimezone(Timezone);
					idResponse.setGender(Gender);
					idResponse.setFullname(Fullname);
					return Response.ok(idResponse).build();

				}

				return errorResponse("Could not get fetched attributes");
			}

		} catch (AssociationException e) {
			return errorResponse("An AssociationException occured , please check your request.");
		} catch (MessageException e) {
			return errorResponse("An MessageException occured , please check your request.");
		} catch (DiscoveryException e) {
			return errorResponse("An DiscoveryException occured , please check your request.");
		} finally {
			identity.logout();
		}

		return errorResponse("An Error occured , please check your request.");
	}

	private Response errorResponse(String p_description) {
		return Response.status(400).entity(new OxChooserError(p_description)).build();
	}

	@Path("/AddUser")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response addUser(@HeaderParam("Authorization") String authorization, ScimPerson person) throws Exception {
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
			String inum = personService.generateInumForNewPerson();
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

			log.debug("adding new GluuPerson");

			personService.addPerson(gluuPerson);
			final ScimPerson newPerson = CopyUtils.copy(gluuPerson, null);
			String uri = "/oxChooser/AddUser/" + newPerson.getId();
			return Response.created(URI.create(uri)).entity(newPerson).build();
		} catch (Exception ex) {
			log.error("Failed to add user", ex);

			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("/EditUser/{email}")
	@GET
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response editUser(@HeaderParam("Authorization") String authorization, @PathParam("email") String email,
			ScimPerson person_update) throws Exception {
		personService = PersonService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByEmail(email);
			if (gluuPerson == null) {
				return getErrorResponse("Resource " + email + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			GluuCustomPerson newGluuPesron = CopyUtils.copy(person_update, gluuPerson, true);

			if (person_update.getGroups().size() > 0) {
				Utils.groupMembersAdder(newGluuPesron, personService.getDnForPerson(gluuPerson.getUid()));
			}

			personService.updatePerson(newGluuPesron);
			log.debug(" person updated ");
			ScimPerson newPerson = CopyUtils.copy(newGluuPesron, null);

			URI location = new URI("/oxChooser/AddUser/" + gluuPerson.getUid());
			return Response.ok(newPerson).location(location).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + email + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("/AddUser/{uid}")
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
			URI location = new URI("/oxChooser/AddUser/" + uid);
			return Response.ok(person).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("/Test")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMarshallingTest() {
		try {
			final IdentityRequest idRequest = new IdentityRequest();
			idRequest.setAxschema("openid");
			idRequest.setIdentifier("https://www.google.com/accounts/o8/id");
			idRequest.setRealm("http://www.gluu.org");
			idRequest.setReturnToUrl("http://www.gluu.org");

			return Response.ok(idRequest).build();
		} catch (Exception ex) {
			return Response.ok("<error>an Error occured!</error>").build();
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response clientIdentification(InitialID id) throws DiscoveryException, Exception {
		try {
			if (personService.authenticate(id.getUserID(), id.getPassWord())) {
				GluuCustomPerson user = personService.getPersonByUid(id.getUserID());
				postLogin(user);
				return Response.ok().build();
			} else {
				return Response.status(401).entity("Not Authorized").build();
			}
		} catch (Exception ex) {
			log.error("an error occured", ex);
			return Response.status(401).entity("Not Authorized").build();
		}
	}

	public void postLogin(GluuCustomPerson person) throws Exception {
		log.debug("Configuring application after user '{0}' login", person.getUid());
		Contexts.getSessionContext().set(OxTrustConstants.CURRENT_PERSON, person);

		// Set user roles
		GluuUserRole[] userRoles = securityService.getUserRoles(person);
		for (GluuUserRole userRole : userRoles) {
			identity.addRole(userRole.getRoleName());
		}
	}

	private Object jsonToObject(byte[] json, Class<?> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, clazz);
	}

}
