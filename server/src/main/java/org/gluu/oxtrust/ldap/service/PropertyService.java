/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Provides operations to get property values
 * 
 * @author Yuriy Movchan Date: 08/11/2013
 */
@Scope(ScopeType.STATELESS)
@Name("propertyService")
@AutoCreate
public class PropertyService implements Serializable {

	private static final long serialVersionUID = -1707238475653913313L;


	/**
	 * Returns object property value
	 * 
	 * @param bean Bean
	 * @param name Property value
	 * @return Value of property
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public Object getPropertyValue(Object bean, String name) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return BeanUtils.getProperty(bean, name);
	}

}
