/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * HTTP response logger.
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientResponseLoggingFilter implements ClientResponseFilter {
    private static final Logger logger = Logger.getLogger(ClientResponseLoggingFilter.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {        
        logger.log(Level.INFO, "Response status: " + responseContext.getStatus());
        
        if (responseContext == null || !responseContext.hasEntity()) {
            logger.log(Level.INFO, "empty responseContext");
        }
    }
    
}
