/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.site.ldap.persistence.annotation.LdapEnum;

/**
 * Group Visibility
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
public enum GluuGroupVisibility implements LdapEnum {

	PUBLIC("public", "Public"), PRIVATE("private", "Private");

	private String value;
	private String displayName;

	private static Map<String, GluuGroupVisibility> mapByValues = new HashMap<String, GluuGroupVisibility>();

	static {
		for (GluuGroupVisibility enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private GluuGroupVisibility(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static GluuGroupVisibility getByValue(String value) {
		return mapByValues.get(value);
	}

	public Enum<? extends LdapEnum> resolveByValue(String value) {
		return getByValue(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
