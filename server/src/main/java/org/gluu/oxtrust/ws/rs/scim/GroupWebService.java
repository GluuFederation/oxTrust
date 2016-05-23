/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

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

import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuGroupList;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/**
 * @author Reda Zerrad Date: 04.13.2012
 */
@Name("GroupWebService")
@Path("/scim/v1/Groups")
public class GroupWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@In
	private IGroupService groupService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response listGroups(@HeaderParam("Authorization") String authorization,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
			@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder) throws Exception {

		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			List<GluuGroup> groupList = groupService.getAllGroupsList();
			GluuGroupList allGroupList = new GluuGroupList();
			if (groupList != null) {
				for (GluuGroup gluuGroup : groupList) {
					ScimGroup group = CopyUtils.copy(gluuGroup, null);
					allGroupList.getResources().add(group);
				}
			}
			List<String> schema = new ArrayList<String>();
			schema.add("urn:scim:schemas:core:1.0");
			allGroupList.setSchemas(schema);
			List<ScimGroup> resources = allGroupList.getResources();
			allGroupList.setTotalResults((long) resources.size());

			URI location = new URI("/Groups/");
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
	public Response getGroupById(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {

		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}

			ScimGroup group = CopyUtils.copy(gluuGroup, null);

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
	public Response createGroup(@HeaderParam("Authorization") String authorization, ScimGroup group) throws Exception {
		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}
		// Return HTTP response with status code 201 Created

		log.debug(" copying gluuGroup ");
		GluuGroup gluuGroup = CopyUtils.copy(group, null, false);
		if (gluuGroup == null) {
			return getErrorResponse("Failed to create group", Response.Status.BAD_REQUEST.getStatusCode());
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

			log.info("group.getMembers().size() : " + group.getMembers().size());
			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(gluuGroup, dn);
			}

			log.debug("adding new GluuGroup");
			groupService.addGroup(gluuGroup);
			ScimGroup newGroup = CopyUtils.copy(gluuGroup, null);
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
	public Response updateGroup(@HeaderParam("Authorization") String authorization, @PathParam("id") String id, ScimGroup group) throws Exception {
		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {
				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());
			}
			GluuGroup newGluuGroup = CopyUtils.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			groupService.updateGroup(newGluuGroup);
			log.debug(" group updated ");
			ScimGroup newGroup = CopyUtils.copy(newGluuGroup, null);

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
	public Response deleteGroup(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {
		groupService = GroupService.instance();

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {
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
