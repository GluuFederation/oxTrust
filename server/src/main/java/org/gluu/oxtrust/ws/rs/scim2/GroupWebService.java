/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.InvalidAttributeValueException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Authorization;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.patch.PatchOperation;
import org.gluu.oxtrust.model.scim2.patch.PatchRequest;
import org.gluu.oxtrust.service.scim2.Scim2GroupService;
import org.gluu.oxtrust.service.scim2.interceptor.Protected;
import org.gluu.oxtrust.service.scim2.interceptor.RefAdjusted;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;
import org.xdi.util.Pair;

import java.net.URI;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Implementation of /Groups endpoint. Methods here are intercepted and/or decorated. Class org.gluu.oxtrust.service.scim2.interceptor.GroupServiceDecorator
 * is used to apply pre-validations on data. Filter org.gluu.oxtrust.ws.rs.scim2.AuthorizationProcessingFilter
 * secures invocations
 *
 * @author Rahat Ali Date: 05.08.2015
 * Updated by jgomer on 2017-10-18
 */
@Named("scim2GroupEndpoint")
@Path("/scim/v2/Groups")
@Api(value = "/v2/Groups", description = "SCIM 2.0 Group Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)",
        authorizations = {@Authorization(value = "Authorization", type = "uma")})
public class GroupWebService extends BaseScimWebService implements GroupService {

    @Inject
    private UserWebService userWebService;

    @Inject
    private Scim2GroupService scim2GroupService;

