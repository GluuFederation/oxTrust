package org.gluu.oxtrust.ws.rs.scim2;

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
public interface UserService {

    @Path("/scim/v2/Users")
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response createUser(
            UserResource user,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization) throws Exception;

    @Path("/scim/v2/Users/{id}")
    @PUT
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    Response updateUser(
            UserResource user,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @PathParam("id") String id,
            @HeaderParam("Authorization") String authorization) throws Exception;

    /*
    Response patchUser(String authorization, String id, ScimPatchUser user,final String attributesArray) throws Exception;
    Response deleteUser(String authorization, String id) throws Exception;
    Response getUserById(String authorization, String id, final String attributesArray) throws Exception;
    Response searchUsers(String authorization, final String filterString, final int startIndex, Integer count, final String sortBy, final String sortOrder, final String attributesArray) throws Exception;
    */
}