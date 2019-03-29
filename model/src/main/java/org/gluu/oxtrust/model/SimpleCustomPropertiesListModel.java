/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.List;

import org.gluu.model.SimpleCustomProperty;

/**
 * Base operations with custom properties list. Need for JSF2 facelet because it
 * doesn't support parameters in action method
 * 
 * @author Yuriy Movchan Date: 12/19/2012
 */
public interface SimpleCustomPropertiesListModel {

	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties);

	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty);

}