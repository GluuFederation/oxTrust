/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.context.J2EContext;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Authorization;

/**
 * SCIM Bulk Endpoint Implementation
 * 
 * @author Rahat ALi Date: 05.08.2015
 */
@Name("scim2BulkEndpoint")
@Path("/scim/v2/Bulk")
@Api(value = "/v2/Bulk", description = "SCIM 2.0 Bulk Endpoint (https://tools.ietf.org/html/rfc7644#section-3.7)", authorizations = {@Authorization(value = "Authorization", type = "uma")})
public class BulkWebService extends BaseScimWebService {

	private static final Logger log = Logger.getLogger(BulkWebService.class);

	@In
	private IPersonService personService;

	@In
	private IGroupService groupService;

	@In
	private ExternalScimService externalScimService;

	@POST
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Bulk Operation",	notes = "Bulk operation (https://tools.ietf.org/html/rfc7644#section-3.7)", response = BulkResponse.class)
	public Response bulkOperation(
		@Context HttpServletRequest request,
		@Context HttpServletResponse response,
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@ApiParam(value = "BulkRequest", required = true) BulkRequest bulkRequest) throws Exception {

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

		personService = PersonService.instance();
		groupService = GroupService.instance();

		J2EContext context = new J2EContext(request, response);
		int removePathLength = "/Bulk".length();
		String domain = context.getFullRequestURL();
		if (domain.endsWith("/")) {
			removePathLength++; 
		}
		domain = domain.substring(0, domain.length() - removePathLength);

		log.info(" getting list of BulkRequest ");
		List<BulkOperation> bulkOperations = bulkRequest.getOperations();

		BulkResponse bulkResponse = new BulkResponse();

		for (BulkOperation operation : bulkOperations) {
			log.info(" checking operations ");

			if (operation.getPath().contains("Users")) {
				operation = processUserOperation(operation, domain);

			} else if (operation.getPath().contains("Groups")) {
				operation = processGroupOperation(operation, domain);
				
			}
			bulkResponse.getOperations().add(operation);
		}

		URI location = new URI("/Bulk/");
		return Response.ok(bulkResponse).location(location).build();

	}

	private BulkOperation processGroupOperation(BulkOperation operation, String domain) throws Exception {
			
			Group group = convertGroup(operation.getData());
			
			if (operation.getMethod().equalsIgnoreCase("POST")) {
					log.info(" method is post ");
					boolean status = createGroup(group);
					if (status) {
						GluuGroup gluuGroup = groupService.getGroupByDisplayName(group.getDisplayName());
						String iD = gluuGroup.getInum();
						String location = (new StringBuilder()).append(domain).append("/Groups/").append(iD).toString();
						operation.setLocation(location);

						log.info(" POST status is true ");
						operation.setStatus("200");
						operation.setResponse(group);						

					} else if (!status) {
						log.info(" POST status is false ");
						operation.setStatus("400");
					}

				}else if (operation.getMethod().equalsIgnoreCase("PUT")) {
					log.info(" Status is PUT ");
					String path = operation.getPath();
					String groupiD = getId(path);
					boolean status = updateGroup(groupiD, group);
					if (status) {
						String location = (new StringBuilder()).append(domain).append("/Groups/").append(groupiD).toString();
						operation.setLocation(location);

						log.info(" PUT status is true ");
						operation.setStatus("200");
						operation.setResponse(group);						

					} else if (!status) {
						log.info(" PUT status is false ");
						operation.setStatus("400");
					}
				} else if (operation.getMethod().equalsIgnoreCase("DELETE")) {
					log.info(" Operation is DELETE ");

					String path = operation.getPath();
					String groupiD = getId(path);
					boolean status = deleteGroup(groupiD);

					if (status) {
						String location = (new StringBuilder()).append(domain).append("/Groups/").append(groupiD).toString();
						operation.setLocation(location);

						log.info(" DELETE operation is true ");
						operation.setStatus("200");
					} else if (!status) {
						log.info(" DELETE operation is False ");
						operation.setStatus("400");
					}
				}
		 
		return operation;
	}

	private BulkOperation processUserOperation(BulkOperation operation, String domain) throws Exception {		 
		User user = convertUser(operation.getData());		
		log.info("operations is for Users");		
		log.info(" method : " + operation.getMethod());
		if (operation.getMethod().equalsIgnoreCase("POST")) {
			log.info(" method is post ");
			if(createUser(user)){
				GluuCustomPerson gluuPerson = personService.getPersonByUid(user.getUserName());
				String inum = gluuPerson.getInum();
				String location = (new StringBuilder()).append(domain).append("/Users/").append(inum).toString();
				operation.setLocation(location);

				operation.setStatus("200");
				operation.setResponse(user);
			}
			else
				operation.setStatus("400");

		} else if (operation.getMethod().equalsIgnoreCase("PUT")) {
			log.info(" Status is PUT ");
			String personiD = getId(operation.getPath());
			log.info("Inum :  " + personiD);
			boolean status = updateUser(personiD, user);
			if (status) {
				String location = (new StringBuilder()).append(domain).append("/Users/").append(personiD).toString();
				operation.setLocation(location);

				log.info(" PUT status is true ");				
				operation.setStatus("200");
				operation.setResponse(user);
			} else
				operation.setStatus("400");
		} else if (operation.getMethod().equalsIgnoreCase("DELETE")) {
			log.info(" Operation is DELETE ");
			String path = operation.getPath();
			String personiD = getId(path);
			boolean status = deleteUser(personiD);
			if (status) {
				String location = (new StringBuilder()).append(domain).append("/Users/").append(personiD).toString();
				operation.setLocation(location);

				log.info(" DELETE operation is true ");
				operation.setStatus("200");

			} else if (!status) {
				log.info(" DELETE operation is False ");
				operation.setStatus("400");
			}
		} 
		return operation;
	}

	private boolean createUser(User person) throws Exception {

		log.info(" >>>>> IN createUser()... ");

		GluuCustomPerson gluuPerson = CopyUtils2.copy(person, null, false);
		if (gluuPerson == null) {
			return false;
		}

		try {

			personService = PersonService.instance();

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

			// For custom script: create user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateUserMethods(gluuPerson);
			}

			personService.addPerson(gluuPerson);

			log.info(" >>>>> LEAVING createUser()... ");

			return true;

		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private boolean updateUser(String id, User person_update) throws Exception {

		log.info(" >>>>> IN updateUser()... ");

		try {

			personService = PersonService.instance();

			GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
			if (gluuPerson == null) {
				return false;
			}

			gluuPerson = CopyUtils2.copy(person_update, gluuPerson, true);

			if (person_update.getGroups().size() > 0) {
				Utils.groupMembersAdder(gluuPerson, personService.getDnForPerson(id));
			}

			log.info(" Setting meta: update user ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateLastModified = DateTime.now().toDate();
			gluuPerson.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
			if (gluuPerson.getAttribute("oxTrustMetaLocation") == null || (("oxTrustMetaLocation") != null && gluuPerson.getAttribute("oxTrustMetaLocation").isEmpty())) {
				String relativeLocation = "/scim/v2/Users/" + id;
				gluuPerson.setAttribute("oxTrustMetaLocation", relativeLocation);
			}

			// Sync email, forward ("oxTrustEmail" -> "mail")
			gluuPerson = Utils.syncEmailForward(gluuPerson, true);

			// For custom script: update user
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateUserMethods(gluuPerson);
			}

			personService.updatePerson(gluuPerson);

			log.info(" >>>>> LEAVING createUser()... ");

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

		log.info(" >>>>> IN deleteUser()... ");

		try {

			personService = PersonService.instance();

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

			log.info(" >>>>> LEAVING deleteUser()... ");

			return true;

		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return false;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

	private boolean createGroup(Group group) throws Exception {

		log.info(" >>>>> IN createGroup()... ");

		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = CopyUtils2.copy(group, null, false);
		if (gluuGroup == null) {
			return false;
		}

		try {

			groupService = GroupService.instance();

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

			// As per spec, the SP must be the one to assign the meta attributes
			log.info(" Setting meta: create group ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateCreated = DateTime.now().toDate();
			String relativeLocation = "/scim/v2/Groups/" + inum;
			gluuGroup.setAttribute("oxTrustMetaCreated", dateTimeFormatter.print(dateCreated.getTime()));
			gluuGroup.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateCreated.getTime()));
			gluuGroup.setAttribute("oxTrustMetaLocation", relativeLocation);

			// For custom script: create group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateGroupMethods(gluuGroup);
			}

			groupService.addGroup(gluuGroup);

			log.info(" >>>>> LEAVING createGroup()... ");

			return true;

		} catch (Exception ex) {
			log.error("Failed to add user", ex);
			return false;
		}
	}

	private boolean updateGroup(String id, Group group) throws Exception {

		log.info(" >>>>> IN updateGroup()... ");

		try {

			groupService = GroupService.instance();

			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				return false;
			}

			GluuGroup newGluuGroup = CopyUtils2.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			log.info(" Setting meta: update group ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateLastModified = DateTime.now().toDate();
			newGluuGroup.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
			if (newGluuGroup.getAttribute("oxTrustMetaLocation") == null || (("oxTrustMetaLocation") != null && newGluuGroup.getAttribute("oxTrustMetaLocation").isEmpty())) {
				String relativeLocation = "/scim/v2/Groups/" + id;
				newGluuGroup.setAttribute("oxTrustMetaLocation", relativeLocation);
			}

			// For custom script: update group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateGroupMethods(newGluuGroup);
			}

			groupService.updateGroup(newGluuGroup);
			log.debug(" group updated ");

			log.info(" >>>>> LEAVING updateGroup()... ");

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

		log.info(" >>>>> IN deleteGroup()... ");

		try {

			groupService = GroupService.instance();

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

			log.info(" >>>>> LEAVING deleteGroup()... ");

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
	
	private User convertUser(Object object){
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(object, User.class);
	}
	
	private Group convertGroup(Object object){
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(object, Group.class);
	}

    /*
	public static void main(String []args){
		LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
		map.put("userName", "Rahat");
		LinkedHashMap<Object, Object> name= new LinkedHashMap<Object, Object>();
		name.put("formatted", "formatted");
		map.put("name", name);
		*/
        /*User user = convert(map);
		System.out.println(user.getUserName());
		System.out.println(user.getName().getFormatted());*//*
	}
    */
}
