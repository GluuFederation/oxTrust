/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

/**
 * A message about test fail.
 * 
 * @author Dmitry Ognyannikov
 */
public class OxTrustAPIException extends Exception {
    
    private int httpResponseCode;
    
    public OxTrustAPIException(String message) {
        super(message);
    }
    
    public OxTrustAPIException(String message, int httpResponseCode) {
        super(message);
        
        this.httpResponseCode = httpResponseCode;
    }

    /**
     * @return the httpResponseCode
     */
    public int getHttpResponseCode() {
        return httpResponseCode;
    }
}
