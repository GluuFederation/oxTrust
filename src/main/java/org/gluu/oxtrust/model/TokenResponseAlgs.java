package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.site.ldap.persistence.annotation.LdapEnum;

/**
 * oxAuth IdToken Signed Response Algorithm
 * 
 * @author Reda Zerrad Date: 06.15.2012
 */
public enum TokenResponseAlgs implements LdapEnum {

	HS256("HS256", "HS256"), HS384("HS384", "HS384"), HS512("HS512", "HS512"), RS256("RS256", "RS256"), RS384("RS384", "RS384"), RS512(
			"RS512", "RS512");

	private String value;
	private String displayName;

	private static Map<String, TokenResponseAlgs> mapByValues = new HashMap<String, TokenResponseAlgs>();

	static {
		for (TokenResponseAlgs enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private TokenResponseAlgs(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static TokenResponseAlgs getByValue(String value) {
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