    @Inject
    private IGroupService groupService;

    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Create group", notes = "Create group (https://tools.ietf.org/html/rfc7644#section-3.3)", response = GroupResource.class)
    public Response createGroup(
            @ApiParam(value = "Group", required = true) GroupResource group,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList) {

        Response response;
        try {
            GluuGroup gluuGroup=scim2GroupService.createGroup(group, endpointUrl, userWebService.getEndpointUrl());

            // For custom script: create group
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimCreateGroupMethods(gluuGroup);
            }

            String json=resourceSerializer.serialize(group, attrsList, excludedAttrsList);
            response=Response.created(new URI(group.getMeta().getLocation())).entity(json).build();
        }
        catch (Exception e){
            log.error("Failure at createGroup method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path("{id}")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Find group by id", notes = "Returns a group by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.2.1)",
            response = GroupResource.class)
    public Response getGroupById(
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList) {

        Response response;
        try {
            GroupResource group=new GroupResource();
            GluuGroup gluuGroup=groupService.getGroupByInum(id);  //gluuGroup is not null (check associated decorator method)
            scim2GroupService.transferAttributesToGroupResource(gluuGroup, group, endpointUrl, userWebService.getEndpointUrl());

            String json=resourceSerializer.serialize(group, attrsList, excludedAttrsList);
            response=Response.ok(new URI(group.getMeta().getLocation())).entity(json).build();
        }
        catch (Exception e){
            log.error("Failure at getGroupById method", e);
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
    @Protected @RefAdjusted
    @ApiOperation(value = "Update group", notes = "Update group (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = GroupResource.class)
    public Response updateGroup(
            @ApiParam(value = "Group", required = true) GroupResource group,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList) {

        Response response;
        try {
            Pair<GluuGroup, GroupResource> pair=scim2GroupService.updateGroup(id, group, endpointUrl, userWebService.getEndpointUrl());

            // For custom script: update group
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimUpdateGroupMethods(pair.getFirst());
            }

            GroupResource updatedResource=pair.getSecond();
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
            log.error("Failure at updateGroup method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path("{id}")
    @DELETE
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Delete group", notes = "Delete group (https://tools.ietf.org/html/rfc7644#section-3.6)")
    public Response deleteGroup(@PathParam("id") String id){

        Response response;
        try {
            GluuGroup group=groupService.getGroupByInum(id);  //group cannot be null (check associated decorator method)

            // For custom script: delete group. Execute before actual deletion
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimDeleteGroupMethods(group);
            }

            log.info("Removing group and updating user's entries");
            //TODO: check if removal updates user entries
            groupService.removeGroup(group);

            response=Response.noContent().build();
        }
        catch (Exception e){
            log.error("Failure at deleteGroup method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Search groups", notes = "Returns a list of groups (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
    public Response searchGroups(
            @QueryParam(QUERY_PARAM_FILTER) String filter,
            @QueryParam(QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(QUERY_PARAM_SORT_ORDER) String sortOrder,
            @QueryParam(QUERY_PARAM_START_INDEX) Integer startIndex,
            @QueryParam(QUERY_PARAM_COUNT) Integer count,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList) {

        Response response;
        try {
            VirtualListViewResponse vlv = new VirtualListViewResponse();
            List<BaseScimResource> resources = scim2GroupService.searchGroups(filter, sortBy, SortOrder.getByValue(sortOrder),
                    startIndex, count, vlv, endpointUrl, userWebService.getEndpointUrl(), getMaxCount());

            String json = getListResponseSerialized(vlv.getTotalResults(), startIndex, resources, attrsList, excludedAttrsList, count==0);
            response=Response.ok(json).location(new URI(endpointUrl)).build();
        }
        catch (SCIMException e){
            log.error(e.getMessage(), e);
            response=getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at searchGroups method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path(SEARCH_SUFFIX)
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Search group POST /.search", notes = "Returns a list of groups (https://tools.ietf.org/html/rfc7644#section-3.4.3)", response = ListResponse.class)
    public Response searchGroupsPost(@ApiParam(value = "SearchRequest", required = true) SearchRequest searchRequest){

        //Calling searchGroups here does not provoke that method's interceptor/decorator being called (only this one's)
        URI uri=null;
        Response response = searchGroups(searchRequest.getFilter(), searchRequest.getSortBy(), searchRequest.getSortOrder(),
                searchRequest.getStartIndex(), searchRequest.getCount(), searchRequest.getAttributes(), searchRequest.getExcludedAttributes());

        try {
            uri = new URI(endpointUrl + "/" + SEARCH_SUFFIX);
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return Response.fromResponse(response).location(uri).build();

    }

    @Path("{id}")
    @PATCH
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "PATCH operation", notes = "https://tools.ietf.org/html/rfc7644#section-3.5.2", response = GroupResource.class)
    public Response patchGroup(
            PatchRequest request,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList){

        Response response;
        try{
            String usersUrl=userWebService.getEndpointUrl();
            GroupResource group=new GroupResource();
            GluuGroup gluuGroup=groupService.getGroupByInum(id);  //group is not null (check associated decorator method)

            //Fill group instance with all info from gluuGroup
            scim2GroupService.transferAttributesToGroupResource(gluuGroup, group, endpointUrl, usersUrl);

            //Apply patches one by one in sequence
            for (PatchOperation po : request.getOperations())
                group=(GroupResource) applyPatchOperation(group, po);

            //Throws exception if final representation does not pass overall validation
            log.debug("patchGroup. Revising final resource representation still passes validations");
            executeDefaultValidation(group);

            //Update timestamp
            String now=ISODateTimeFormat.dateTime().withZoneUTC().print(System.currentTimeMillis());
            group.getMeta().setLastModified(now);

            //Replaces the information found in person with the contents of user
            scim2GroupService.replaceGroupInfo(gluuGroup, group, usersUrl);

            // For custom script: update group
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimUpdateGroupMethods(gluuGroup);
            }

            String json=resourceSerializer.serialize(group, attrsList, excludedAttrsList);
            response=Response.ok(new URI(group.getMeta().getLocation())).entity(json).build();
        }
        catch (InvalidAttributeValueException e){
            log.error(e.getMessage(), e);
            response=getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.MUTABILITY, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at patchGroup method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @PostConstruct
    public void setup(){
        //Do not use getClass() here... a typical weld issue...
        endpointUrl=appConfiguration.getBaseEndpoint() + GroupWebService.class.getAnnotation(Path.class).value();
    }

}
