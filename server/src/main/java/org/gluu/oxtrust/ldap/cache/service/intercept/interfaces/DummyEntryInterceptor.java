/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service.intercept.interfaces;

import org.gluu.oxtrust.model.GluuCustomPerson;

/**
 * Dummy implementation of interface EntryInterceptorType
 * 
 * @author Yuriy Movchan Date: 07.05.2012
 */
public class DummyEntryInterceptor implements EntryInterceptorType {

	public boolean updateAttributes(GluuCustomPerson person) {
		return false;
	}

}
