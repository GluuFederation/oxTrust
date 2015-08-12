/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

/**
 * SCIM Group members
 * 
 * @author Reda Zerrad Date: 05.03.2012
 */
public class ScimGroupMembers {

	private String value;
	private String display;

	public ScimGroupMembers() {

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
