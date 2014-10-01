/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import lombok.Data;


public @Data class ProfileConfiguration implements Serializable {

	private static final long serialVersionUID = 7083971450893323016L;
	private String name;
	private boolean includeAttributeStatement;
	private String signResponses;
	private String signAssertions;
	private String signRequests;
	private int assertionLifetime;
	private int assertionProxyCount;
	private String encryptNameIds;
	private String encryptAssertions;
	private String profileConfigurationCertFileName;
}
