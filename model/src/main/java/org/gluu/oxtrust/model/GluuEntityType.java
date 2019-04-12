package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.persist.annotation.AttributeEnum;

/**
 * Metadata source type
 * 
 * @author Shekhar Laad Date: 25.08.2016
 */

public enum GluuEntityType implements AttributeEnum {

	SingleSP("Single SP", "Single SP"), FederationAggregate("Federation/Aggregate", "Federation/Aggregate");

	private final String value;
	private final String displayName;

	private static final Map<String, GluuEntityType> mapByValues = new HashMap<String, GluuEntityType>();
	static {
		for (GluuEntityType enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private GluuEntityType(String value, String displayName) {
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

	public static GluuEntityType getByValue(String value) {
		return mapByValues.get(value);
	}

        @Override
	public Enum<? extends AttributeEnum> resolveByValue(String value) {
		return getByValue(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
