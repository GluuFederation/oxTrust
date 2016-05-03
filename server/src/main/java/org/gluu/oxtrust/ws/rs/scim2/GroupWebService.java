/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Authorization;

import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("scim2GroupEndpoint")
@Path("/scim/v2/Groups")
@Api(value = "/v2/Groups", description = "SCIM 2.0 Group Endpoint (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.4.1)",
		authorizations = {
				@Authorization(value = "Authorization", type = "oauth2")})
public class GroupWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IGroupService groupService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List Users",
			notes = "Returns a list of Groups (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.4)",
			response = ListResponse.class
	)
	public Response listGroups(
			@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder) throws Exception {

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

			List<GluuGroup> groupList = groupService.getAllGroupsList();
			ListResponse allGroupList = new ListResponse();
			if (groupList != null) {
				for (GluuGroup gluuGroup : groupList) {
					Group group = CopyUtils2.copy(gluuGroup, null);
					allGroupList.getResources().add(group);
				}
			}
			List<String> schema = new ArrayList<String>();
			schema.add(Constants.LIST_RESPONSE_SCHEMA_ID);
			allGroupList.setSchemas(schema);
			allGroupList.setTotalResults(allGroupList.getResources().size());

			URI location = new URI("/v2/Groups/");
			return Response.ok(allGroupList).location(location).build();
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Find Group by id",
			notes = "Returns a Group on the basis of provided id as path param (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.4.2.1)",
			response = Group.class
	)
	public Response getGroupById(
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

			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			Group group = CopyUtils2.copy(gluuGroup, null);

			URI location = new URI("/Groups/" + id);

			return Response.ok(group).location(location).build();
		} catch (EntryPersistenceException ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Create Group",
			notes = "Create Group (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.3)",
			response = Group.class
	)
	public Response createGroup(
			@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
			@ApiParam(value = "Group", required = true) Group group) throws Exception {

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
		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = CopyUtils2.copy(group, null, false);
		if (gluuGroup == null) {
			return getErrorResponse("Failed to create group", Response.Status.BAD_REQUEST.getStatusCode());
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

			log.info("group.getMembers().size() : " + group.getMembers().size());
			if (group.getMembers().size() > 0) {
				Utils.personMemebersAdder(gluuGroup, dn);
			}

			log.debug("adding new GluuGroup");
			groupService.addGroup(gluuGroup);
			Group newGroup = CopyUtils2.copy(gluuGroup, null);
			String uri = "/Groups/" + newGroup.getId();
			return Response.created(URI.create(uri)).entity(newGroup).build();
		} catch (Exception ex) {
			log.error("Failed to add user", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update Group",
			notes = "Update Group (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.3)",
			response = Group.class
	)
	public Response updateGroup(
			@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
			@PathParam("id") String id,
			@ApiParam(value = "Group", required = true) Group group) throws Exception {

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
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			GluuGroup newGluuGroup = CopyUtils2.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMemebersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			groupService.updateGroup(newGluuGroup);
			log.debug(" group updated ");
			Group newGroup = CopyUtils2.copy(newGluuGroup, null);

			URI location = new URI("/Groups/" + id);
			return Response.ok(newGroup).location(location).build();
		} catch (EntryPersistenceException ex) {
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Delete Group",
			notes = "Delete Group (https://tools.ietf.org/html/draft-ietf-scim-api-19#section-3.3)"
	)
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
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
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
			log.error("Exception: ", ex);
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
}
