package org.gluu.oxtrust.ws.rs.scim2;

import org.gluu.oxtrust.model.scim2.PatchRequest;
import org.gluu.oxtrust.model.scim2.SearchRequest;
import org.gluu.oxtrust.model.scim2.group.GroupResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Created by jgomer on 2017-10-18.
 *
 * Shared (rest-easy) interface of the SCIM service.
 * Besides SCIM server code, this class is also used by SCIM java client, edit carefully.
 * The class org.gluu.oxtrust.service.scim2.interceptor.GroupServiceDecorator uses this interface as well
 */
public interface GroupService {

    @Path("/scim/v2/Groups")
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response createGroup(
            GroupResource group,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups/{id}")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response getGroupById(
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups/{id}")
    @PUT
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response updateGroup(
            GroupResource group,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups/{id}")
    @DELETE
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response deleteGroup(
            @PathParam("id") String id,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response searchGroups(
            @QueryParam(QUERY_PARAM_FILTER) String filter,
            @QueryParam(QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(QUERY_PARAM_SORT_ORDER) String sortOrder,
            @QueryParam(QUERY_PARAM_START_INDEX) Integer startIndex,
            @QueryParam(QUERY_PARAM_COUNT) Integer count,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups/.search")
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response searchGroupsPost(
            SearchRequest searchRequest,
            @HeaderParam("Authorization") String authorization);

    @Path("/scim/v2/Groups/{id}")
    @PATCH
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response patchGroup(
            PatchRequest request,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization);

}
