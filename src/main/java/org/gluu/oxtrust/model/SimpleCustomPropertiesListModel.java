package org.gluu.oxtrust.model;

import java.util.List;

import org.xdi.model.SimpleCustomProperty;

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