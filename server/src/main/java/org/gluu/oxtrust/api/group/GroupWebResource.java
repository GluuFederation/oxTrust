package org.gluu.oxtrust.api.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.GluuPersonApi;
import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.GROUPS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
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
	@ApiOperation(value = "Get groups ")
	public Response listGroups(@DefaultValue("0") @QueryParam(OxTrustApiConstants.SIZE) int size) {
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a group by inum")
	public Response getGroupByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Get group having group" + inum);
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
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search groups")
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size) {
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete a group")
	public Response deleteGroup(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
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
	@ApiOperation(value = "Update a group")
	public Response updateGroup(GluuGroupApi group) {
		String inum = group.getInum();
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
	@ApiOperation(value = "Add a group")
	public Response createGroup(GluuGroupApi group) {
		log("Adding group " + group.getDisplayName());
		try {
			Objects.requireNonNull(group, "Attempt to create null group");
			GluuGroup gluuGroup = copyAttributes(group);
			String inum = groupService.generateInumForNewGroup();
			gluuGroup.setDn(groupService.getDnForGroup(inum));
			gluuGroup.setInum(inum);
			groupService.addGroup(gluuGroup);
			return Response.ok(convert(Arrays.asList(groupService.getGroupByInum(inum))).get(0)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.GROUP_MEMBERS)
	@ApiOperation(value = "Get a group members")
	public Response getGroupMembers(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Get members of group " + inum);
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
	@ApiOperation(value = "Add group member")
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.GROUP_MEMBERS
			+ OxTrustApiConstants.MEMBER_INUM_PARAM_PATH)
	public Response addGroupMember(@PathParam(OxTrustApiConstants.INUM) @NotNull String groupInum,
			@PathParam(OxTrustApiConstants.MEMBER_INUM) @NotNull String memberInum) {
		log("Add member " + memberInum + " to group" + groupInum);
		try {
			Objects.requireNonNull(groupInum, "Group's inum should not be null");
			Objects.requireNonNull(memberInum, "Member's inum should not be null");
			GluuGroup group = groupService.getGroupByInum(groupInum);
			GluuCustomPerson person = personService.getPersonByInum(memberInum);
			if (group != null && person != null) {
				List<String> members = new ArrayList<String>(group.getMembers());
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
	@ApiOperation(value = "Remove a member from group")
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.GROUP_MEMBERS
			+ OxTrustApiConstants.MEMBER_INUM_PARAM_PATH)
	public Response removeGroupMember(@PathParam(OxTrustApiConstants.INUM) @NotNull String groupInum,
			@PathParam(OxTrustApiConstants.MEMBER_INUM) @NotNull String memberInum) {
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
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private GluuGroup copyAttributes(GluuGroupApi group) {
		GluuGroup gluuGroup = new GluuGroup();
		gluuGroup.setIname(group.getIname());
		gluuGroup.setDescription(group.getDescription());
		gluuGroup.setDisplayName(group.getDisplayName());
		gluuGroup.setOwner(group.getOwner());
		gluuGroup.setStatus(group.getStatus());
		gluuGroup.setOrganization(organizationService.getDnForOrganization());
		gluuGroup.setMembers(group.getMembers());
		return gluuGroup;
	}

	private GluuGroup updateValues(GluuGroup gluuGroup, GluuGroupApi group) {
		gluuGroup.setIname(group.getIname());
		gluuGroup.setDescription(group.getDescription());
		gluuGroup.setDisplayName(group.getDisplayName());
		gluuGroup.setOwner(group.getOwner());
		gluuGroup.setStatus(group.getStatus());
		gluuGroup.setOrganization(organizationService.getDnForOrganization());
		gluuGroup.setMembers(group.getMembers());
		return gluuGroup;
	}

	private List<GluuPersonApi> computeMembers(List<String> membersAsString) {
		List<GluuPersonApi> gluuCustomPersons = new ArrayList<GluuPersonApi>();
		if (!membersAsString.isEmpty()) {
			for (String memberAsString : membersAsString) {
				String uncompleteinum = memberAsString.split(",")[0];
				String inum = uncompleteinum.split("=")[1];
				gluuCustomPersons.add(new GluuPersonApi(personService.getPersonByInum(inum)));
			}
		}
		return gluuCustomPersons;
	}

	private List<GluuGroupApi> convert(List<GluuGroup> gluuGroups) {
		List<GluuGroupApi> result=new ArrayList<GluuGroupApi>();
		for (GluuGroup  p:gluuGroups) {
			result.add(new GluuGroupApi(p) );
		}
		return result;
	}

	private void log(String message) {
		logger.debug("#################Request: " + message);
	}

}
