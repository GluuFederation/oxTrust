/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuGroupList;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.service.antlr.scimFilter.util.GluuGroupListSerializer;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.VirtualListViewResponse;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

/**
 * @author Reda Zerrad Date: 04.13.2012
 */
@Named("GroupWebService")
@Path("/scim/v1/Groups")
public class GroupWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@Inject
	private IGroupService groupService;

	@Inject
	private ExternalScimService externalScimService;

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@HeaderParam("Accept") @DefaultValue(MediaType.APPLICATION_JSON)
	public Response searchGroups(@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			if (count > getMaxCount()) {

				String detail = "Too many results (=" + count + ") would be returned; max is " + getMaxCount() + " only.";
				return getErrorResponse(detail, Response.Status.BAD_REQUEST.getStatusCode());

			} else {

				log.info(" Searching groups from LDAP ");

				groupService = GroupService.instance();

				VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

				List<GluuGroup> gluuGroups = search(groupService.getDnForGroup(null), GluuGroup.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);
				// List<GluuGroup> groupList = groupService.getAllGroupsList();

				GluuGroupList groupsList = new GluuGroupList();

				List<String> schema = new ArrayList<String>();
				schema.add(Constants.SCIM1_CORE_SCHEMA_ID);

				log.info(" setting schema");
				groupsList.setSchemas(schema);

				// Set total
				groupsList.setTotalResults(vlvResponse.getTotalResults());

				if (count > 0 && gluuGroups != null && !gluuGroups.isEmpty()) {

					// log.info(" LDAP group list is not empty ");

					for (GluuGroup gluuGroup : gluuGroups) {

						ScimGroup group = CopyUtils.copy(gluuGroup, null);

						log.info(" group to be added displayName : " + group.getDisplayName());

						groupsList.getResources().add(group);

						log.info(" group added? : " + groupsList.getResources().contains(group));
					}

					// Set the rest of results info
					groupsList.setItemsPerPage(vlvResponse.getItemsPerPage());
					groupsList.setStartIndex(vlvResponse.getStartIndex());
				}

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v1/Groups");

				// Serialize to JSON
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
				SimpleModule customScimFilterModule = new SimpleModule("CustomScim1GroupFilterModule", new Version(1, 0, 0, ""));
				GluuGroupListSerializer serializer = new GluuGroupListSerializer();
				serializer.setAttributesArray(attributesArray);
				customScimFilterModule.addSerializer(ScimGroup.class, serializer);
				mapper.registerModule(customScimFilterModule);
				String json = mapper.writeValueAsString(groupsList);

				return Response.ok(json).location(location).build();
			}

		} catch (Exception ex) {

			log.error("Error in searchGroups", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getGroupById(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
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

			ScimGroup group = CopyUtils.copy(gluuGroup, null);

			URI location = new URI("/Groups/" + id);

			return Response.ok(group).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createGroup(@HeaderParam("Authorization") String authorization, ScimGroup group) throws Exception {

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

			// For custom script: create group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimCreateGroupMethods(gluuGroup);
			}

			log.debug("adding new GluuGroup");
			groupService.addGroup(gluuGroup);

			ScimGroup newGroup = CopyUtils.copy(gluuGroup, null);

			String uri = "/Groups/" + newGroup.getId();

			return Response.created(URI.create(uri)).entity(newGroup).build();

		} catch (DuplicateEntryException ex) {

			log.error("Failed to create group", ex);
			ex.printStackTrace();
			return getErrorResponse(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());

		} catch (Exception ex) {

			log.error("Failed to create group", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateGroup(@HeaderParam("Authorization") String authorization, @PathParam("id") String id, ScimGroup group) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			groupService = GroupService.instance();

			GluuGroup gluuGroup = groupService.getGroupByInum(id);
			if (gluuGroup == null) {

				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

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

			GluuGroup newGluuGroup = CopyUtils.copy(group, gluuGroup, true);

			if (group.getMembers().size() > 0) {
				Utils.personMembersAdder(newGluuGroup, groupService.getDnForGroup(id));
			}

			// For custom script: update group
			if (externalScimService.isEnabled()) {
				externalScimService.executeScimUpdateGroupMethods(newGluuGroup);
			}

			groupService.updateGroup(newGluuGroup);
			log.debug(" group updated ");

			ScimGroup newGroup = CopyUtils.copy(newGluuGroup, null);

			URI location = new URI("/Groups/" + id);

			return Response.ok(newGroup).location(location).build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (DuplicateEntryException ex) {

			log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(ex.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());

		} catch (Exception ex) {

			log.error("Failed to update group", ex);
			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteGroup(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {

		Response authorizationResponse = processAuthorization(authorization);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			groupService = GroupService.instance();

			log.info(" Checking if the group exists ");
			log.info(" id : " + id);

			GluuGroup gluuGroup = groupService.getGroupByInum(id);

			if (gluuGroup == null) {

				log.info(" the group is null ");

				return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

			} else {

				// For custom script: delete group
				if (externalScimService.isEnabled()) {
					externalScimService.executeScimDeleteGroupMethods(gluuGroup);
				}

				log.info(" getting started to delete members from groups ");
				if (gluuGroup.getMembers() != null) {

					if (gluuGroup.getMembers().size() > 0) {

						log.info(" getting dn for group ");
						String dn = groupService.getDnForGroup(id);
						log.info(" DN : " + dn);

						Utils.deleteGroupFromPerson(gluuGroup, dn);
					}
				}

				log.info(" removing the group ");
				groupService.removeGroup(gluuGroup);
			}

			return Response.ok().build();

		} catch (EntryPersistenceException ex) {

			ex.printStackTrace();
			return getErrorResponse("Resource " + id + " not found", Response.Status.NOT_FOUND.getStatusCode());

		} catch (Exception ex) {

			ex.printStackTrace();
			return getErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
}
