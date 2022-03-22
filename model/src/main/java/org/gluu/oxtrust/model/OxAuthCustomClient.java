/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.AttributesList;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.util.StringHelper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@DataEntry(sortBy = { "displayName" })
@ObjectClass(value = "oxAuthClient")
@JsonInclude(Include.NON_NULL)
public class OxAuthCustomClient extends CustomEntry implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3319774915823259905L;

	@AttributesList(name = "name", value = "values", sortByName = true, attributesConfiguration = {
			@AttributeName(name = "inum", ignoreDuringUpdate = true),
			@AttributeName(name = "userPassword", ignoreDuringRead = true) })
	private List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();

	public List<GluuCustomAttribute> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public String getInum() {
		return StringHelper.toString(getAttribute("inum"));
	}

	public void setInum(String value) {
		setAttribute("inum", value);
	}

	public String getDisplayName() {
		return StringHelper.toString(getAttribute("displayName"));
	}

	public void setDisplayName(String value) {
		setAttribute("displayName", value);
	}

	public String getOxAuthClientSecret() {
		return StringHelper.toString(getAttribute("oxAuthClientSecret"));
	}

	public void setOxAuthClientSecret(String value) {
		setAttribute("oxAuthClientSecret", value);
	}

	public String getUserPassword() {
		return StringHelper.toString(getAttribute("userPassword"));
	}

	public void setUserPassword(String value) {
		setAttribute("userPassword", value);
	}

}
