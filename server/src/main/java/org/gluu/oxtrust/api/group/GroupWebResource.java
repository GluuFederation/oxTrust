package org.gluu.oxtrust.api.group;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.GROUPS)
public class GroupWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private GroupService groupService;

	@Inject
	private AppConfiguration appConfiguration;

	public GroupWebResource() {
	}

	@GET
	@QueryParam(value = "size")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get n groups ")
	public Response listGroups(@DefaultValue("0") @QueryParam(OxTrustApiConstants.SIZE) int size,
			@Context HttpServletResponse response) {
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a group by inum")
	public Response getGroupByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Search groups")
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size, @Context HttpServletResponse response) {
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete a group")
	public Response deleteGroup(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		log("Delete group having inum " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			if (group != null) {
				groupService.removeGroup(group);
				return Response.ok(group).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update a group")
	public Response updateGroup(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, GluuGroupApi group,
			@Context HttpServletResponse response) {
		log("Update group " + group.getDisplayName());
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Objects.requireNonNull(group, "Attempt to update null group");
			GluuGroup existingGroup = groupService.getGroupByInum(inum);
			if (existingGroup != null) {
				group.setInum(existingGroup.getInum());
				groupService.updateGroup(copyAttributes(group));
				return Response.ok(group).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add a group")
	public Response createGroup(GluuGroupApi group) {
		log("Adding group " + group.getDisplayName());
		try {
			Objects.requireNonNull(group, "Attempt to create null group");
			GluuGroup gluuGroup = copyAttributes(group);
			gluuGroup.setBaseDn(appConfiguration.getBaseDN());
			String inum = groupService.generateInumForNewGroup();
			gluuGroup.setInum(inum);
			log(gluuGroup.toString());
			groupService.addGroup(gluuGroup);
			return Response.ok(groupService.getGroupByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private GluuGroup copyAttributes(GluuGroupApi group) {
		GluuGroup gluuGroup = new GluuGroup();
		gluuGroup.setInum(group.getInum());
		gluuGroup.setIname(group.getIname());
		gluuGroup.setDescription(group.getDescription());
		gluuGroup.setDisplayName(group.getDisplayName());
		gluuGroup.setMembers(group.getMembers());
		gluuGroup.setOwner(group.getOwner());
		gluuGroup.setStatus(group.getStatus());
		return gluuGroup;
	}

	private List<GluuGroupApi> convert(List<GluuGroup> gluuGroups) {
		return gluuGroups.stream().map(g -> new GluuGroupApi(g)).collect(Collectors.toList());
	}

	private void log(String message) {
		logger.debug("#################Request: " + message);
	}

}
