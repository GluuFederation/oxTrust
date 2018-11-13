/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.group;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.ResponseHeader;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path(OxTrustApiConstants.BASE_API_URL + "/v1/group")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupWebService {

    @Inject
    private Logger logger;

    @Inject
    private IGroupService groupService;

    @GET
    @ApiOperation(value = "Get all the groups")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = GluuGroup.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response list() {
        try {
            List<GluuGroup> groupList = groupService.getAllGroups();
            return Response.ok(groupList).build();
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{inum}")
    @ApiOperation(value = "Find a group by inum")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = GluuGroup.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response read(@PathParam("inum") String inum) {
        try {
            GluuGroup gluuGroup = groupService.getGroupByInum(inum);
            return Response.ok(gluuGroup).build();
        } catch (Exception e) {
            logger.error("read() Exception", e);
            return Response.serverError().build();
        }
    }

    @POST
    @ApiOperation(value = "Create a new group")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, responseHeaders = {@ResponseHeader(name = "Location", description = "url of the created group")}, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response create(GluuGroup gluuGroup, @Context UriInfo uriInfo) {
        try {
            Group group = new Group(gluuGroup);
            group.save(groupService);

            return Response.created(uriInfo.getAbsolutePathBuilder()
                    .path(GroupWebService.class)
                    .path(String.format("/%s", group.id())).build()).build();
        } catch (Exception e) {
            logger.error("create() Exception", e);
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/{inum}")
    @ApiOperation(value = "Update a group by inum")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "Success"),
                    @ApiResponse(code = 404, message = "inum not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response update(@PathParam("inum") String inum, GluuGroup gluuGroup) {
        try {
            Group group = new Group(gluuGroup);
            group.update(inum, groupService);

            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("update() Exception", e);
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{inum}")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "Success"),
                    @ApiResponse(code = 404, message = "inum not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response delete(@PathParam("inum") String inum) {
        try {
            GluuGroup group = groupService.getGroupByInum(inum);
            if (group == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            groupService.removeGroup(group);

            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return Response.serverError().build();
        }
    }


}
