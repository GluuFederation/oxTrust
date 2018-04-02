/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * HTTP request logger.
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientRequestLoggingFilter implements ClientRequestFilter {
    private static final Logger logger = Logger.getLogger(ClientRequestLoggingFilter.class.getName());
    
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        logger.log(Level.INFO, "Request URI: {0}", requestContext.getUri());
        logger.log(Level.INFO, "Request method: {0}", requestContext.getMethod());
        if (requestContext.getEntity() == null) {
            logger.log(Level.INFO, "empty requestContext");
        }
        else
            logger.log(Level.INFO, objectMapper.writeValueAsString(requestContext.getEntity()));
    }
}
