/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person groups attributes
 * 
 * @author Reda Zerrad Date: 04.17.2012
 */
@JsonPropertyOrder({ "display", "value" })
@XmlType(propOrder = { "display", "value" })
public class ScimPersonGroups {

	private String value;
	private String display;

	public ScimPersonGroups() {
		value = "";
		display = "";
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {

		this.value = value;
	}

	public String getDisplay() {
		return this.display;
	}

	public void setDisplay(String display) {

		this.display = display;
	}

}
