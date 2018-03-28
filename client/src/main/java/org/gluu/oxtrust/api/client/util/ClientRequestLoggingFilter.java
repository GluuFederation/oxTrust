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
import javax.ws.rs.client.ClientRequestFilter;

/**
 * HTTP request logger.
 * 
 * @author Dmitry Ognyannikov
 */
public class ClientRequestLoggingFilter implements ClientRequestFilter {
    private static final Logger logger = Logger.getLogger(ClientRequestLoggingFilter.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (requestContext == null || requestContext.getEntity() == null) {
            logger.log(Level.INFO, "empty requestContext");
        }
        else
            logger.log(Level.INFO, requestContext.getEntity().toString());
    }
}
