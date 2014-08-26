package org.gluu.oxtrust.model;

import java.util.List;

import org.xdi.model.SimpleProperty;

/**
 * Base operations with properties list. Need for JSF2 facelet because it
 * doesn't support parameters in action method
 * 
 * @author Yuriy Movchan Date: 12/19/2012
 */
public interface SimplePropertiesListModel {

	public void addItemToSimpleProperties(List<SimpleProperty> simpleProperties);

	public void removeItemFromSimpleProperties(List<SimpleProperty> simpleProperties, SimpleProperty simpleProperty);

}