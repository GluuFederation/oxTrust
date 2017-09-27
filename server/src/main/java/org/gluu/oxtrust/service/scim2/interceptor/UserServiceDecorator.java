package org.gluu.oxtrust.service.scim2.interceptor;

import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.user.Meta;
import org.gluu.oxtrust.model.scim2.user.UserResource;
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
import java.util.Date;

/**
 * Aims at decorating SCIM service methods. Currently applies validations via ResourceValidator class
 *
 * Created by jgomer on 2017-09-01.
 */
@Priority(Interceptor.Priority.APPLICATION)
@Decorator
public abstract class UserServiceDecorator extends BaseScimWebService implements UserService {

    @Inject
    private Logger log;

    @Inject @Delegate @Any
    UserService userService;

    private void assignMetaInformation(BaseScimResource resource){

        //Generate some meta information (this replaces the info client passed in the request)
        long now=new Date().getTime();
        String val= ISODateTimeFormat.dateTime().withZoneUTC().print(now);

        Meta meta=new Meta();
        meta.setResourceType(ResourceType.getType(resource.getClass()));
        meta.setCreated(val);
        meta.setLastModified(val);
        //For version attritute: Service provider support for this attribute is optional and subject to the service provider's support for versioning
        //For location attribute: this will be set after current user creation at LDAP
        resource.setMeta(meta);

    }

    private String executeDefaultValidation(BaseScimResource resource){

        String error=null;
        try {
            ResourceValidator rv=new ResourceValidator(resource);
            rv.validateRequiredAttributes();
            rv.validateSchemaAttribute();
            rv.validateValidableAttributes();
            //By section 7 of RFC 7643, we are not forced to constrain attribute values when they have a list of canonical values associated
            //rv.validateCanonicalizedAttributes();
        }
        catch (SCIMException e){
            error=e.getMessage();
        }
        return error;

    }

    public Response createUser(UserResource user, String attrsList, String excludedAttrsList, String authorization) throws Exception {

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

    public Response updateUser(UserResource user, String attrsList, String excludedAttrsList, String id, String authorization) throws Exception {

        Response response;
        String error=executeDefaultValidation(user);

        if (error==null) {
            //Proceed with actual implementation of updateUser method
            response = userService.updateUser(user, attrsList, excludedAttrsList, id, authorization);
        }
        else {
            log.error("Validation check at updateUser returned: {}", error);
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, error);
        }
        return response;

    }
/*
    public Response updateUser(String authorization, String id, User user, final String attributesArray) throws Exception {
        Response validationResponse=applyValidation(user);
        log.info("Validation at updateUser returned {}", (validationResponse==null));
        return (validationResponse==null) ? userService.updateUser(authorization, id, user, attributesArray) : validationResponse;
    }

    public Response patchUser(String authorization, String id, ScimPatchUser user, final String attributesArray) throws Exception{

        boolean validation=ResourceValidator.validate(user);
        log.info("Validation at patchUser returned {}", validation);
        if (validation) {
            return userService.patchUser(authorization, id, user, attributesArray);
        }
        else {
            return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, ResourceValidator.FAIL_MESSAGE_USER);
        }
    }
*/

}
