/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim.BulkRequests;
import org.gluu.oxtrust.model.scim.BulkResponseStatus;
import org.gluu.oxtrust.model.scim.BulkResponses;
import org.gluu.oxtrust.model.scim.ScimBulkOperation;
import org.gluu.oxtrust.model.scim.ScimBulkResponse;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;

/**
 * SCIM Bulk Implementation
 * 
 * @author Reda Zerrad Date: 04.19.2012
 */
@Named("BulkWebService")
@Path("/scim/v1/Bulk")
public class BulkWebService extends BaseScimWebService {

	private static final Logger log = Logger.getLogger(BulkWebService.class);

	@Inject
	private IPersonService personService;

	@Inject
	private IGroupService groupService;

	@Inject
	private IPersonService persinService;

	@Inject
	private CopyUtils copyUtils;

	@Inject
	private ExternalScimService externalScimService;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response bulkOperation(@Context HttpServletRequest request, @HeaderParam("Authorization") String authorization, ScimBulkOperation operation) throws Exception {
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

					ScimGroup group = copyUtils.copy(oneRequest.getData(), null);
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
					ScimGroup group = copyUtils.copy(oneRequest.getData(), null);
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
		schemas.add(Constants.SCIM1_CORE_SCHEMA_ID);
		scimBulkResponse.setSchemas(schemas);
		scimBulkResponse.setOperations(listResponses);

		URI location = new URI("/Bulk/");
		return Response.ok(scimBulkResponse).location(location).build();

	}

	private boolean createUser(ScimPerson person) throws Exception {

		GluuCustomPerson gluuPerson = copyUtils.copy(person, null, false);
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
				Utils.groupMembersAdder(gluuPerson, gluuPerson.getDn());
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			gluuPerson = Utils.syncEmailForward(gluuPerson, false);

			// For custom script: create user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateUserMethods(gluuPerson);
			}

			personService.addPerson(gluuPerson);

			return true;

		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private boolean updateUser(String uid, ScimPerson person_update) throws Exception {
		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				return false;
			}

			gluuPerson = copyUtils.copy(person_update, gluuPerson, true);

			if (person_update.getGroups().size() > 0) {
				Utils.groupMembersAdder(gluuPerson, personService.getDnForPerson(uid));
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			gluuPerson = Utils.syncEmailForward(gluuPerson, false);

			// For custom script: update user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateUserMethods(gluuPerson);
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
		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);

			if (gluuPerson == null) {

				return false;

			} else {

				// For custom script: delete user
				if (externalScimService.isEnabled()) {
					externalScimService.executeScimDeleteUserMethods(gluuPerson);
				}

				if (gluuPerson.getMemberOf() != null) {

					if (gluuPerson.getMemberOf().size() > 0) {
						String dn = personService.getDnForPerson(uid);
						Utils.deleteUserFromGroup(gluuPerson, dn);
					}
				}

				personService.removePerson(gluuPerson);
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
		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = copyUtils.copy(group, null, false);
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
				Utils.personMembersAdder(gluuGroup, dn);
			}

			// For custom script: create group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateGroupMethods(gluuGroup);
			}

			groupService.addGroup(gluuGroup);

			return true;

		} catch (Exception ex) {
			log.error("Failed to add user", ex);
			return false;
		}
	}

	private boolean updateGroup(String id, ScimGroup group) throws Exception {
		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				return false;
			}

			GluuGroup newGluuGroup = copyUtils.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			// For custom script: update group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateGroupMethods(newGluuGroup);
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
		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);

			if (gluuGroup == null) {

				return false;

			} else {

				// For custom script: delete group
				if (externalScimService.isEnabled()) {
					externalScimService.executeScimDeleteGroupMethods(gluuGroup);
				}

				if (gluuGroup.getMembers() != null) {

					if (gluuGroup.getMembers().size() > 0) {
						String dn = groupService.getDnForGroup(id);
						Utils.deleteGroupFromPerson(gluuGroup, dn);
					}
				}

				groupService.removeGroup(gluuGroup);
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
