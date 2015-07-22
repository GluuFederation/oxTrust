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
 * oxAuthTrustedClient
 * 
 * @author Reda Zerrad Date: 07.30.2012
 */
public enum OxAuthTrustedClientBox implements LdapEnum {

	WEB("true", "Enabled"), NATIVE("false", "Disabled");

	private String value;
	private String displayName;

	private static Map<String, OxAuthTrustedClientBox> mapByValues = new HashMap<String, OxAuthTrustedClientBox>();

	static {
		for (OxAuthTrustedClientBox enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private OxAuthTrustedClientBox(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static OxAuthTrustedClientBox getByValue(String value) {
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
