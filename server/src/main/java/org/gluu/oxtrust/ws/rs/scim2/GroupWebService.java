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
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseGroupSerializer;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("scim2GroupEndpoint")
@Path("/scim/v2/Groups")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/v2/Groups", description = "SCIM 2.0 Group Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)", authorizations = { @Authorization(value = "Authorization", type = "uma") })
public class GroupWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IGroupService groupService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List groups", notes = "Returns a list of groups (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
	public Response listGroups(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

		try {

			if (count > MAX_COUNT) {

				String detail = "Too many results (=" + count + ") would be returned; max is " + MAX_COUNT + " only.";
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

						log.info(" group to be added userid : " + group.getDisplayName());

						groupsListResponse.getResources().add(group);

						log.info(" group added? : " + groupsListResponse.getResources().contains(group));
					}

					// Set the rest of results info
					groupsListResponse.setItemsPerPage(vlvResponse.getItemsPerPage());
					groupsListResponse.setStartIndex(vlvResponse.getStartIndex());
				}

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Groups");

				// Serialize to JSON
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
				mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
				SimpleModule customScimFilterModule = new SimpleModule("CustomScimGroupFilterModule", new Version(1, 0, 0, ""));
				ListResponseGroupSerializer serializer = new ListResponseGroupSerializer();
				serializer.setAttributesArray(attributesArray);
				customScimFilterModule.addSerializer(Group.class, serializer);
				mapper.registerModule(customScimFilterModule);
				String json = mapper.writeValueAsString(groupsListResponse);

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

			ex.printStackTrace();
			String detail = "Unexpected processing error; please check the input parameters";
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, detail);
		}
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Find group by id", notes = "Returns a group by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.2.1)", response = Group.class)
	public Response getGroupById(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

		try {

			groupService = GroupService.instance();

			String filterString = "id eq \"" + id + "\"";
			VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

			List<GluuGroup> groupList = search(groupService.getDnForGroup(null), GluuGroup.class, filterString, 1, 1, "id", SortOrder.ASCENDING.getValue(), vlvResponse, attributesArray);
			// GluuGroup gluuGroup = groupService.getGroupByInum(id);

			if (groupList == null || groupList.isEmpty() || vlvResponse.getTotalResults() == 0) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");
			} else {
				log.info(" Resource " + id + " found ");
			}

			GluuGroup gluuGroup = groupList.get(0);

			Group group = CopyUtils2.copy(gluuGroup, null);

			URI location = new URI(group.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimGroupFilterModule", new Version(1, 0, 0, ""));
			ListResponseGroupSerializer serializer = new ListResponseGroupSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(Group.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(group);

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Create group", notes = "Create group (https://tools.ietf.org/html/rfc7644#section-3.3)", response = Group.class)
	public Response createGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@ApiParam(value = "Group", required = true) Group group,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

		try {

			log.debug(" copying gluuGroup ");
			GluuGroup gluuGroup = CopyUtils2.copy(group, null, false);
			if (gluuGroup == null) {
				return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create group");
			}

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

			log.info("group.getMembers().size() : " + group.getMembers().size());
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

			log.debug("adding new GluuGroup");
			groupService.addGroup(gluuGroup);

			Group createdGroup = CopyUtils2.copy(gluuGroup, null);

			URI location = new URI(createdGroup.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimGroupFilterModule", new Version(1, 0, 0, ""));
			ListResponseGroupSerializer serializer = new ListResponseGroupSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(Group.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(createdGroup);

			// Return HTTP response with status code 201 Created
			return Response.created(location).entity(json).build();

		} catch (DuplicateEntryException ex) {

			log.error("Failed to create group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to create group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update group", notes = "Update group (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = Group.class)
	public Response updateGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@ApiParam(value = "Group", required = true) Group group,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

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

		try {

			groupService = GroupService.instance();

			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {

				return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

			} else {

				// Validate if attempting to update displayName of a different id
				if (gluuGroup.getDisplayName() != null) {

					GluuGroup groupToFind = new GluuGroup();
					groupToFind.setDisplayName(group.getDisplayName());

					List<GluuGroup> foundGroups = groupService.findGroups(groupToFind, 2);
					if (foundGroups != null && foundGroups.size() > 0) {
						for (GluuGroup foundGroup : foundGroups) {
							if (foundGroup != null && !foundGroup.getInum().equalsIgnoreCase(gluuGroup.getInum())) {
								throw new DuplicateEntryException("Cannot update displayName of a different id: " + group.getDisplayName());
							}
						}
					}
				}
			}

			GluuGroup updatedGluuGroup = CopyUtils2.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(updatedGluuGroup, groupService.getDnForGroup(id));
			}

			log.info(" Setting meta: update group ");
			DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
			Date dateLastModified = DateTime.now().toDate();
			updatedGluuGroup.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
			if (updatedGluuGroup.getAttribute("oxTrustMetaLocation") == null || (("oxTrustMetaLocation") != null && updatedGluuGroup.getAttribute("oxTrustMetaLocation").isEmpty())) {
				String relativeLocation = "/scim/v2/Groups/" + id;
				updatedGluuGroup.setAttribute("oxTrustMetaLocation", relativeLocation);
			}

			groupService.updateGroup(updatedGluuGroup);

			log.debug(" group updated ");

			Group updatedGroup = CopyUtils2.copy(updatedGluuGroup, null);

			URI location = new URI(updatedGroup.getMeta().getLocation());

			// Serialize to JSON
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
			SimpleModule customScimFilterModule = new SimpleModule("CustomScimGroupFilterModule", new Version(1, 0, 0, ""));
			ListResponseGroupSerializer serializer = new ListResponseGroupSerializer();
			serializer.setAttributesArray(attributesArray);
			customScimFilterModule.addSerializer(Group.class, serializer);
			mapper.registerModule(customScimFilterModule);
			String json = mapper.writeValueAsString(updatedGroup);

			return Response.ok(json).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (DuplicateEntryException ex) {

			log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

		} catch (Exception ex) {

			log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}

	@Path("{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Delete group", notes = "Delete group (https://tools.ietf.org/html/rfc7644#section-3.6)")
	public Response deleteGroup(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id) throws Exception {

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

		try {

			groupService = GroupService.instance();

			log.info(" Checking if the group exists ");
			log.info(" id : " + id);
			GluuGroup group = groupService.getGroupByInum(id);
			if (group == null) {
				log.info(" the group is null ");
				return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");
			} else {
				log.info(" getting started to delete members from groups ");
				if (group.getMembers() != null) {
					if (group.getMembers().size() > 0) {
						log.info(" getting dn for group ");
						String dn = groupService.getDnForGroup(id);
						log.info(" DN : " + dn);
						Utils.deleteGroupFromPerson(group, dn);
					}
				}
				log.info(" removing the group ");
				groupService.removeGroup(group);
			}

			return Response.ok().build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected processing error, please check the input parameters");
		}
	}
}
