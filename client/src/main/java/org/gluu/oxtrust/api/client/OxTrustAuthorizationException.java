/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

/**
 * A message about an authorization fail.
 * 
 * @author Yuriy Movchan
 * @author Dmitry Ognyannikov
 */
public class OxTrustAuthorizationException extends Exception {
    
    public OxTrustAuthorizationException(String message) {
        super(message);
    }
    
    public OxTrustAuthorizationException(String message, int httpResponseCode) {
        super(message);
    }

    public OxTrustAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}