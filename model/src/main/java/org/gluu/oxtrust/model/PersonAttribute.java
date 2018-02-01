/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "attribute")
public class PersonAttribute {

	private String name;
	private String value;
	private String displayName;

	public PersonAttribute() {
		// Do Nothing
	}

	public PersonAttribute(String name, String value, String displayName) {
		this.name = name;
		this.value = value;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
