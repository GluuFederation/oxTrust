package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

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
