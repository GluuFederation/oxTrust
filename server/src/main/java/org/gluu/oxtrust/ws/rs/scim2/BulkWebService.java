/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.BulkOperation;
import org.gluu.oxtrust.model.scim2.BulkRequest;
import org.gluu.oxtrust.model.scim2.BulkResponse;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
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
@Api(value = "/v2/Bulk", description = "SCIM 2.0 Bulk Endpoint (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.7)",
		authorizations = {
				@Authorization(value = "Authorization", type = "oauth2")})
public class BulkWebService extends BaseScimWebService {

	private static final Logger log = Logger.getLogger(BulkWebService.class);

	@In
	private IPersonService personService;

	@In
	private IGroupService groupService;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Bulk Operation",
			notes = "SCIM Bulk Operation (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.7)",
			response = BulkResponse.class
	)
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

		personService = PersonService.instance();

		GluuCustomPerson gluuPerson = CopyUtils2.copy(person, null, false);
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

	private boolean updateUser(String uid, User person_update) throws Exception {
		personService = PersonService.instance();

		try {
			GluuCustomPerson gluuPerson = personService.getPersonByInum(uid);
			if (gluuPerson == null) {
				return false;
			}

			gluuPerson = CopyUtils2.copy(person_update, gluuPerson, true);

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

	private boolean createGroup(Group group) throws Exception {
		groupService = GroupService.instance();

		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = CopyUtils2.copy(group, null, false);
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

	private boolean updateGroup(String id, Group group) throws Exception {

		groupService = GroupService.instance();

		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				return false;
			}
			GluuGroup newGluuGroup = CopyUtils2.copy(group, gluuGroup, true);

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
	
	private User convertUser(Object object){
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(object, User.class);
	}
	
	private Group convertGroup(Object object){
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(object, Group.class);
	}
	
	public static void main(String []args){
		LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
		map.put("userName", "Rahat");
		LinkedHashMap<Object, Object> name= new LinkedHashMap<Object, Object>();
		name.put("formatted", "formatted");
		map.put("name", name);
		/*User user = convert(map);
		System.out.println(user.getUserName());
		System.out.println(user.getName().getFormatted());*/
	}
	
	

}
