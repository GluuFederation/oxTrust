package org.gluu.oxtrust.ws.rs.scim;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthCustomClient;
import org.gluu.oxtrust.model.association.ClientAssociation;
import org.gluu.oxtrust.model.association.PersonAssociation;
import org.gluu.oxtrust.util.MapperUtil;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

@Name("clientAssociationWebService")
@Path("/ClientAssociation")
public class ClientAssociationWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private PersonService personService;

	@In
	private ClientService clientService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@Path("/User/{uid}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAssociatedClients(@HeaderParam("Authorization") String authorization, @PathParam("uid") String uid) throws Exception {

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

			PersonAssociation personAssociation = MapperUtil.map(gluuPerson, null);

			URI location = new URI("/ClientAssociation/User/" + uid);

			return Response.ok(personAssociation).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + uid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("/Client/{cid}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAssociatedPersons(@HeaderParam("Authorization") String authorization, @PathParam("cid") String cid) throws Exception {
		clientService = ClientService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			log.info("getting the client");
			OxAuthClient client = clientService.getClientByInum(cid);

			if (client == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + cid + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			log.info("mapping client attributes");
			ClientAssociation clientAssociation = MapperUtil.map(client, null);
			log.info("getting URL");
			URI location = new URI("/ClientAssociation/Client/" + cid);
			log.info("returning response");
			return Response.ok(clientAssociation).location(location).build();

		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + cid + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

	}

	@Path("/Associate/")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createAssociation(@HeaderParam("Authorization") String authorization, PersonAssociation personAssociation) throws Exception {

		personService = PersonService.instance();
		clientService = ClientService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			log.info("creating an instance of gluuCustomperson");
			GluuCustomPerson person = personService.getPersonByInum(personAssociation.getUserAssociation().replaceAll(" ", ""));

			log.info("setting AssociatedClientDNs");
			List<String> cleanCDNList = new ArrayList<String>();
			for (String dn : personAssociation.getEntryAssociations()) {
				cleanCDNList.add(dn.replaceAll(" ", ""));
			}

			person.setAssociatedClient(cleanCDNList);

			log.info("updating person");

			personService.updatePerson(person);

			log.info("setting user in clients");
			for (String clientDn : personAssociation.getEntryAssociations()) {
				log.info("getting a client");
				OxAuthCustomClient client = clientService.getClientByAttributeCustom(applicationConfiguration
						.getClientAssociationAttribute(), clientDn.replaceAll(" ", ""));

				log.info("the inum of the client ", client.getInum());

				log.info("checking if the list is empty");
				boolean isAPDNsEmpty = client.getAttributes("associatedPerson") == null;

				log.info("instantiating a new arraylist");

				List<String> listOfpersons = new ArrayList<String>();
				log.info("getting AssociatedPersonDN");
				if (!isAPDNsEmpty) {
					listOfpersons = new ArrayList(Arrays.asList(client.getAttributes("associatedPerson")));
					/*
					 * for(String dn :
					 * client.getAttributes("associatedPersonDN")){ if(dn !=
					 * null && !dn.equalsIgnoreCase("")){listOfpersons.add(dn);}
					 * }
					 */

				}
				log.info("getting persons dn");
				String personInum = personAssociation.getUserAssociation().replaceAll(" ", "");

				if (isAPDNsEmpty || !listOfpersons.contains(personInum)) {
					log.info("adding person");
					listOfpersons.add(personInum);
				}

				String[] arrayOfpersons = new String[listOfpersons.size()];
				for (int i = 0; i < listOfpersons.size(); i++) {
					arrayOfpersons[i] = listOfpersons.get(i);
				}
				log.info("setting list of AssociatedPersonDns");
				client.setAttribute("associatedPerson", arrayOfpersons);
				log.info("Updating client");
				clientService.updateCustomClient(client);
			}

			String uri = "/ClientAssociation/Associate/" + person.getInum();
			log.info("returning response");
			return Response.created(URI.create(uri)).entity(personAssociation).build();

		} catch (Exception ex) {
			log.error("Failed to add Association", ex);
			// log.info("Failed to add Association" , ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

	}

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteAssociation(@HeaderParam("Authorization") String authorization, PersonAssociation personAssociation) throws Exception {

		personService = PersonService.instance();
		clientService = ClientService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			log.info("Creating an instance of GluuCustomPerson");
			GluuCustomPerson person = personService.getPersonByInum(personAssociation.getUserAssociation().replaceAll(" ", ""));
			log.info("getting a list of clientDNs");
			List<String> listClientDNs = new ArrayList<String>();

			boolean isACDNsEmpty = person.getAssociatedClient() == null;

			if (!isACDNsEmpty) {
				for (String dn : person.getAssociatedClient()) {
					log.info("isACDNsEmpty = false");
					if (dn != null && !dn.equalsIgnoreCase("")) {
						listClientDNs.add(dn.replaceAll(" ", ""));
					}

				}
			}

			log.info("getting a list of clean clients");

			List<String> cleanPACDNs = new ArrayList<String>();
			for (String dn : personAssociation.getEntryAssociations()) {
				if (dn != null && !dn.equalsIgnoreCase("")) {
					cleanPACDNs.add(dn);
				}
			}

			log.info("removing clientdns");

			for (String clientdn : cleanPACDNs) {

				if (listClientDNs.contains(clientdn)) {
					listClientDNs.remove(clientdn);
				}

			}

			log.info("geting a cleanlist");

			List<String> cleanList = new ArrayList<String>();
			for (String cDn : listClientDNs) {
				if (cDn != null && !cDn.equalsIgnoreCase("")) {
					cleanList.add(cDn);
				}
			}
			log.info("setting AssociatedClientDNs");
			if (cleanList.size() < 1) {
				person.setAssociatedClient(null);
			} else {
				person.setAssociatedClient(cleanList);
			}

			log.info("Updating person");

			personService.updatePerson(person);

			log.info("deleting user dn from clients");

			List<String> EntryAssociations = new ArrayList<String>();

			for (String dn : personAssociation.getEntryAssociations()) {
				if (dn != null && !dn.equalsIgnoreCase("")) {
					EntryAssociations.add(dn.replaceAll(" ", ""));
				}
			}

			for (String clientDn : EntryAssociations) {
				log.info("getting a client");

				OxAuthCustomClient client = clientService.getClientByAttributeCustom(applicationConfiguration
						.getClientAssociationAttribute(), clientDn.replaceAll(" ", ""));
				// String[] personDNS =
				// client.getAttributes("associatedPersonDN");
				log.info("checking if the associatedPerson is empty");
				log.info("client dn : ", client.getDn());
				boolean isAPDNsEmpty = client.getAttributes("associatedPerson") == null;
				log.info("new ArrayList");
				List<String> list = new ArrayList<String>();
				if (!isAPDNsEmpty) {
					log.info("!isAPDNsEmpty");
					// list =
					// Arrays.asList(client.getAttributes("associatedPersonDN"));
					for (int i = 0; i < client.getAttributes("associatedPerson").length; i++) {
						if (client.getAttributes("associatedPerson")[i] != null
								&& !client.getAttributes("associatedPerson")[i].equalsIgnoreCase("")) {
							list.add(client.getAttributes("associatedPerson")[i]);
						}
					}
					/*
					 * for(String dn : client.getAssociatedPersonDNs()){ if(dn
					 * != null && !dn.equalsIgnoreCase("")){list.add(dn);} }
					 */
				}
				log.info("getting personDN");
				String personInum = personAssociation.getUserAssociation().replaceAll(" ", "");

				if (list.contains(personInum)) {
					log.info("removing person's dn");
					list.remove(personInum);
				}

				log.info("Creating a clean list");

				List<String> cleanPersonList = new ArrayList<String>();
				for (String cDn : list) {
					if (cDn != null && cDn.equalsIgnoreCase("")) {
						cleanPersonList.add(cDn);
					}
				}
				log.info("Setting AssociatedPersonDNs");
				if (cleanPersonList.size() < 1) {
					String[] nullArray = null;
					client.setAttribute("associatedPerson", nullArray);
				} else {
					String[] arrayPersonDns = new String[cleanPersonList.size()];
					for (int i = 0; i < cleanPersonList.size(); i++) {
						arrayPersonDns[i] = cleanPersonList.get(i);
					}
					client.setAttribute("associatedPerson", arrayPersonDns);
				}
				clientService.updateCustomClient(client);

			}
			log.info("returning result;");

			return Response.ok().build();
		} catch (Exception ex) {
			log.info("Exception: ", ex);
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

}
