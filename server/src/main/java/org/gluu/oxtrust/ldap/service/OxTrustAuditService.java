package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import javax.inject.Inject;

import org.slf4j.Logger;

public class OxTrustAuditService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3495894016120236517L;
	@Inject
	private Logger log;

	public void audit(String message) {
		log.info(message);
	}

}
