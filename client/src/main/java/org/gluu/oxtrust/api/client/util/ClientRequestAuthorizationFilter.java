/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.util;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * HTTP request authentication filter.
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientRequestAuthorizationFilter implements ClientRequestFilter {
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String HEADER_NAME = "oauth_token";
    
    private final String authenticationToken;
    
    public ClientRequestAuthorizationFilter(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Add session token header
        requestContext.setProperty(HEADER_NAME, authenticationToken);
    }
}