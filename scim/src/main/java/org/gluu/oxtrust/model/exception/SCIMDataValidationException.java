/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.exception;

public class SCIMDataValidationException extends SCIMException{

    private static final long serialVersionUID = 7416418339453997681L;

    public SCIMDataValidationException(String message) {
        super(message);
    }
    
    public SCIMDataValidationException(String message, Throwable e) {
        super(message, e);
    }

}
