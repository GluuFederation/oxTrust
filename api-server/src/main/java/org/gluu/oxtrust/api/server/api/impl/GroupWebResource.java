package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxtrust.api.server.model.GluuGroupApi;
import org.gluu.oxtrust.api.server.model.GluuPersonApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.GroupService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Path(ApiConstants.BASE_API_URL + ApiConstants.GROUPS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GroupWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private GroupService groupService;
	@Inject
	private PersonService personService;
	@Inject
	private OrganizationService organizationService;

	public GroupWebResource() {
	}
	
	@GET
	@Operation(summary = "Get groups", description = "Get groups")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuGroupApi[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_READ })
	public Response listGroups(@DefaultValue("0") @QueryParam(ApiConstants.SIZE) int size) {
		log("Get groups");
		try {
			if (size <= 0) {
				return Response.ok(convert(groupService.getAllGroups())).build();
			} else {
				return Response.ok(convert(groupService.getAllGroups(size))).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get group by inum", description = "Get a group by inum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuGroupApi.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_READ })
	public Response getGroupByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log("Get group having group" + inum);
		inum = inum.equalsIgnoreCase("") ? null : inum;
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			if (group != null) {
				return Response.ok(convert(Arrays.asList(group)).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search groups", description = "Search groups")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuGroupApi[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_READ })
	public Response searchGroups(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(ApiConstants.SIZE) int size) {
		log("Search groups with pattern= " + pattern + " and size " + size);
		try {
			List<GluuGroup> groups = groupService.searchGroups(pattern, size);
			return Response.ok(convert(groups)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Delete group", description = "Delete a group")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response deleteGroup(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log("Delete group having inum " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			if (group != null) {
				groupService.removeGroup(group);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update group", description = "Update a group")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuGroupApi.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response updateGroup(GluuGroupApi group) {
		String inum = group.getInum();
		inum = inum.equalsIgnoreCase("") ? null : inum;
		log("Update group " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Objects.requireNonNull(group, "Attempt to update null group");
			GluuGroup existingGroup = groupService.getGroupByInum(inum);
			if (existingGroup != null) {
				group.setInum(existingGroup.getInum());
				GluuGroup groupToUpdate = updateValues(existingGroup, group);
				groupToUpdate.setDn(groupService.getDnForGroup(inum));
				groupService.updateGroup(groupToUpdate);
				return Response.ok(convert(Arrays.asList(groupService.getGroupByInum(inum))).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add group", description = "Add a group")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = GluuGroupApi.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response createGroup(GluuGroupApi group) {
		log("Adding group " + group.getDisplayName());
		try {
			Objects.requireNonNull(group, "Attempt to create null group");
			GluuGroup gluuGroup = copyAttributes(group);
			String inum = gluuGroup.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = groupService.generateInumForNewGroup();
			}
			gluuGroup.setDn(groupService.getDnForGroup(inum));
			gluuGroup.setInum(inum);
			groupService.addGroup(gluuGroup);
			return Response.status(Response.Status.CREATED)
					.entity(convert(Arrays.asList(groupService.getGroupByInum(inum))).get(0)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.GROUP_MEMBERS)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuPersonApi[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Operation(summary = "Get group members", description = "Get a group members")
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response getGroupMembers(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log("Get members of group " + inum);
		inum = inum.equalsIgnoreCase("") ? null : inum;
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			List<String> members = new ArrayList<String>();
			if (group != null) {
				GluuGroupApi gluuGroupApi = convert(Arrays.asList(group)).get(0);
				members = gluuGroupApi.getMembers();
				return Response.ok(computeMembers(members)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add group member", description = "Add group member")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuGroupApi[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.GROUP_MEMBERS + ApiConstants.MEMBER_INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response addGroupMember(@PathParam(ApiConstants.INUM) @NotNull String groupInum,
			@PathParam(ApiConstants.MEMBER_INUM) @NotNull String memberInum) {
		log("Add member " + memberInum + " to group" + groupInum);
		try {
			Objects.requireNonNull(groupInum, "Group's inum should not be null");
			Objects.requireNonNull(memberInum, "Member's inum should not be null");
			GluuGroup group = groupService.getGroupByInum(groupInum);
			GluuCustomPerson person = personService.getPersonByInum(memberInum);
			if (group != null && person != null) {
				List<String> members = new ArrayList<String>();
				if (group.getMembers() != null) {
					members = group.getMembers();
				}
				members.add(personService.getDnForPerson(person.getInum()));
				group.setMembers(members);
				groupService.updateGroup(group);
				return Response.ok(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Operation(summary = "Remove group member", description = "Remove a member from group")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.GROUP_MEMBERS + ApiConstants.MEMBER_INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response removeGroupMember(@PathParam(ApiConstants.INUM) @NotNull String groupInum,
			@PathParam(ApiConstants.MEMBER_INUM) @NotNull String memberInum) {
		log("Remove member " + memberInum + " from group" + groupInum);
		try {
			Objects.requireNonNull(groupInum, "Group's inum should not be null");
			Objects.requireNonNull(memberInum, "Member's inum should not be null");
			GluuGroup group = groupService.getGroupByInum(groupInum);
			GluuCustomPerson person = personService.getPersonByInum(memberInum);
			if (group != null && person != null) {
				List<String> members = new ArrayList<String>(group.getMembers());
				members.remove(personService.getDnForPerson(person.getInum()));
				group.setMembers(members);
				groupService.updateGroup(group);
				return Response.ok(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	// What this operation does is unclear. We will comment it out and re-evaluate
	// @DELETE
	// @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	// public Response deleteGroups() {
	// return Response.status(Response.Status.UNAUTHORIZED).build();
	// }

	@DELETE
	@Operation(summary = "Delete group member", description = "Delete group member")
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.GROUP_MEMBERS)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_GROUP_WRITE })
	public Response deleteGroupMembers(@PathParam(ApiConstants.INUM) @NotNull String groupInum) {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	private GluuGroup copyAttributes(GluuGroupApi group) {
		GluuGroup gluuGroup = new GluuGroup();
		gluuGroup.setDescription(group.getDescription());
		gluuGroup.setDisplayName(group.getDisplayName());
		gluuGroup.setOwner(group.getOwner());
		gluuGroup.setStatus(group.getStatus());
		gluuGroup.setOrganization(organizationService.getDnForOrganization());
		gluuGroup.setMembers(group.getMembers());
		gluuGroup.setInum(group.getInum());
		return gluuGroup;
	}

	private GluuGroup updateValues(GluuGroup gluuGroup, GluuGroupApi group) {
		gluuGroup.setDescription(group.getDescription());
		gluuGroup.setDisplayName(group.getDisplayName());
		gluuGroup.setOwner(group.getOwner());
		gluuGroup.setStatus(group.getStatus());
		gluuGroup.setOrganization(organizationService.getDnForOrganization());
		gluuGroup.setMembers(group.getMembers());
		return gluuGroup;
	}

	private List<GluuPersonApi> computeMembers(List<String> membersDn) {
		List<GluuPersonApi> gluuCustomPersons = new ArrayList<GluuPersonApi>();
		if (membersDn != null && !membersDn.isEmpty()) {
			membersDn.stream().forEach(e -> {
				gluuCustomPersons.add(new GluuPersonApi(personService.getPersonByDn(e)));
			});
		}
		return gluuCustomPersons;
	}

	private List<GluuGroupApi> convert(List<GluuGroup> gluuGroups) {
		List<GluuGroupApi> result = new ArrayList<GluuGroupApi>();
		gluuGroups.stream().forEach(e -> {
			result.add(new GluuGroupApi(e));
		});
		return result;
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

}
