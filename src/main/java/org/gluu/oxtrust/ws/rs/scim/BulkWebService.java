package org.gluu.oxtrust.ws.rs.scim;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.SecurityService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.model.scim.BulkRequests;
import org.gluu.oxtrust.model.scim.BulkResponseStatus;
import org.gluu.oxtrust.model.scim.BulkResponses;
import org.gluu.oxtrust.model.scim.Error;
import org.gluu.oxtrust.model.scim.Errors;
import org.gluu.oxtrust.model.scim.ScimBulkOperation;
import org.gluu.oxtrust.model.scim.ScimBulkResponse;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.xdi.ldap.model.GluuStatus;

/**
 * SCIM Bulk Implementation
 * 
 * @author Reda Zerrad Date: 04.19.2012
 */
@Name("BulkWebService")
@Path("/Bulk")
public class BulkWebService extends BaseScimWebService {

	private static final Logger log = Logger.getLogger(BulkWebService.class);

	@In
	private PersonService personService;

	@In
	private GroupService groupService;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response bulkOperation(@Context HttpServletRequest request, @HeaderParam("Authorization") String authorization, ScimBulkOperation operation) throws WebApplicationException,
			MalformedURLException, URISyntaxException, JsonGenerationException, JsonMappingException, IOException, Exception {

		personService = PersonService.instance();
		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		String domain;
		URL reconstructedURL;
		reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
		domain = reconstructedURL.toString();
		log.info(" getting list of BulkRequest ");
		List<BulkRequests> bulkRequests = operation.getOperations();

		ScimBulkResponse scimBulkResponse = new ScimBulkResponse();

		List<BulkResponses> listResponses = new ArrayList<BulkResponses>();

		for (BulkRequests oneRequest : bulkRequests) {
			log.info(" checking operations ");

			if (oneRequest.getPath().contains("Users")) {
				log.info("  operations is for Users ");
				log.info(" method : " + oneRequest.getMethod());
				if (oneRequest.getMethod().equalsIgnoreCase("POST")) {
					log.info(" method is post ");

					String bulkId = oneRequest.getBulkId();
					String method = oneRequest.getMethod();
					ScimPerson person = oneRequest.getData();
					boolean status = createUser(person);

					if (status) {
						log.info(" POST status is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setBulkId(bulkId);
						bulkResponses.setMethod(method);
						GluuCustomPerson gluuPerson = personService.getPersonByUid(person.getUserName());
						String iD = gluuPerson.getInum();
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Users/").append(iD)
								.toString();
						bulkResponses.setLocation(location);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("201");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" POST status is false ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setBulkId(bulkId);
						bulkResponses.setMethod(method);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}

				} else if (oneRequest.getMethod().equalsIgnoreCase("PUT")) {
					log.info(" Status is PUT ");

					String method = oneRequest.getMethod();
					String version = oneRequest.getVersion();
					String path = oneRequest.getPath();
					ScimPerson person = oneRequest.getData();
					String personiD = getId(path);
					log.info(" Inum :  " + getId(path));

					boolean status = updateUser(personiD, person);

					if (status) {
						log.info(" PUT status is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						String iD = personiD;
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Users/").append(iD)
								.toString();
						bulkResponses.setLocation(location);
						EntityTag eTag = new EntityTag(version, true);
						String newVersion = eTag.getValue();
						bulkResponses.setVersion(newVersion);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("200");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" PUT status is false ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						bulkResponses.setVersion(version);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}
				} else if (oneRequest.getMethod().equalsIgnoreCase("DELETE")) {
					log.info(" Operation is DELETE ");

					String method = oneRequest.getMethod();
					String path = oneRequest.getPath();
					String personiD = getId(path);
					boolean status = deleteUser(personiD);

					if (status) {
						log.info(" DELETE operation is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Users/")
								.append(personiD).toString();
						bulkResponses.setLocation(location);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("200");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" DELETE operation is False ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}

				} //

			} else if (oneRequest.getPath().contains("Groups")) {

				if (oneRequest.getMethod().equalsIgnoreCase("POST")) {
					log.info(" method is post ");

					String bulkId = oneRequest.getBulkId();
					String method = oneRequest.getMethod();

					ScimGroup group = CopyUtils.copy(oneRequest.getData(), null);
					boolean status = createGroup(group);

					if (status) {
						log.info(" POST status is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setBulkId(bulkId);
						bulkResponses.setMethod(method);
						GluuGroup gluuGroup = groupService.getGroupByDisplayName(group.getDisplayName());
						String iD = gluuGroup.getInum();
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Groups/").append(iD)
								.toString();
						bulkResponses.setLocation(location);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("201");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" POST status is false ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setBulkId(bulkId);
						bulkResponses.setMethod(method);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}

				} else if (oneRequest.getMethod().equalsIgnoreCase("PUT")) {
					log.info(" Status is PUT ");

					String method = oneRequest.getMethod();
					String version = oneRequest.getVersion();
					String path = oneRequest.getPath();
					ScimGroup group = CopyUtils.copy(oneRequest.getData(), null);
					String groupiD = getId(path);

					boolean status = updateGroup(groupiD, group);

					if (status) {
						log.info(" PUT status is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						String iD = groupiD;
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Groups/").append(iD)
								.toString();
						bulkResponses.setLocation(location);
						EntityTag eTag = new EntityTag(version, true);
						String newVersion = eTag.getValue();
						bulkResponses.setVersion(newVersion);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("200");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" PUT status is false ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						bulkResponses.setVersion(version);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}
				} else if (oneRequest.getMethod().equalsIgnoreCase("DELETE")) {
					log.info(" Operation is DELETE ");

					String method = oneRequest.getMethod();
					String path = oneRequest.getPath();
					String groupiD = getId(path);
					boolean status = deleteGroup(groupiD);

					if (status) {
						log.info(" DELETE operation is true ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						String location = (new StringBuilder()).append(domain).append("/oxTrust/seam/resource/restv1/Groups/")
								.append(groupiD).toString();
						bulkResponses.setLocation(location);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("200");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);

					} else if (!status) {
						log.info(" DELETE operation is False ");

						BulkResponses bulkResponses = new BulkResponses();
						bulkResponses.setMethod(method);
						BulkResponseStatus result = new BulkResponseStatus();
						result.setCode("400");
						result.setDescription("Request is unparseable, syntactically incorrect, or violates schema.");
						bulkResponses.setStatus(result);
						listResponses.add(bulkResponses);
					}

				}
			}

		}
		List<String> schemas = new ArrayList<String>();
		schemas.add("urn:scim:schemas:core:1.0");
		scimBulkResponse.setSchemas(schemas);
		scimBulkResponse.setOperations(listResponses);

		URI location = new URI("/Bulk/");
		return Response.ok(scimBulkResponse).location(location).build();

	}

	private boolean createUser(ScimPerson person) throws Exception {

		personService = PersonService.instance();

		GluuCustomPerson gluuPerson = CopyUtils.copy(person, null, false);
		if (gluuPerson == null) {
			return false;
		}

		try {

			String inum = personService.generateInumForNewPerson(); // inumService.generateInums(Configuration.INUM_TYPE_PEOPLE_SLUG);
																	// //personService.generateInumForNewPerson();

			String dn = personService.getDnForPerson(inum);

			String iname = personService.generateInameForNewPerson(person.getUserName());
			gluuPerson.setDn(dn);
			gluuPerson.setInum(inum);
			gluuPerson.setIname(iname);
			gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

			if (person.getGroups().size() > 0) {
				Utils.groupMemebersAdder(gluuPerson, gluuPerson.getDn());
			}

			personService.addPerson(gluuPerson);

			return true;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private boolean updateUser(String uid, ScimPerson person_update) throws Exception {
		personService = PersonService.instance();

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				return false;
			}

			gluuPerson = CopyUtils.copy(person_update, gluuPerson, true);

			if (person_update.getGroups().size() > 0) {
				Utils.groupMemebersAdder(gluuPerson, personService.getDnForPerson(uid));
			}
			personService.updatePerson(gluuPerson);

			return true;
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return false;
		}
	}

	private boolean deleteUser(String uid) throws Exception {
		personService = PersonService.instance();

		try {
			GluuCustomPerson person = personService.getPersonByInum(uid);
			if (person == null) {
				return false;
			} else {
				if (person.getMemberOf() != null) {
					if (person.getMemberOf().size() > 0) {
						String dn = personService.getDnForPerson(uid);
						Utils.deleteUserFromGroup(person, dn);
					}
				}
				personService.removePerson(person);
			}

			return true;
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private boolean createGroup(ScimGroup group) throws Exception {
		groupService = GroupService.instance();

		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = CopyUtils.copy(group, null, false);
		if (gluuGroup == null) {
			return false;
		}

		try {
			log.debug(" generating inum ");
			String inum = groupService.generateInumForNewGroup();
			log.debug(" getting DN ");
			String dn = groupService.getDnForGroup(inum);
			log.debug(" getting iname ");
			String iname = groupService.generateInameForNewGroup(group.getDisplayName().replaceAll(" ", ""));
			log.debug(" setting dn ");
			gluuGroup.setDn(dn);
			log.debug(" setting inum ");
			gluuGroup.setInum(inum);
			log.debug(" setting iname ");
			gluuGroup.setIname(iname);
			log.debug("adding new GluuGroup");

			if (group.getMembers().size() > 0) {
				Utils.personMemebersAdder(gluuGroup, dn);
			}

			groupService.addGroup(gluuGroup);

			return true;
		} catch (Exception ex) {
			log.error("Failed to add user", ex);
			return false;
		}
	}

	private boolean updateGroup(String id, ScimGroup group) throws Exception {

		groupService = GroupService.instance();

		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				return false;
			}
			GluuGroup newGluuGroup = CopyUtils.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMemebersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			groupService.updateGroup(newGluuGroup);
			log.debug(" group updated ");

			return true;
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return false;
		}

	}

	private boolean deleteGroup(String id) throws Exception {
		groupService = GroupService.instance();

		try {
			GluuGroup group = groupService.getGroupByInum(id);
			if (group == null) {
				return false;
			} else {
				if (group.getMembers() != null) {
					if (group.getMembers().size() > 0) {
						String dn = groupService.getDnForGroup(id);
						Utils.deleteGroupFromPerson(group, dn);
					}
				}

				groupService.removeGroup(group);
			}
			return true;
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private String getId(String path) {

		String str[] = path.split("/");
		return str[2];

	}

}
