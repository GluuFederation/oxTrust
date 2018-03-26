package org.gluu.oxtrust.api.group;

import java.util.List;
import java.util.Objects;

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

import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.GROUPS)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private GroupService groupService;

	public GroupWebResource() {
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@ApiOperation(value = "Get all groups")
	public String listGroups(@Context HttpServletResponse response) {
		try {
			List<GluuGroup> groups = groupService.getAllGroups();
			response.setStatus(HttpServletResponse.SC_OK);
			return mapper.writeValueAsString(groups);
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when retrieving groups", response);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@ApiOperation(value = "Get size groups ")
	public String listGroup(@QueryParam(OxTrustApiConstants.SIZE) int sizeLimit,
			@Context HttpServletResponse response) {
		try {
			List<GluuGroup> groups = groupService.getAllGroups(sizeLimit);
			response.setStatus(HttpServletResponse.SC_OK);
			return mapper.writeValueAsString(groups);
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when retrieving groups", response);
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a group by inum")
	public String getGroupByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			if (group != null) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return mapper.writeValueAsString(group);
		} catch (Exception e) {
			return handleError(logger, e, "Exception when retrieving group " + inum, response);
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a group by display name")
	public String getGroupByDisplayName(@PathParam(OxTrustApiConstants.DISPLAY_NAME) @NotNull String displayName,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(displayName, "inum should not be null");
			GluuGroup group = groupService.getGroupByDisplayName(displayName);
			if (group != null) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return mapper.writeValueAsString(group);
		} catch (Exception e) {
			return handleError(logger, e, "Exception when retrieving group " + displayName, response);
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Add a group")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 500, message = "Server error") })
	public String createGroup(GluuGroup group, @Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(group, "Attempt to create null group");
			String inum = groupService.generateInumForNewGroup();
			group.setInum(inum);
			groupService.addGroup(group);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return inum;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during group insertion", response);
		}
	}

	@PUT
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Update a group")
	public String updateGroup(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, GluuGroup group,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Objects.requireNonNull(group, "Attempt to update null group");
			GluuGroup existingGroup = groupService.getGroupByInum(inum);
			if (existingGroup != null) {
				group.setInum(existingGroup.getInum());
				groupService.updateGroup(group);
				response.setStatus(HttpServletResponse.SC_OK);
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return OxTrustConstants.RESULT_FAILURE;
			}
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during group update", response);
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Search groups")
	public String searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size, @Context HttpServletResponse response) {
		try {
			List<GluuGroup> groups = groupService.searchGroups(pattern, size);
			response.setStatus(HttpServletResponse.SC_OK);
			return mapper.writeValueAsString(groups);
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during groups search", response);
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Delete a group")
	public String deleteGroup(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			GluuGroup group = groupService.getGroupByInum(inum);
			if (group != null) {
				groupService.removeGroup(group);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when deleting group " + inum, response);
		}
	}

}
