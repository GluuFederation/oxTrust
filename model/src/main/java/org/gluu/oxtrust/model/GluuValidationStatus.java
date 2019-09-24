/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.persist.annotation.AttributeEnum;

/**
 * @author �Oleksiy Tataryn�
 */
public enum GluuValidationStatus implements AttributeEnum {

	PENDING("In Progress", "In Progress"), SUCCESS("Success", "Success"), SCHEDULED("Scheduled",
			"Scheduled"), FAILED("Failed", "Failed");

	private String value;
	private String displayName;

	private static Map<String, GluuValidationStatus> mapByValues = new HashMap<String, GluuValidationStatus>();
	static {
		for (GluuValidationStatus enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private GluuValidationStatus(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static GluuValidationStatus getByValue(String value) {
		return mapByValues.get(value);
	}

	public Enum<? extends AttributeEnum> resolveByValue(String value) {
		return getByValue(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
