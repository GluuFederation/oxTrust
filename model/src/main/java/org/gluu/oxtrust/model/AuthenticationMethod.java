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
 * @author Yuriy Movchan Date: 07/07/2014
 */
public enum AuthenticationMethod implements AttributeEnum {

    /**
     * Clients in possession of a client password authenticate with the Authorization Server
     * using HTTP Basic authentication scheme. Default one if not client authentication is specified.
     */
    CLIENT_SECRET_BASIC("client_secret_basic", "client_secret_basic"),

    /**
     * Clients in possession of a client password authenticate with the Authorization Server
     * by including the client credentials in the request body.
     */
    CLIENT_SECRET_POST("client_secret_post", "client_secret_post"),

    /**
     * Clients in possession of a client password create a JWT using the HMAC-SHA algorithm.
     * The HMAC (Hash-based Message Authentication Code) is calculated using the client_secret
     * as the shared key.
     */
    CLIENT_SECRET_JWT("client_secret_jwt", "client_secret_jwt"),

    /**
     * Clients that have registered a public key sign a JWT using the RSA algorithm if a RSA
     * key was registered or the ECDSA algorithm if an Elliptic Curve key was registered.
     */
    PRIVATE_KEY_JWT("private_key_jwt", "private_key_jwt"),

    /**
     * The Client does not authenticate itself at the Token Endpoint, either because it uses only the Implicit Flow
     * (and so does not use the Token Endpoint) or because it is a Public Client with no Client Secret or other
     * authentication mechanism.
     */
    NONE("none", "none");


	private String value;
	private String displayName;

	private static Map<String, AuthenticationMethod> mapByValues = new HashMap<String, AuthenticationMethod>();

	static {
		for (AuthenticationMethod enumType : values()) {
			mapByValues.put(enumType.getValue(), enumType);
		}
	}

	private AuthenticationMethod(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static AuthenticationMethod getByValue(String value) {
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