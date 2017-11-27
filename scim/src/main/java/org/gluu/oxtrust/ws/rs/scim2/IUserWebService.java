package org.gluu.oxtrust.ws.rs.scim2;

import org.gluu.oxtrust.model.scim2.patch.PatchRequest;
import org.gluu.oxtrust.model.scim2.SearchRequest;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Created by jgomer on 2017-09-01.
 *
 * Shared (rest-easy) interface of the SCIM service.
 * Besides SCIM server code, this class is also used by SCIM java client, edit carefully.
 * The class org.gluu.oxtrust.service.scim2.interceptor.UserServiceDecorator uses this interface as well
 */
public interface IUserWebService {

    @Path("/scim/v2/Users")
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response createUser(
            UserResource user,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList);

    @Path("/scim/v2/Users/{id}")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response getUserById(
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList);

    @Path("/scim/v2/Users/{id}")
    @PUT
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response updateUser(
            UserResource user,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList);

    @Path("/scim/v2/Users/{id}")
    @DELETE
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response deleteUser(@PathParam("id") String id);

    @Path("/scim/v2/Users")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response searchUsers(
            @QueryParam(QUERY_PARAM_FILTER) String filter,
            @QueryParam(QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(QUERY_PARAM_SORT_ORDER) String sortOrder,
            @QueryParam(QUERY_PARAM_START_INDEX) Integer startIndex,
            @QueryParam(QUERY_PARAM_COUNT) Integer count,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList);

    @Path("/scim/v2/Users/.search")
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response searchUsersPost(SearchRequest searchRequest);

    @Path("/scim/v2/Users/{id}")
    @PATCH
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response patchUser(
                PatchRequest request,
                @PathParam("id") String id,
                @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
                @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList);

}