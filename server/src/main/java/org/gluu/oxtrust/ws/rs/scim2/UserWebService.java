package org.gluu.oxtrust.ws.rs.scim2;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.service.scim2.serialization.ScimResourceSerializer;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.slf4j.Logger;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.service.scim2.Scim2UserService;
import org.gluu.oxtrust.service.scim2.interceptor.Protected;
import org.xdi.util.Pair;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.InvalidAttributeValueException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Implementation of /User endpoint. Methods here are intercepted and/or decorated. Class org.gluu.oxtrust.service.scim2.interceptor.UserServiceDecorator
 * is used to apply pre-validations on data. Interceptor org.gluu.oxtrust.service.scim2.interceptor.ServiceInterceptor
 * adds security to invocations
 *
 * @author Rahat Ali Date: 05.08.2015
 * Updated by jgomer on 2017-09-12.
 */
@Named
@Path("/scim/v2/Users")
public class UserWebService extends BaseScimWebService implements UserService {

    @Inject
    private IPersonService personService;

    @Inject
    private Scim2UserService scim2UserService;

    @Inject
    private ExternalScimService externalScimService;

    /**
     *
     */
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Create user", notes = "https://tools.ietf.org/html/rfc7644#section-3.3", response = UserResource.class)
    public Response createUser(
            @ApiParam(value = "User", required = true) UserResource user,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization) throws Exception{

        Response response;
        try {
            GluuCustomPerson person = scim2UserService.createUser(user, endpointUrl);

            // For custom script: create user
            if (externalScimService.isEnabled())
                externalScimService.executeScimCreateUserMethods(person);

            String json=resourceSerializer.serialize(user, attrsList, excludedAttrsList);
            response=Response.created(new URI(user.getMeta().getLocation())).entity(json).build();
        }
        catch (Exception e){
            log.error("Failure at createUser method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path("/scim/v2/Users/{id}")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Find user by id", notes = "Returns a user by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.1)"
            , response = UserResource.class)
    public Response getUserById(
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization) throws Exception {

        Response response;
        try {
            UserResource user=new UserResource();
            GluuCustomPerson person=personService.getPersonByInum(id);  //person cannot be null (check associated decorator method)
            scim2UserService.transferAttributesToUserResource(person, user);

            String json=resourceSerializer.serialize(user, attrsList, excludedAttrsList);
            response=Response.ok(new URI(user.getMeta().getLocation())).entity(json).build();
        }
        catch (Exception e){
            log.error("Failure at getUserById method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    /**
     * This implementation differs from spec in the following aspects:
     * - Passing a null value for an attribute, does not modify the attribute in the destination, however passing an
     * empty array for a multivalued attribute does clear the attribute. Thus, to clear single-valued attribute, PATCH
     * operation should be used
     */
    @Path("{id}")
    @PUT
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Update user", notes = "Update user (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = UserResource.class)
    public Response updateUser(
            @ApiParam(value = "User", required = true) UserResource user,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization) throws Exception {

        Response response;
        try {
            Pair<GluuCustomPerson, UserResource> pair=scim2UserService.updateUser(id, user);

            // For custom script: update user
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimUpdateUserMethods(pair.getFirst());
            }

            UserResource updatedResource=pair.getSecond();
            String json=resourceSerializer.serialize(updatedResource, attrsList, excludedAttrsList);
            response=Response.ok(new URI(updatedResource.getMeta().getLocation())).entity(json).build();
        }
        catch (NotFoundException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, e.getMessage());
        }
        catch (InvalidAttributeValueException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.CONFLICT, ErrorScimType.MUTABILITY, e.getMessage());
        }
        catch (DuplicateEntryException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at updateUser method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;
    }

    @Path("/scim/v2/Users/{id}")
    @DELETE
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Delete User", notes = "Delete User (https://tools.ietf.org/html/rfc7644#section-3.6)")
    public Response deleteUser(
            @PathParam("id") String id,
            @HeaderParam("Authorization") String authorization) throws Exception{

        Response response;
        try {
            GluuCustomPerson person=personService.getPersonByInum(id);  //person cannot be null (check associated decorator method)

            // For custom script: delete user. Execute before actual deletion
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimDeleteUserMethods(person);
            }

            scim2UserService.deleteUser(person);
            response=Response.noContent().build();
        }
        catch (Exception e){
            log.error("Failure at deleteUser method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @PostConstruct
    public void setup(){
        //Do not use getClass() here... a typical weld issue...
        endpointUrl=UserWebService.class.getAnnotation(Path.class).value();
    }

}
