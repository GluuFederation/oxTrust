/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.*;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseGroupSerializer;
import org.gluu.oxtrust.service.scim2.Scim2GroupService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.slf4j.Logger;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Named("scim2GroupEndpoint")
@Path("/scim/v2/Groups")
@Api(value = "/v2/Groups", description = "SCIM 2.0 Group Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)", authorizations = {@Authorization(value = "Authorization", type = "uma")})
public class GroupWebService extends BaseScimWebService {

	@Inject
	private Logger log;

	@Inject
	private IGroupService groupService;

    @Inject
    private Scim2GroupService scim2GroupService;

	@GET
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Search groups", notes = "Returns a list of groups (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
	public Response searchGroups(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

			if (count > getMaxCount()) {

				String detail = "Too many results (=" + count + ") would be returned; max is " + getMaxCount() + " only.";
				return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, detail);

			} else {

				log.info(" Searching groups from LDAP ");

				groupService = GroupService.instance();

				VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

				List<GluuGroup> groupList = search(groupService.getDnForGroup(null), GluuGroup.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);
				// List<GluuGroup> groupList = groupService.getAllGroupsList();

				ListResponse groupsListResponse = new ListResponse();

				List<String> schema = new ArrayList<String>();
				schema.add(Constants.LIST_RESPONSE_SCHEMA_ID);

				log.info(" setting schema");
				groupsListResponse.setSchemas(schema);

				// Set total
				groupsListResponse.setTotalResults(vlvResponse.getTotalResults());

				if (count > 0 && groupList != null && !groupList.isEmpty()) {

					// log.info(" LDAP group list is not empty ");

					for (GluuGroup gluuGroup : groupList) {

						Group group = CopyUtils2.copy(gluuGroup, null);

						log.info(" group to be added displayName : " + group.getDisplayName());

						groupsListResponse.getResources().add(group);

						log.info(" group added? : " + groupsListResponse.getResources().contains(group));
					}

					// Set the rest of results info
					groupsListResponse.setItemsPerPage(vlvResponse.getItemsPerPage());
					groupsListResponse.setStartIndex(vlvResponse.getStartIndex());
				}

				// Serialize to JSON
				String json = serializeToJson(groupsListResponse, attributesArray);

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Groups");

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

            log.error("Error in searchGroups", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("{id}")
	@GET
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Find group by id", notes = "Returns a group by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.2.1)", response = Group.class)
	public Response getGroupById(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

			groupService = GroupService.instance();

			String filterString = "id eq \"" + id + "\"";
			VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

			List<GluuGroup> groupList = search(groupService.getDnForGroup(null), GluuGroup.class, filterString, 1, 1, "id", SortOrder.ASCENDING.getValue(), vlvResponse, attributesArray);
			// GluuGroup gluuGroup = groupService.getGroupByInum(id);

			if (groupList == null || groupList.isEmpty() || vlvResponse.getTotalResults() == 0) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");
			} else {
				log.info(" Resource " + id + " found ");
			}

			GluuGroup gluuGroup = groupList.get(0);

			Group group = CopyUtils2.copy(gluuGroup, null);

			// Serialize to JSON
			String json = serializeToJson(group, attributesArray);

			URI location = new URI(group.getMeta().getLocation());

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

            log.error("Error in getGroupById", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

		} catch (Exception ex) {

            log.error("Error in getGroupById", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@POST
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Create group", notes = "Create group (https://tools.ietf.org/html/rfc7644#section-3.3)", response = Group.class)
	public Response createGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@ApiParam(value = "Group", required = true) Group group,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

			Group createdGroup = scim2GroupService.createGroup(group);

			// Serialize to JSON
			String json = serializeToJson(createdGroup, attributesArray);

			URI location = new URI(createdGroup.getMeta().getLocation());

			// Return HTTP response with status code 201 Created
			return Response.created(location).entity(json).build();

		} catch (DuplicateEntryException ex) {

			log.error("DuplicateEntryException", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to create group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("{id}")
	@PUT
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Update group", notes = "Update group (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = Group.class)
	public Response updateGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@ApiParam(value = "Group", required = true) Group group,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

			Group updatedGroup = scim2GroupService.updateGroup(id, group);

			// Serialize to JSON
			String json = serializeToJson(updatedGroup, attributesArray);

			URI location = new URI(updatedGroup.getMeta().getLocation());

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

            log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

		} catch (DuplicateEntryException ex) {

			log.error("DuplicateEntryException", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Delete group", notes = "Delete group (https://tools.ietf.org/html/rfc7644#section-3.6)")
	public Response deleteGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id) throws Exception {

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

            scim2GroupService.deleteGroup(id);

			return Response.noContent().build();

		} catch (EntryPersistenceException ex) {

            log.error("Failed to delete group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (Exception ex) {

            log.error("Failed to delete group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

    @Path("/.search")
    @POST
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Search group POST /.search", notes = "Returns a list of groups (https://tools.ietf.org/html/rfc7644#section-3.4.3)", response = ListResponse.class)
    public Response searchGroupsPost(
        @HeaderParam("Authorization") String authorization,
        @QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
        @ApiParam(value = "SearchRequest", required = true) SearchRequest searchRequest) throws Exception {

        try {

            log.info("IN GroupWebService.searchGroupsPost()...");

            // Authorization check is done in searchGroups()
            Response response = searchGroups(
                authorization,
                token,
                searchRequest.getFilter(),
                searchRequest.getStartIndex(),
                searchRequest.getCount(),
                searchRequest.getSortBy(),
                searchRequest.getSortOrder(),
                searchRequest.getAttributesArray()
            );

            URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Groups/.search");

            log.info("LEAVING GroupWebService.searchGroupsPost()...");

            return Response.fromResponse(response).location(location).build();

        } catch (EntryPersistenceException ex) {

            log.error("Error in searchGroupsPost", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource not found");

        } catch (Exception ex) {

            log.error("Error in searchGroupsPost", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("/Me")
    @GET
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "GET \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
    public Response meGet() {
        return getErrorResponse(501, "Not Implemented");
    }

    @Path("/Me")
    @POST
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "POST \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
    public Response mePost() {
        return getErrorResponse(501, "Not Implemented");
    }

	private String serializeToJson(Object object, String attributesArray) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
		SimpleModule customScimFilterModule = new SimpleModule("CustomScim2GroupFilterModule", new Version(1, 0, 0, ""));
		ListResponseGroupSerializer serializer = new ListResponseGroupSerializer();
		serializer.setAttributesArray(attributesArray);
		customScimFilterModule.addSerializer(Group.class, serializer);
		mapper.registerModule(customScimFilterModule);

		return mapper.writeValueAsString(object);
	}
}
