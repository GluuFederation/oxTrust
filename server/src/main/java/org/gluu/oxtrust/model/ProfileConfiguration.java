package org.gluu.oxtrust.model;

import java.io.Serializable;

import lombok.Data;

import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;


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
