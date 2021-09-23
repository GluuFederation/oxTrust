package org.gluu.oxtrust.auth.oauth;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.gluu.oxauth.client.service.ClientFactory;
import org.gluu.oxauth.client.service.IntrospectionService;
import org.gluu.oxauth.model.common.IntrospectionResponse;
import org.gluu.oxtrust.auth.IProtectionService;
import org.gluu.oxtrust.service.JsonConfigurationService;

import org.slf4j.Logger;

public abstract class BaseOAuthProtectionService implements IProtectionService, Serializable {

    private static final long serialVersionUID = -1147131971095460010L;

    @Inject
    private Logger log;

    @Inject
    private JsonConfigurationService jsonConfigurationService;
    
    private IntrospectionService introspectionService;

    protected abstract Response processIntrospectionResponse(IntrospectionResponse response,
            ResourceInfo resourceInfo);
    
    @Override
    public Response processAuthorization(HttpHeaders headers, ResourceInfo resourceInfo) {
        
        Response authorizationResponse;
        try {
            String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            boolean authFound = StringUtils.isNotEmpty(token);
            log.info("Authorization header {} found", authFound ? "" : "not");
            
            if (authFound) {
                token = token.replaceFirst("Bearer\\s+","");
                log.debug("Validating token {}", token);

                IntrospectionResponse iresp = null;
                try {
                    iresp = introspectionService.introspectToken("Bearer " + token, token);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                authorizationResponse = processIntrospectionResponse(iresp, resourceInfo);

            } else {
                log.info("Request is missing authorization header");
                //see section 3.12 RFC 7644
                authorizationResponse = IProtectionService.simpleResponse(Response.Status.UNAUTHORIZED,
                        "No authorization header found");
            }    
        } catch (Exception e){
            log.error(e.getMessage(), e);
            authorizationResponse = IProtectionService.simpleResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
        return authorizationResponse;

    }
    
    @PostConstruct
    private void init() {

        try {
            String introspectionEndpoint = jsonConfigurationService.getOxauthAppConfiguration()
                    .getIntrospectionEndpoint();
            
            introspectionService = ClientFactory.instance().createIntrospectionService(
            	introspectionEndpoint, ClientFactory.instance().createEngine());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
    }

}
