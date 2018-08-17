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

	public void audit(String message, GluuCustomPerson user, HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR")
				: request.getRemoteAddr();
		String fullMessage = "+" + message.concat(" BY USER " + user.getDisplayName() + " FROM IP ADDRESS " + ipAddress
				+ " ON " + dayOfWeek + " " + format.format(new Date()) + " +");
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < fullMessage.length(); i++) {
			buffer.append("+");
		}
		log.info(buffer.toString());
		log.info(fullMessage);
	}

}
