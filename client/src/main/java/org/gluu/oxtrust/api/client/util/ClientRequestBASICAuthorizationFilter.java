/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gluu.oxtrust.api.client.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * HTTP request authentication filter.
 * 
 * BASIC method should be user only for test purposes.
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientRequestBASICAuthorizationFilter implements ClientRequestFilter {
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String HEADER_NAME = "Authorization";
    
    private final String authenticationEncoded;
    
    public ClientRequestBASICAuthorizationFilter(String user, String password) throws UnsupportedEncodingException {
        String auth = user + ":" + password;
        
        this.authenticationEncoded = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes("UTF8"));
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Add session token header
        if (authenticationEncoded != null && !authenticationEncoded.isEmpty())
            requestContext.setProperty(HEADER_NAME, authenticationEncoded);
    }
}