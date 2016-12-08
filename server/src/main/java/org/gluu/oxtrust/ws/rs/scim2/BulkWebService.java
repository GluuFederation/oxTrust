/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseGroupSerializer;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseUserSerializer;
import org.gluu.oxtrust.service.scim2.Scim2GroupService;
import org.gluu.oxtrust.service.scim2.Scim2UserService;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserDeserializer;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
// import org.xdi.context.J2EContext;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Authorization;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_BULK_OPERATIONS;
import static org.gluu.oxtrust.model.scim2.Constants.MAX_BULK_PAYLOAD_SIZE;
import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

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
        @HeaderParam("Content-Length") int contentLength,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@ApiParam(value = "BulkRequest", required = true) BulkRequest bulkRequest) throws Exception {

		Response authorizationResponse;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

        try {

            /*
            J2EContext context = new J2EContext(request, response);
            int removePathLength = "/Bulk".length();
            String domain = context.getFullRequestURL();
            if (domain.endsWith("/")) {
                removePathLength++;
            }
            domain = domain.substring(0, domain.length() - removePathLength);
            */

            log.info("##### Operation count = " + bulkRequest.getOperations().size());
            log.info("##### Content-Length = " + contentLength);

            if (bulkRequest.getOperations().size() > MAX_BULK_OPERATIONS || contentLength > MAX_BULK_PAYLOAD_SIZE) {

                StringBuilder message = new StringBuilder("The size of the bulk operation exceeds the ");

                if (bulkRequest.getOperations().size() > MAX_BULK_OPERATIONS && contentLength <= MAX_BULK_PAYLOAD_SIZE) {

                    message.append("maxOperations (").append(MAX_BULK_OPERATIONS).append(")");

                } else if (bulkRequest.getOperations().size() <= MAX_BULK_OPERATIONS && contentLength > MAX_BULK_PAYLOAD_SIZE) {

                    message.append("maxPayloadSize (").append(MAX_BULK_PAYLOAD_SIZE).append(")");

                } else if (bulkRequest.getOperations().size() > MAX_BULK_OPERATIONS && contentLength > MAX_BULK_PAYLOAD_SIZE) {

                    message.append("maxOperations (").append(MAX_BULK_OPERATIONS).append(") ");
                    message.append("and ");
                    message.append("maxPayloadSize (").append(MAX_BULK_PAYLOAD_SIZE).append(")");
                }

                log.info("Payload Too Large: " + message.toString());
                return getErrorResponse(413, message.toString());
            }

            int failOnErrorsLimit = (bulkRequest.getFailOnErrors() != null) ? bulkRequest.getFailOnErrors() : 0;
            int failOnErrorsCount = 0;

            List<BulkOperation> bulkOperations = bulkRequest.getOperations();

            BulkResponse bulkResponse = new BulkResponse();
            Map<String, String> processedBulkIds = new LinkedHashMap<String, String>();

            operationsLoop:
            for (BulkOperation operation : bulkOperations) {

                log.info(" Checking operations... ");

                if (operation.getPath().startsWith("/Users")) {

                    // operation = processUserOperation(operation, domain);
                    operation = processUserOperation(operation, processedBulkIds);

                } else if (operation.getPath().startsWith("/Groups")) {

                    // operation = processGroupOperation(operation, domain);
                    operation = processGroupOperation(operation, processedBulkIds);
                }

                bulkResponse.getOperations().add(operation);

                // Error handling
                String okCode = String.valueOf(Response.Status.OK.getStatusCode());
                String createdCode = String.valueOf(Response.Status.CREATED.getStatusCode());
                if (!operation.getStatus().equalsIgnoreCase(okCode) && !operation.getStatus().equalsIgnoreCase(createdCode)) {
                    failOnErrorsCount++;
                    if ((failOnErrorsLimit > 0) && (failOnErrorsCount >= failOnErrorsLimit)) {
                        break operationsLoop;
                    }
                }
            }

            URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Bulk");

            // Serialize to JSON

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            SimpleModule customBulkOperationsModule = new SimpleModule("CustomBulkOperationsModule", new Version(1, 0, 0, ""));

            // Custom serializers for both User and Group
            ListResponseUserSerializer userSerializer = new ListResponseUserSerializer();
            ListResponseGroupSerializer groupSerializer = new ListResponseGroupSerializer();
            customBulkOperationsModule.addSerializer(User.class, userSerializer);
            customBulkOperationsModule.addSerializer(Group.class, groupSerializer);

            mapper.registerModule(customBulkOperationsModule);

            String json = mapper.writeValueAsString(bulkResponse);

            return Response.ok(json).location(location).build();

        } catch (Exception ex) {

            log.error("Error in processBulkOperations", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
	}

	private BulkOperation processUserOperation(BulkOperation operation, Map<String, String> processedBulkIds) throws Exception {

        log.info(" Operation is for User ");

        // Intercept bulkId
        User user = null;
        if (operation.getData() != null) {  // Required in a request when "method" is "POST", "PUT", or "PATCH".

            String serializedData = serialize(operation.getData());

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                serializedData = serializedData.replaceAll(key, entry.getValue());
            }

            user = deserializeToUser(serializedData);
        }

		String userRootEndpoint = applicationConfiguration.getBaseEndpoint() + "/scim/v2/Users/";

		if (operation.getMethod().equalsIgnoreCase(HttpMethod.POST)) {

			log.info(" Method is POST ");

			try {

                user = scim2UserService.createUser(user);

                personService = PersonService.instance();
				GluuCustomPerson gluuPerson = personService.getPersonByUid(user.getUserName());
				String inum = gluuPerson.getInum();

				// String location = (new StringBuilder()).append(domain).append("/Users/").append(inum).toString();
				String location = userRootEndpoint + inum;
				operation.setLocation(location);

				operation.setStatus(String.valueOf(Response.Status.CREATED.getStatusCode()));
				operation.setResponse(user);

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                processedBulkIds.put(operation.getBulkId(), user.getId());

            } catch (DuplicateEntryException ex) {

                log.error("DuplicateEntryException", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.CONFLICT.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage()));

            } catch (PersonRequiredFieldsException ex) {

                log.error("PersonRequiredFieldsException: ", ex);
                operation.setStatus(String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, ex.getMessage()));

            } catch (Exception ex) {

                log.error("Failed to create user", ex);
                ex.printStackTrace();
				operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}

		} else if (operation.getMethod().equalsIgnoreCase(HttpMethod.PUT)) {

			log.info(" Method is PUT ");

            String path = operation.getPath();
            String id = getId(path);

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                if (id.equalsIgnoreCase(key)) {
                    id = id.replaceAll(key, entry.getValue());
                    break;
                }
            }

			try {

                user = scim2UserService.updateUser(id, user);

				// String location = (new StringBuilder()).append(domain).append("/Users/").append(personiD).toString();
				String location = userRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus(String.valueOf(Response.Status.OK.getStatusCode()));
				operation.setResponse(user);

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                if (operation.getBulkId() != null) {
                    processedBulkIds.put(operation.getBulkId(), user.getId());
                }

            } catch (EntryPersistenceException ex) {

                log.error("Failed to update user", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found"));

            } catch (DuplicateEntryException ex) {

                log.error("DuplicateEntryException", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.CONFLICT.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage()));

            } catch (Exception ex) {

                log.error("Failed to update user", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}

		} else if (operation.getMethod().equalsIgnoreCase(HttpMethod.DELETE)) {

			log.info(" Method is DELETE ");

			String path = operation.getPath();
			String id = getId(path);

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                if (id.equalsIgnoreCase(key)) {
                    id = id.replaceAll(key, entry.getValue());
                    break;
                }
            }

			try {

                scim2UserService.deleteUser(id);

                // Location may be omitted on DELETE
				operation.setStatus(String.valueOf(Response.Status.OK.getStatusCode()));
                operation.setResponse("User " + id + " deleted");

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                if (operation.getBulkId() != null) {
                    processedBulkIds.put(operation.getBulkId(), id);
                }

            } catch (EntryPersistenceException ex) {

                log.error("Failed to delete user", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.NOT_FOUND, null, "Resource " + id + " not found"));

            } catch (Exception ex) {

                log.error("Failed to delete user", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}
		}

		return operation;
	}

	private BulkOperation processGroupOperation(BulkOperation operation, Map<String, String> processedBulkIds) throws Exception {

        log.info(" Operation is for Group ");

        // Intercept bulkId
        Group group = null;
        if (operation.getData() != null) {  // Required in a request when "method" is "POST", "PUT", or "PATCH".

            String serializedData = serialize(operation.getData());

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                serializedData = serializedData.replaceAll(key, entry.getValue());
            }

            group = deserializeToGroup(serializedData);
        }

		String groupRootEndpoint = applicationConfiguration.getBaseEndpoint() + "/scim/v2/Groups/";

		if (operation.getMethod().equalsIgnoreCase(HttpMethod.POST)) {

			log.info(" Method is POST ");

			try {

                group = scim2GroupService.createGroup(group);

                groupService = GroupService.instance();
				GluuGroup gluuGroup = groupService.getGroupByDisplayName(group.getDisplayName());
				String id = gluuGroup.getInum();

				// String location = (new StringBuilder()).append(domain).append("/Groups/").append(id).toString();
				String location = groupRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus(String.valueOf(Response.Status.CREATED.getStatusCode()));
				operation.setResponse(group);

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                processedBulkIds.put(operation.getBulkId(), group.getId());

            } catch (DuplicateEntryException ex) {

                log.error("DuplicateEntryException", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.CONFLICT.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage()));

            } catch (Exception ex) {

                log.error("Failed to create group", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}

		} else if (operation.getMethod().equalsIgnoreCase(HttpMethod.PUT)) {

			log.info(" Method is PUT ");

			String path = operation.getPath();
			String id = getId(path);

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                if (id.equalsIgnoreCase(key)) {
                    id = id.replaceAll(key, entry.getValue());
                    break;
                }
            }

			try {

                group = scim2GroupService.updateGroup(id, group);

				// String location = (new StringBuilder()).append(domain).append("/Groups/").append(groupiD).toString();
				String location = groupRootEndpoint + id;
				operation.setLocation(location);

				operation.setStatus(String.valueOf(Response.Status.OK.getStatusCode()));
				operation.setResponse(group);

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                if (operation.getBulkId() != null) {
                    processedBulkIds.put(operation.getBulkId(), group.getId());
                }

            } catch (EntryPersistenceException ex) {

                log.error("Failed to update group", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found"));

            } catch (DuplicateEntryException ex) {

                log.error("DuplicateEntryException", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.CONFLICT.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage()));

            } catch (Exception ex) {

                log.error("Failed to update group", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}

		} else if (operation.getMethod().equalsIgnoreCase(HttpMethod.DELETE)) {

			log.info(" Method is DELETE ");

			String path = operation.getPath();
			String id = getId(path);

            for (Map.Entry<String, String> entry : processedBulkIds.entrySet()) {
                String key = "bulkId:" + entry.getKey();
                if (id.equalsIgnoreCase(key)) {
                    id = id.replaceAll(key, entry.getValue());
                    break;
                }
            }

			try {

                scim2GroupService.deleteGroup(id);

                // Location may be omitted on DELETE
				operation.setStatus(String.valueOf(Response.Status.OK.getStatusCode()));
                operation.setResponse("Group " + id + " deleted");

                // Set aside successfully-processed bulkId
                // bulkId is only required in POST
                if (operation.getBulkId() != null) {
                    processedBulkIds.put(operation.getBulkId(), id);
                }

            } catch (EntryPersistenceException ex) {

                log.error("Failed to delete group", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.NOT_FOUND, null, "Resource " + id + " not found"));

            } catch (Exception ex) {

                log.error("Failed to delete group", ex);
                ex.printStackTrace();
                operation.setStatus(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
                operation.setResponse(createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, null, INTERNAL_SERVER_ERROR_MESSAGE));
			}
		}

		return operation;
	}

	private String getId(String path) {

		String str[] = path.split("/");
		return str[2];
	}

    private String serialize(Object object) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

        return mapper.writeValueAsString(object);
    }

	private User deserializeToUser(String dataString) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleModule simpleModule = new SimpleModule("DeserializeToUserModule", new Version(1, 0, 0, ""));
        simpleModule.addDeserializer(User.class, new UserDeserializer());
        mapper.registerModule(simpleModule);

		return mapper.readValue(dataString, User.class);
	}
	
	private Group deserializeToGroup(String dataString) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

		return mapper.readValue(dataString, Group.class);
	}

    private ErrorResponse createErrorResponse(Response.Status status, ErrorScimType scimType, String detail) {

        ErrorResponse errorResponse = new ErrorResponse();

        List<String> schemas = new ArrayList<String>();
        schemas.add(Constants.ERROR_RESPONSE_URI);
        errorResponse.setSchemas(schemas);

        errorResponse.setStatus(String.valueOf(status.getStatusCode()));
        errorResponse.setScimType(scimType);
        errorResponse.setDetail(detail);

        return errorResponse;
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
