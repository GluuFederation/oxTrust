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
 * oxAuth IdToken Signed Response Algorithm
 * 
 * @author Reda Zerrad Date: 06.15.2012
 * @author Javier Rojas Blum
 * @version November 10, 2015
 */
public enum SignatureAlgorithm implements AttributeEnum {

    NONE("none", "none"),
	HS256("HS256", "HS256"), HS384("HS384", "HS384"), HS512("HS512", "HS512"),
    RS256("RS256", "RS256"), RS384("RS384", "RS384"), RS512("RS512", "RS512"),
    ES256("ES256", "ES256"), ES384("ES384", "ES384"), ES512("ES512", "ES512"),
	PS256("PS256", "PS256"), PS384("PS384", "PS384"), PS512("PS512", "PS512");

	private String value;
	private String displayName;

	private static Map<String, SignatureAlgorithm> mapByValues = new HashMap<String, SignatureAlgorithm>();

	static {
		for (SignatureAlgorithm enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private SignatureAlgorithm(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static SignatureAlgorithm getByValue(String value) {
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
