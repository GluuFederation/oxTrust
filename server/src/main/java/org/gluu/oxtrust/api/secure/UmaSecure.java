/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.secure;

import javax.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * API protection annotation
 * 
 * @author Yuriy Movchan Date: 02/13/2017
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface UmaSecure {

	/**
     * @return UMA scopes which application should have to access this endpoint.
     */
	String[] scopes() default {};
}
