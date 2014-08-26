package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person Photos
 * 
 * @author Reda Zerrad Date: 04.23.2012
 */
@JsonPropertyOrder({ "value", "type" })
@XmlType(propOrder = { "value", "type" })
public class ScimPersonPhotos {

	private String value;
	private String type;

	public ScimPersonPhotos() {
		value = "";
		type = "";
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

}
