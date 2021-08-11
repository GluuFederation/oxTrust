package org.gluu.oxtrust.auth.oauth;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import org.gluu.oxauth.client.service.ClientFactory;
import org.gluu.oxauth.client.service.IntrospectionService;
import org.gluu.oxauth.model.common.IntrospectionResponse;
import org.gluu.oxtrust.service.JsonConfigurationService;

import org.slf4j.Logger;

@ApplicationScoped
@Named
public class OAuthProtectionService implements Serializable {

	private static final long serialVersionUID = -1147131971095468865L;

    @Inject
    private Logger log;

    @Inject
    private JsonConfigurationService jsonConfigurationService;
    
    private IntrospectionService introspectionService;

    public Response processAuthorization(HttpHeaders headers, List<String> scopes) {

        Response authorizationResponse = null;
        String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        boolean authFound = StringUtils.isNotEmpty(token);

        try {
            log.info("Authorization header {} found", authFound ? "" : "not");   
			if (authFound) {
				token = token.replaceFirst("Bearer\\s+","");
				log.debug("Validating token {}", token);
								
                IntrospectionResponse response = null;
                try {
                	response = introspectionService.introspectToken("Bearer " + token, token);
                } catch (Exception e) {
                	log.error(e.getMessage());
                }
			    log.info("Call requires scopes: {}", scopes);	
                List<String> tokenScopes = Optional.ofNullable(response).map(IntrospectionResponse::getScope).orElse(null);

                if (tokenScopes == null || !response.isActive() || !tokenScopes.containsAll(scopes)) {
                	String msg = "Invalid token or insufficient scopes";
                    log.error("{}. Token scopes: {}", msg, tokenScopes);
				    //see section 3.12 RFC 7644
                    authorizationResponse = getErrorResponse(Status.FORBIDDEN, msg);
                }
			} else {
				log.info("Request is missing authorization header");
				//see section 3.12 RFC 7644
				authorizationResponse = getErrorResponse(Status.UNAUTHORIZED, "No authorization header found");
			}    
        } catch (Exception e){
            log.error(e.getMessage(), e);
            authorizationResponse = getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return authorizationResponse;

    }

	private Response getErrorResponse(Response.Status status, String detail) {
		return Response.status(status).entity(detail).build();
	}
    
    @PostConstruct
    private void init() {

        try {
        	String introspectionEndpoint = jsonConfigurationService.getOxauthAppConfiguration().getIntrospectionEndpoint();
            introspectionService = ClientFactory.instance().createIntrospectionService(
            	introspectionEndpoint, ClientFactory.instance().createEngine());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
    }

}