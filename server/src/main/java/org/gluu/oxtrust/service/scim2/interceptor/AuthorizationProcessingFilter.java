/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service.scim2.interceptor;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.uma.ScimUmaProtectionService;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;
import org.slf4j.Logger;
import org.xdi.oxauth.client.ClientInfoClient;
import org.xdi.oxauth.client.ClientInfoResponse;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status;

/**
 * This class checks whether authorization header is present and is valid before scim service methods are actually called.
 * To protect methods with this filter just add the ScimAuthorization annotation to them
 *
 * Created by jgomer on 2017-11-25.
 */
@Provider
@ScimAuthorization
public class AuthorizationProcessingFilter implements ContainerRequestFilter {

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
     * This method performs the protection check of SCIM invocations: it searches for the "Authorization" Header param
     * and does the respective processing by calling test mode or UMA validation token logic. If successful, the request
     * follows to its destination service object. If not, a Response object is returned immediately signaling an
     * authorization error
     * @param requestContext The ContainerRequestContext associated to filter execution
     * @throws IOException Whenever checking the authorization throws an exception as well
     */
    /*
     * Comment this method body if you want to skip the authorization check and proceed straight to use the service. This
     * is useful under certain development circumstances
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        //log.warn("Bypassing protection TEMPORARILY");
/**/
        Response authorizationResponse;
        log.info("==== SCIM Service call intercepted ====");
        String authorization = requestContext.getHeaderString("Authorization");
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
                authorizationResponse= BaseScimWebService.getErrorResponse(Status.UNAUTHORIZED, "SCIM API not protected");
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
            authorizationResponse=BaseScimWebService.getErrorResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        if (authorizationResponse == null)
            log.info("Authorization passed");   //If authorization passed, proceed with actual processing of request
        else
            requestContext.abortWith(authorizationResponse);

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
            response = BaseScimWebService.getErrorResponse(Status.INTERNAL_SERVER_ERROR, "No authorization header found");
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
            return BaseScimWebService.getErrorResponse(Status.UNAUTHORIZED, "Invalid GAT/RPT token");
        }
        return null;

    }

}
