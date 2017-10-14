package org.gluu.oxtrust.service.scim2.interceptor;

import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

/**
 * This class checks whether authorization header is present and is valid before current scim service methods are
 * actually called.
 * To protect methods with this interceptor just add the Protected annotation to them
 *
 * Created by jgomer on 2017-08-31.
 */
@Protected
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ServiceInterceptor {

    @Inject
    private Logger log;

    @Inject
    private JsonConfigurationService jsonConfigurationService;

    /**
     * Does some pre-processing of parameters:
     * Searches for a parameter of type String and annotated with HeaderParam. If found, the respective processing
     * authorization method is called (test mode or UMA protection) and  if successful, the request follows to its
     * destination service object. If not found a Response object is returned immediately signaling the authorization error
     * @param ctx InvocationContext of current call
     * @return An object (usually the result of calling ctx.proceed()
     */
    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {
        log.warn("Bypassing protection TEMPORARILY");
        return ctx.proceed();
/*
        log.debug("SCIM Service call intercepted");
        String authorization=getAuthzHeaderValue(ctx);
        Response authorizationResponse=null;

        if (authorization==null) {
            log.info("Request missing authorization header");
            authorizationResponse= BaseScimWebService.getErrorResponse(Response.Status.FORBIDDEN, "No authorization header found");
        }
        else{
            log.info("Found authorization header");

            try {
                if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
                    log.info(" ##### SCIM Test Mode is ACTIVE");
                    authorizationResponse = processTestModeAuthorization(authorization);
                }
                else {
                    authorizationResponse = processAuthorization(authorization);
                }
            }
            catch (Exception e){
                log.error(e.getMessage(), e);
                authorizationResponse=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        if (authorizationResponse == null) {
            log.info("Authorization passed");
            //If authorization passed, proceed with actual processing of request
            return ctx.proceed();
        }
        else{
            return authorizationResponse;
        }
*/
    }

    private String getAuthzHeaderValue(InvocationContext ctx){

        Object[] params=ctx.getParameters();
        Annotation[][] annotations=ctx.getMethod().getParameterAnnotations();

        int i=IntrospectUtil.indexOfAuthzHeader(annotations);
        return (i>=0 && params[i]!=null && params[i] instanceof String) ? params[i].toString() : null;

    }


}