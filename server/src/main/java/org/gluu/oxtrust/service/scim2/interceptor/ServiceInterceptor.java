package org.gluu.oxtrust.service.scim2.interceptor;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.uma.ScimUmaProtectionService;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;
import org.slf4j.Logger;
import org.xdi.oxauth.client.ClientInfoClient;
import org.xdi.oxauth.client.ClientInfoResponse;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;

import static javax.ws.rs.core.Response.Status;

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

    @Inject
    private OpenIdService openIdService;

    @Inject
    private ScimUmaProtectionService scimUmaProtectionService;

    @Inject
    private UmaPermissionService umaPermissionService;

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
/*       log.warn("Bypassing protection TEMPORARILY");
        return ctx.proceed();
*/
        Response authorizationResponse;

        log.info("==== SCIM Service call intercepted ====");
        String authorization=getAuthzHeaderValue(ctx);
        log.info("Authorization header {} found", StringUtils.isEmpty(authorization) ? "not" : "");

        try {
            if (jsonConfigurationService.getOxTrustappConfiguration().isScimTestMode()) {
                log.info("SCIM Test Mode is ACTIVE");
                authorizationResponse = processTestModeAuthorization(authorization);
            }
            else
            if (scimUmaProtectionService.isEnabled()){
                log.info("SCIM is protected by UMA");
                authorizationResponse = processUmaAuthorization(authorization);
            }
            else{
                log.info("Please activate UMA or test mode to protect your SCIM endpoints. Read the Gluu SCIM docs to learn more");
                authorizationResponse= BaseScimWebService.getErrorResponse(Status.SERVICE_UNAVAILABLE, "SCIM API not protected");
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
            authorizationResponse=BaseScimWebService.getErrorResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        if (authorizationResponse == null) {
            log.info("Authorization passed");
            //If authorization passed, proceed with actual processing of request
            return ctx.proceed();
        }
        else
            return authorizationResponse;

    }

    private Response processTestModeAuthorization(String token) throws Exception {

        Response response = null;

        if (StringUtils.isNotEmpty(token)) {
            token=token.replaceFirst("Bearer\\s+","");
            log.debug("Validating token {}", token);

            String clientInfoEndpoint=openIdService.getOpenIdConfiguration().getClientInfoEndpoint();
            ClientInfoClient clientInfoClient = new ClientInfoClient(clientInfoEndpoint);
            ClientInfoResponse clientInfoResponse = clientInfoClient.execClientInfo(token);

            if (clientInfoResponse.getErrorType()!=null) {
                response=BaseScimWebService.getErrorResponse(Status.UNAUTHORIZED, "Invalid token "+ token);
                log.debug("Error validating access token: {}", clientInfoResponse.getErrorDescription());
            }
        }
        else{
            log.info("Request is missing authorization header");
            //see section 3.12 RFC 7644
            response = BaseScimWebService.getErrorResponse(Status.UNAUTHORIZED, "No authorization header found");
        }
        return response;

    }

    private Response processUmaAuthorization(String authorization) throws Exception {

        Token patToken;
        try {
            patToken = scimUmaProtectionService.getPatToken();
        }
        catch (UmaProtectionException ex) {
            return BaseScimWebService.getErrorResponse(Status.INTERNAL_SERVER_ERROR, "Failed to obtain PAT token");
        }

        Pair<Boolean, Response> rptTokenValidationResult = umaPermissionService.validateRptToken(patToken, authorization, scimUmaProtectionService.getUmaResourceId(), scimUmaProtectionService.getUmaScope());
        if (rptTokenValidationResult.getFirst()) {
            if (rptTokenValidationResult.getSecond() != null) {
                return rptTokenValidationResult.getSecond();
            }
        }
        else {
            return BaseScimWebService.getErrorResponse(Status.INTERNAL_SERVER_ERROR, "Invalid GAT/RPT token");
        }
        return null;

    }

    private String getAuthzHeaderValue(InvocationContext ctx){

        Object[] params=ctx.getParameters();
        Annotation[][] annotations=ctx.getMethod().getParameterAnnotations();

        int i=IntrospectUtil.indexOfAuthzHeader(annotations);
        return (i>=0 && params[i]!=null && params[i] instanceof String) ? params[i].toString() : null;

    }


}