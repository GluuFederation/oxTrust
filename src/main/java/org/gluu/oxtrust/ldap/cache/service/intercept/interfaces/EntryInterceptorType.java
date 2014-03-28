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
