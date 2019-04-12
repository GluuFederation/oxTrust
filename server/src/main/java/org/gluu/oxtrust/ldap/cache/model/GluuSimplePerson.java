/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.model.CustomEntry;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.persist.annotation.AttributesList;
import org.gluu.persist.annotation.DataEntry;

/**
 * Person with custom attributes
 * 
 * @author Yuriy Movchan Date: 07.13.2011
 */
@DataEntry
public class GluuSimplePerson extends CustomEntry implements Serializable {

	private static final long serialVersionUID = -2279582184398161100L;

	private String sourceServerName;

	@AttributesList(name = "name", value = "values", sortByName = true)
	private List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();

	public List<GluuCustomAttribute> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public String getSourceServerName() {
		return sourceServerName;
	}

	public void setSourceServerName(String sourceServerName) {
		this.sourceServerName = sourceServerName;
	}

}
