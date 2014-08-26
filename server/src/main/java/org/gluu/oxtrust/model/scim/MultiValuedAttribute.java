package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * User: Dejan Maric
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multiValuedAttribute")
public class MultiValuedAttribute {

	protected String value;
	protected String display;
	protected Boolean primary;
	protected String type;
	protected String operation;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public MultiValuedAttribute() {
		value = "";
		display = "";
		primary = true;
		type = "";
		operation = "";

	}
}
