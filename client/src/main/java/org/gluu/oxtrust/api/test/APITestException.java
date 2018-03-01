/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

/**
 * A message about test fail.
 * 
 * @author Dmitry Ognyannikov
 */
public class APITestException extends Exception {
    public APITestException(String message) {
        super(message);
    }
}
