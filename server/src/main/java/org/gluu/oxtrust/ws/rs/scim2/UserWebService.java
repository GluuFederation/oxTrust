/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.ScimPatchUser;
import org.gluu.oxtrust.model.scim2.SearchRequest;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseUserSerializer;
import org.gluu.oxtrust.service.scim2.Scim2UserService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.ws.rs.scim2.validators.UserValidator;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Authorization;

/**
 * scim2UserEndpoint Implementation
 *
 * @author Rahat Ali Date: 05.08.2015
 */
@Named("scim2UserEndpoint")
@Path("/scim/v2/Users")
@Api(value = "/v2/Users", description = "SCIM 2.0 User Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)", authorizations = {@Authorization(value = "Authorization", type = "uma")})
public class UserWebService extends BaseScimWebService {

    @Inject
    private Logger log;

    @Inject
    private JsonConfigurationService jsonConfigurationService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private IPersonService personService;

    @Inject
    private Scim2UserService scim2UserService;

    @Inject
    private CopyUtils2 copyUtils2;

    @GET
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Search users", notes = "Returns a list of users (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
    public Response searchUsers(
            @HeaderParam("Authorization") String authorization,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT)  Integer count,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }

        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {
            count = (count == null) ? getMaxCount() : count;

            if (count > getMaxCount()) {
                String detail = "Too many results (=" + count + ") would be returned; max is " + getMaxCount() + " only.";
                return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, detail);
            } else {
                log.info(" Searching users from LDAP ");

                VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

                List<GluuCustomPerson> gluuCustomPersons = search(personService.getDnForPerson(null), GluuCustomPerson.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);
                // List<GluuCustomPerson> personList = personService.findAllPersons(null);

                ListResponse usersListResponse = new ListResponse();

                List<String> schema = new ArrayList<String>();
                schema.add(Constants.LIST_RESPONSE_SCHEMA_ID);

                log.info(" setting schema");
                usersListResponse.setSchemas(schema);

                // Set total
                usersListResponse.setTotalResults(vlvResponse.getTotalResults());

                if (count > 0 && gluuCustomPersons != null && !gluuCustomPersons.isEmpty()) {

                    // log.info(" LDAP person list is not empty ");

                    for (GluuCustomPerson gluuPerson : gluuCustomPersons) {

                        User user = copyUtils2.copy(gluuPerson, null);

                        log.info(" user to be added id : " + user.getUserName());

                        usersListResponse.getResources().add(user);

                        log.info(" user added? : " + usersListResponse.getResources().contains(user));
                    }

                    // Set the rest of results info
                    usersListResponse.setItemsPerPage(vlvResponse.getItemsPerPage());
                    usersListResponse.setStartIndex(vlvResponse.getStartIndex());
                }

                // Serialize to JSON
                String json = serializeToJson(usersListResponse, attributesArray);

                URI location = new URI(appConfiguration.getBaseEndpoint() + "/scim/v2/Users");

                return Response.ok(json).location(location).build();
            }

        } catch (Exception ex) {

            log.error("Error in searchUsers", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("{id}")
    @GET
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Find user by id", notes = "Returns a user by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.1)", response = User.class)
    public Response getUserById(
            @HeaderParam("Authorization") String authorization,
            @PathParam("id") String id,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }
        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {
            String filterString = "id eq \"" + id + "\"";
            VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

            List<GluuCustomPerson> personList = search(personService.getDnForPerson(null), GluuCustomPerson.class, filterString, 1, 1, "id", SortOrder.ASCENDING.getValue(), vlvResponse, attributesArray);
            // GluuCustomPerson gluuPerson = personService.getPersonByInum(id);

            if (personList == null || personList.isEmpty() || vlvResponse.getTotalResults() == 0) {
                // sets HTTP status code 404 Not Found
                return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");
            } else {
                log.info(" Resource " + id + " found ");
            }

            GluuCustomPerson gluuPerson = personList.get(0);

            User user = copyUtils2.copy(gluuPerson, null);

            // Serialize to JSON
            String json = serializeToJson(user, attributesArray);

            URI location = new URI(user.getMeta().getLocation());

            return Response.ok(json).location(location).build();

        } catch (EntryPersistenceException ex) {

            log.error("Error in getUserById", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

        } catch (Exception ex) {

            log.error("Error in getUserById", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @POST
    @Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Create user", notes = "Create user (https://tools.ietf.org/html/rfc7644#section-3.3)", response = User.class)
    public Response createUser(
            @HeaderParam("Authorization") String authorization,
            @ApiParam(value = "User", required = true) User user,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }
        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {
            if (UserValidator.validate(user)) {
                User createdUser = scim2UserService.createUser(user);
                // Serialize to JSON
                String json = serializeToJson(createdUser, attributesArray);
                URI location = new URI(createdUser.getMeta().getLocation());
                // Return HTTP response with status code 201 Created
                return Response.created(location).entity(json).build();
            }
            else{
                return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE,"User object did not pass validation of one or more attributes");
            }
        }
        catch (DuplicateEntryException ex) {
            log.error("DuplicateEntryException", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());
        }
        catch (PersonRequiredFieldsException ex) {
            log.error("PersonRequiredFieldsException: ", ex);
            return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, ex.getMessage());
        }
        catch (Exception ex) {
            log.error("Failed to create user " + ex.getMessage(), ex);
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("{id}")
    @PUT
    @Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Update user", notes = "Update user (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = User.class)
    public Response updateUser(
            @HeaderParam("Authorization") String authorization,
            @PathParam("id") String id,
            @ApiParam(value = "User", required = true) User user,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }
        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {
            if (UserValidator.validate(user)) {
                User updatedUser = scim2UserService.updateUser(id, user);
                // Serialize to JSON
                String json = serializeToJson(updatedUser, attributesArray);
                URI location = new URI(updatedUser.getMeta().getLocation());
                return Response.ok(json).location(location).build();
            }
            else{
                return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE,"User object did not pass validation of one or more attributes");
            }
        }
        catch (EntryPersistenceException ex) {
            log.error("Failed to update user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

        }
        catch (DuplicateEntryException ex) {
            log.error("DuplicateEntryException", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());

        }
        catch (Exception ex) {
            log.error("Failed to update user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("{id}")
    @DELETE
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Delete User", notes = "Delete User (https://tools.ietf.org/html/rfc7644#section-3.6)")
    public Response deleteUser(
            @HeaderParam("Authorization") String authorization,
            @PathParam("id") String id) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }
        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {

            scim2UserService.deleteUser(id);

            return Response.noContent().build();

        } catch (EntryPersistenceException ex) {

            log.error("Failed to delete user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");

        } catch (Exception ex) {

            log.error("Failed to delete user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("/.search")
    @POST
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "Search users POST /.search", notes = "Returns a list of users (https://tools.ietf.org/html/rfc7644#section-3.4.3)", response = ListResponse.class)
    public Response searchUsersPost(
            @HeaderParam("Authorization") String authorization,
            @ApiParam(value = "SearchRequest", required = true) SearchRequest searchRequest) throws Exception {

        try {

            log.info("IN UserWebService.searchUsersPost()...");

            // Authorization check is done in searchUsers()
            Response response = searchUsers(
                    authorization,
                    searchRequest.getFilter(),
                    searchRequest.getStartIndex(),
                    searchRequest.getCount(),
                    searchRequest.getSortBy(),
                    searchRequest.getSortOrder(),
                    searchRequest.getAttributesArray()
            );

            URI location = new URI(appConfiguration.getBaseEndpoint() + "/scim/v2/Users/.search");

            log.info("LEAVING UserWebService.searchUsersPost()...");

            return Response.fromResponse(response).location(location).build();

        } catch (EntryPersistenceException ex) {

            log.error("Error in searchUsersPost", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource not found");

        } catch (Exception ex) {

            log.error("Error in searchUsersPost", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    @Path("/Me")
    @GET
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "GET \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
    public Response meGet() {
        return getErrorResponse(501, "Not Implemented");
    }

    @Path("/Me")
    @POST
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "POST \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
    public Response mePost() {
        return getErrorResponse(501, "Not Implemented");
    }

    private String serializeToJson(Object object, String attributesArray) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        SimpleModule customScimFilterModule = new SimpleModule("CustomScim2UserFilterModule", new Version(1, 0, 0, ""));
        ListResponseUserSerializer serializer = new ListResponseUserSerializer();
        serializer.setAttributesArray(attributesArray);
        customScimFilterModule.addSerializer(User.class, serializer);
        mapper.registerModule(customScimFilterModule);

        return mapper.writeValueAsString(object);
    }

    //  PATCH WEBSERVICES
    @Path("/patch/{id}")
    @PUT
    @Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    @ApiOperation(value = "patch user", notes = "Update user (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = User.class)
    public Response patchUser(
            @HeaderParam("Authorization") String authorization,
            @PathParam("id") String id,
            @ApiParam(value = "User", required = true) ScimPatchUser user,
            @QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

        Response authorizationResponse;
        if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
            log.info(" ##### SCIM Test Mode is ACTIVE");
            authorizationResponse = processTestModeAuthorization(authorization);
        } else {
            authorizationResponse = processAuthorization(authorization);
        }
        if (authorizationResponse != null) {
            return authorizationResponse;
        }

        try {
            if (UserValidator.validate(user)) {
                User updatedUser = scim2UserService.patchUser(id, user);
                // Serialize to JSON
                String json = serializeToJson(updatedUser, attributesArray);
                URI location = new URI(updatedUser.getMeta().getLocation());
                return Response.ok(json).location(location).build();
            }
            else{
                return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE,"User object did not pass validation of one or more attributes");
            }
        }
        catch (EntryPersistenceException ex) {
            log.error("Failed to update user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");
        }
        catch (DuplicateEntryException ex) {
            log.error("DuplicateEntryException", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, ex.getMessage());
        }
        catch (Exception ex) {
            log.error("Failed to update user", ex);
            ex.printStackTrace();
            return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

}