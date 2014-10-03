/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service.intercept.interfaces;

import org.gluu.oxtrust.model.GluuCustomPerson;

/**
 * Base interface for python script
 * 
 * @author Yuriy Movchan Date: 07.04.2012
 */
public interface EntryInterceptorType {

	public boolean updateAttributes(GluuCustomPerson person);

}
