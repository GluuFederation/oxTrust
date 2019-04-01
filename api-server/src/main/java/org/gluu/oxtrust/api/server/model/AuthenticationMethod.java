package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

public class AuthenticationMethod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1390824828571620660L;
	private String defaultAcr;
	private String oxtrustAcr;

	public String getDefaultAcr() {
		return defaultAcr;
	}

	public void setDefaultAcr(String defaultAcr) {
		this.defaultAcr = defaultAcr;
	}

	public String getOxtrustAcr() {
		return oxtrustAcr;
	}

	public void setOxtrustAcr(String oxtrustAcr) {
		this.oxtrustAcr = oxtrustAcr;
	}

}
