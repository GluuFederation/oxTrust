/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.List;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
// import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.scim2.Scim2GroupService;
import org.gluu.oxtrust.service.scim2.Scim2UserService;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserDeserializer;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
// import org.xdi.context.J2EContext;

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
    private Scim2UserService scim2UserService;

    @In
    private Scim2GroupService scim2GroupService;

    @POST
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Bulk Operations", notes = "Bulk Operations (https://tools.ietf.org/html/rfc7644#section-3.7)", response = BulkResponse.class)
	public Response processBulkOperations(
		// @Context HttpServletRequest request,
		// @Context HttpServletResponse response,
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

		/*
		 * 1. Add failOnErrors processing
		 * 2. Add bulkId (="serialized") processing
		 * 3. Validate operation params (?)
		 */

		/*
		J2EContext context = new J2EContext(request, response);
		int removePathLength = "/Bulk".length();
		String domain = context.getFullRequestURL();
		if (domain.endsWith("/")) {
			removePathLength++; 
		}
		domain = domain.substring(0, domain.length() - removePathLength);
		*/

        Integer failOnErrorsCount = bulkRequest.getFailOnErrors();

		log.info(" getting list of BulkRequest ");
		List<BulkOperation> bulkOperations = bulkRequest.getOperations();

		BulkResponse bulkResponse = new BulkResponse();

		for (BulkOperation operation : bulkOperations) {

			log.info(" checking operations ");

			if (operation.getPath().startsWith("/Users")) {

				// operation = processUserOperation(operation, domain);
				operation = processUserOperation(operation);

			} else if (operation.getPath().startsWith("/Groups")) {

				// operation = processGroupOperation(operation, domain);
				operation = processGroupOperation(operation);
			}

			bulkResponse.getOperations().add(operation);
		}

		URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Bulk");

		return Response.ok(bulkResponse).location(location).build();
	}

	private BulkOperation processUserOperation(BulkOperation operation) throws Exception {

		User user = deserializeToUser(operation.getData());

		log.info("operations is for Users");		
		log.info(" method : " + operation.getMethod());

		String userRootEndpoint = applicationConfiguration.getBaseEndpoint() + "/scim/v2/Users/";

		if (operation.getMethod().equalsIgnoreCase("POST")) {

			log.info(" method is post ");

			try {

                user = scim2UserService.createUser(user);

                personService = PersonService.instance();
				GluuCustomPerson gluuPerson = personService.getPersonByUid(user.getUserName());
				String inum = gluuPerson.getInum();

				// String location = (new StringBuilder()).append(domain).append("/Users/").append(inum).toString();
				String location = userRootEndpoint + inum;
				operation.setLocation(location);

				operation.setStatus("200");
				operation.setResponse(user);

			} catch(Exception e) {
				operation.setStatus("400");
			}

		} else if (operation.getMethod().equalsIgnoreCase("PUT")) {

			log.info(" Status is PUT ");

			String id = getId(operation.getPath());
			log.info("Inum :  " + id);

			try {

                user = scim2UserService.updateUser(id, user);

				// String location = (new StringBuilder()).append(domain).append("/Users/").append(personiD).toString();
				String location = userRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus("200");
				operation.setResponse(user);

			} catch(Exception e) {
				operation.setStatus("400");
			}

		} else if (operation.getMethod().equalsIgnoreCase("DELETE")) {

			log.info(" Operation is DELETE ");

			String path = operation.getPath();
			String id = getId(path);

			try {

                scim2UserService.deleteUser(id);

				// String location = (new StringBuilder()).append(domain).append("/Users/").append(personiD).toString();
				String location = userRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus("200");

			} catch (Exception e) {
				operation.setStatus("400");
			}
		}

		return operation;
	}

	private BulkOperation processGroupOperation(BulkOperation operation) throws Exception {

		Group group = deserializeToGroup(operation.getData());

		String groupRootEndpoint = applicationConfiguration.getBaseEndpoint() + "/scim/v2/Groups/";

		if (operation.getMethod().equalsIgnoreCase("POST")) {

			log.info(" method is post ");

			try {

                group = scim2GroupService.createGroup(group);

                groupService = GroupService.instance();
				GluuGroup gluuGroup = groupService.getGroupByDisplayName(group.getDisplayName());
				String id = gluuGroup.getInum();

				// String location = (new StringBuilder()).append(domain).append("/Groups/").append(id).toString();
				String location = groupRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus("200");
				operation.setResponse(group);

			} catch (Exception e) {
				operation.setStatus("400");
			}

		} else if (operation.getMethod().equalsIgnoreCase("PUT")) {

			log.info(" Status is PUT ");

			String path = operation.getPath();
			String id = getId(path);

			try {

                group = scim2GroupService.updateGroup(id, group);

				// String location = (new StringBuilder()).append(domain).append("/Groups/").append(groupiD).toString();
				String location = groupRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus("200");
				operation.setResponse(group);

			} catch (Exception e) {
				operation.setStatus("400");
			}

		} else if (operation.getMethod().equalsIgnoreCase("DELETE")) {

			log.info(" Operation is DELETE ");

			String path = operation.getPath();
			String id = getId(path);

			try {

                scim2GroupService.deleteGroup(id);

				// String location = (new StringBuilder()).append(domain).append("/Groups/").append(groupiD).toString();
				String location = groupRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus("200");

			} catch (Exception e) {
				operation.setStatus("400");
			}
		}

		return operation;
	}

	private String getId(String path) {

		String str[] = path.split("/");
		return str[2];
	}
	
	private User deserializeToUser(Object object) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleModule simpleModule = new SimpleModule("DeserializeToUserModule", new Version(1, 0, 0, ""));
        simpleModule.addDeserializer(User.class, new UserDeserializer());
        mapper.registerModule(simpleModule);

		return mapper.convertValue(object, User.class);
	}
	
	private Group deserializeToGroup(Object object) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

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
