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
 * Metadata source type
 * 
 * @author Yuriy Movchan Date: 11.05.2010
 */
public enum GluuMetadataSourceType implements LdapEnum {

	NONE("none", "None") ,FILE("file", "File"), URI("uri", "URI"), GENERATE("generate", "Generate"), FEDERATION("federation", "Federation");

	private final String value;
	private final String displayName;

	private static final Map<String, GluuMetadataSourceType> mapByValues = new HashMap<String, GluuMetadataSourceType>();
	static {
		for (GluuMetadataSourceType enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private GluuMetadataSourceType(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

        @Override
	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static GluuMetadataSourceType getByValue(String value) {
		return mapByValues.get(value);
	}

        @Override
	public Enum<? extends LdapEnum> resolveByValue(String value) {
		return getByValue(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
