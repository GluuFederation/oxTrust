/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person Emails
 * 
 * @author Reda Zerrad Date: 04.23.2012
 */

@JsonPropertyOrder({ "value", "type", "primary" })
@XmlType(propOrder = { "value", "type", "primary" })
public class ScimPersonEmails {

	private String value;
	private String type;
	private String primary;

	public ScimPersonEmails() {
		value = "";
		type = "";
		primary = "";
	}

	public String getValue() {

		return this.value;
	}

	public void setValue(String value) {

		this.value = value;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPrimary() {
		return this.primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}
}
