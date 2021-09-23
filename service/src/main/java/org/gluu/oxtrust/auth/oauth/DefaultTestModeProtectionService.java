package org.gluu.oxtrust.auth.oauth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import org.gluu.oxauth.model.common.IntrospectionResponse;
import org.gluu.oxtrust.auth.IProtectionService;

import org.slf4j.Logger;
        
@ApplicationScoped
public class DefaultTestModeProtectionService extends BaseOAuthProtectionService {

    @Inject
    private Logger log;
    
    public Response processIntrospectionResponse(IntrospectionResponse iresponse,
            ResourceInfo resourceInfo) {
        
        Response response = null;
        if (iresponse == null || !iresponse.isActive()) {
            String msg = "Invalid token";
            log.error(msg);
            //see section 3.12 RFC 7644
            response = IProtectionService.simpleResponse(Response.Status.FORBIDDEN, msg);
        }
        return response;
        
    }
    
}
