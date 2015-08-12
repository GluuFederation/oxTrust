/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.*;

/**
 * Person Search
 * 
 * @author Reda Zerrad Date: 08.07.2012
 */

@XmlRootElement(name = "SearchService")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "attribute", "value" })
@XmlType(propOrder = { "attribute", "value" })
public class ScimPersonSearch {

	private String attribute;
	private String value;

	public ScimPersonSearch() {
		this.attribute = "";
		this.value = "";
	}

	@XmlElement
	public String getAttribute() {
		return this.attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	@XmlElement
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
