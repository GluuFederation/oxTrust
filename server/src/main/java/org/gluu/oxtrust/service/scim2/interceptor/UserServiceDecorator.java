package org.gluu.oxtrust.service.scim2.interceptor;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.user.Meta;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;
import org.gluu.oxtrust.ws.rs.scim2.UserService;
import org.gluu.oxtrust.model.scim2.util.ResourceValidator;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.ldap.model.SortOrder;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

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

    private String executeDefaultValidation(BaseScimResource resource){

        String error=null;
        try {
            ResourceValidator rv=new ResourceValidator(resource, extService.getResourceExtensions(resource.getClass()));
            rv.validateRequiredAttributes();
            rv.validateSchemasAttribute();
            rv.validateValidableAttributes();
            //By section 7 of RFC 7643, we are not forced to constrain attribute values when they have a list of canonical values associated
            //rv.validateCanonicalizedAttributes();
            rv.validateExtendedAttributes();
        }
        catch (SCIMException e){
            error=e.getMessage();
        }
        return error;

    }

    private Response validateExistenceOfUser(String id){

        Response response=null;
        GluuCustomPerson person = personService.getPersonByInum(id);

        if (person==null) {
            log.info("Person with inum {} not found", id);
            response = getErrorResponse(Response.Status.NOT_FOUND, "Resource " + id + " not found");
        }
        return response;

    }

    public Response createUser(UserResource user, String attrsList, String excludedAttrsList, String authorization) {

        Response response;
        String error=executeDefaultValidation(user);

        if (error==null) {
            assignMetaInformation(user);
            //Proceed with actual implementation of createUser method
            response = userService.createUser(user, attrsList, excludedAttrsList, authorization);
        }
        else {
            log.error("Validation check at createUser returned: {}", error);
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, error);
        }
        return response;

    }

    public Response updateUser(UserResource user, String id, String attrsList, String excludedAttrsList, String authorization) {

        Response response;
        String error=executeDefaultValidation(user);

        if (error==null) {
            //Proceed with actual implementation of updateUser method
            response = userService.updateUser(user, id, attrsList, excludedAttrsList, authorization);
        }
        else {
            log.error("Validation check at updateUser returned: {}", error);
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, error);
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

    public Response getUserById(String id, String attrsList, String excludedAttrsList, String authorization){

        Response response=validateExistenceOfUser(id);
        if (response==null)
            //Proceed with actual implementation of getUserById method
            response=userService.getUserById(id, attrsList, excludedAttrsList, authorization);

        return response;

    }

    private boolean isAttributeRecognized(Class<? extends BaseScimResource> cls, String attribute){

        boolean valid;

        Extension ext=extService.extensionOfAttribute(cls, attribute);
        valid=ext!=null;

        if (!valid) {
            attribute = extService.stripDefaultSchema(cls, attribute);
            Field f=IntrospectUtil.findFieldFromPath(cls, attribute);
            valid= f!=null;
        }
        return valid;

    }

    private Response prepareSearchRequest(String filter, String sortBy, String sortOrder, Integer startIndex, Integer count,
                                          String attrsList, String excludedAttrsList, SearchRequest request){

        Response response;

        count=count==null ? MAX_COUNT : count;
        if (count>=0){
            if (count <= MAX_COUNT) {

                startIndex = (startIndex == null || startIndex < 1) ? 1 : startIndex;
                sortBy = StringUtils.isEmpty(sortBy) ? "userName" : sortBy;

                if (isAttributeRecognized(UserResource.class, sortBy)) {
                    if (StringUtils.isEmpty(sortOrder) || !sortOrder.equals(SortOrder.DESCENDING.getValue()))
                        sortOrder = SortOrder.ASCENDING.getValue();

                    request.setAttributes(attrsList);
                    request.setExcludedAttributes(excludedAttrsList);
                    request.setFilter(filter);
                    request.setSortBy(sortBy);
                    request.setSortOrder(sortOrder);
                    request.setStartIndex(startIndex);
                    request.setCount(count);

                    response=null;
                }
                else
                    response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_PATH, "sortBy parameter value not recognized");
            }
            else
                response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, "Maximum number of results per page is " + MAX_COUNT);
        }
        else
            response=getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, "count parameter must be non-negative");

        return response;

    }

    public Response searchUsers(String filter, String sortBy, String sortOrder, Integer startIndex, Integer count,
                                String attrsList, String excludedAttrsList, String authorization){

        SearchRequest searchReq=new SearchRequest();
        Response response=prepareSearchRequest(filter, sortBy, sortOrder, startIndex, count, attrsList, excludedAttrsList, searchReq);

        if (response==null)
            response = userService.searchUsers(searchReq.getFilter(), searchReq.getSortBy(), searchReq.getSortOrder(),
                                        searchReq.getStartIndex(), searchReq.getCount(), searchReq.getAttributes(),
                                        searchReq.getExcludedAttributes(), authorization);
        return response;

    }

    public Response searchUsersPost(SearchRequest searchRequest, String authorization){

        SearchRequest searchReq=new SearchRequest();
        Response response=prepareSearchRequest(searchRequest.getFilter(), searchRequest.getSortBy(), searchRequest.getSortOrder(),
                searchRequest.getStartIndex(), searchRequest.getCount(), searchRequest.getAttributes(), searchRequest.getExcludedAttributes(), searchReq);

        if (response==null)
            response = userService.searchUsersPost(searchReq, authorization);

        return response;

    }

}
