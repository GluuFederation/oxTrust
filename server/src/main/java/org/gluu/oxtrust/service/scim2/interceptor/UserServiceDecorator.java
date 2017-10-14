package org.gluu.oxtrust.service.scim2.interceptor;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;
import org.gluu.oxtrust.ws.rs.scim2.UserService;
import org.gluu.oxtrust.model.scim2.util.ResourceValidator;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.SEARCH_REQUEST_SCHEMA_ID;

/**
 * Aims at decorating SCIM service methods. Currently applies validations via ResourceValidator class or other custom
 * validation logic
 *
 * Created by jgomer on 2017-09-01.
 */
@Priority(Interceptor.Priority.APPLICATION)
@Decorator
public class UserServiceDecorator extends BaseScimWebService implements UserService {

    @Inject
    private Logger log;

    @Inject @Delegate @Any
    UserService userService;

    @Inject
    private IPersonService personService;

    @Inject
    private ExtensionService extService;

    private void assignMetaInformation(BaseScimResource resource){

        //Generate some meta information (this replaces the info client passed in the request)
        long now=new Date().getTime();
        String val= ISODateTimeFormat.dateTime().withZoneUTC().print(now);

        Meta meta=new Meta();
        meta.setResourceType(BaseScimResource.getType(resource.getClass()));
        meta.setCreated(val);
        meta.setLastModified(val);
        //For version attritute: Service provider support for this attribute is optional and subject to the service provider's support for versioning
        //For location attribute: this will be set after current user creation at LDAP
        resource.setMeta(meta);

    }

    private void executeDefaultValidation(BaseScimResource resource) throws SCIMException{

        ResourceValidator rv=new ResourceValidator(resource, extService.getResourceExtensions(resource.getClass()));
        rv.validateRequiredAttributes();
        rv.validateSchemasAttribute();
        rv.validateValidableAttributes();
        //By section 7 of RFC 7643, we are not forced to constrain attribute values when they have a list of canonical values associated
        //rv.validateCanonicalizedAttributes();
        rv.validateExtendedAttributes();

    }

    private Response validateExistenceOfUser(String id){

        Response response=null;
        GluuCustomPerson person = StringUtils.isEmpty(id) ? null : personService.getPersonByInum(id);

        if (person==null) {
            log.info("Person with inum {} not found", id);
            response = getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");
        }
        return response;

    }

    public Response createUser(UserResource user, String attrsList, String excludedAttrsList, String authorization) {

        Response response;
        try {
            executeDefaultValidation(user);
            assignMetaInformation(user);
            //Proceed with actual implementation of createUser method
            response = userService.createUser(user, attrsList, excludedAttrsList, authorization);
        }
        catch (SCIMException e){
            log.error("Validation check at createUser returned: {}", e.getMessage());
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, e.getMessage());
        }
        return response;

    }

    public Response getUserById(String id, String attrsList, String excludedAttrsList, String authorization){

        Response response=validateExistenceOfUser(id);
        if (response==null)
            //Proceed with actual implementation of getUserById method
            response=userService.getUserById(id, attrsList, excludedAttrsList, authorization);

        return response;

    }

    public Response updateUser(UserResource user, String id, String attrsList, String excludedAttrsList, String authorization) {

        Response response;
        try{
            response=validateExistenceOfUser(id);
            if (response==null) {
                executeDefaultValidation(user);

                if (!user.getId().equals(id))
                    throw new SCIMException("Parameter id does not match with id attribute of User");

                //Proceed with actual implementation of updateUser method
                response = userService.updateUser(user, id, attrsList, excludedAttrsList, authorization);
            }
        }
        catch (SCIMException e){
            log.error("Validation check at updateUser returned: {}", e.getMessage());
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, e.getMessage());
        }
        return response;

    }

    public Response deleteUser(String id, String authorization){

        Response response=validateExistenceOfUser(id);
        if (response==null)
            //Proceed with actual implementation of deleteUser method
            response=userService.deleteUser(id, authorization);

        return response;

    }

    public Response searchUsers(String filter, String sortBy, String sortOrder, Integer startIndex, Integer count,
                                String attrsList, String excludedAttrsList, String authorization){

        SearchRequest searchReq=new SearchRequest();
        Response response=prepareSearchRequest(Collections.singletonList(SEARCH_REQUEST_SCHEMA_ID), filter, sortBy, sortOrder,
                                                startIndex, count, attrsList, excludedAttrsList, searchReq, "userName");

        if (response==null) {
            if (!isAttributeRecognized(UserResource.class, searchReq.getSortBy()))
                response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_PATH, "sortBy parameter value not recognized");
            else
                response = userService.searchUsers(searchReq.getFilter(), searchReq.getSortBy(), searchReq.getSortOrder(),
                                            searchReq.getStartIndex(), searchReq.getCount(), searchReq.getAttributes(),
                                            searchReq.getExcludedAttributes(), authorization);
        }
        return response;

    }

    public Response searchUsersPost(SearchRequest searchRequest, String authorization){

        SearchRequest searchReq=new SearchRequest();
        Response response=prepareSearchRequest(searchRequest.getSchemas(), searchRequest.getFilter(), searchRequest.getSortBy(),
                            searchRequest.getSortOrder(), searchRequest.getStartIndex(), searchRequest.getCount(),
                            searchRequest.getAttributes(), searchRequest.getExcludedAttributes(), searchReq, "userName");

        if (response==null) {
            if (!isAttributeRecognized(UserResource.class, searchReq.getSortBy()))
                response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_PATH, "sortBy parameter value not recognized");
            else
                response = userService.searchUsersPost(searchReq, authorization);
        }
        return response;

    }

}
