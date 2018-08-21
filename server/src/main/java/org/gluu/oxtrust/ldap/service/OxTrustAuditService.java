package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.slf4j.Logger;

public class OxTrustAuditService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3495894016120236517L;
	@Inject
	private Logger log;
	private SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private String dayOfWeek = new SimpleDateFormat("EE").format(new Date());

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
	
	public void audit(String message) {
		log.info(message);
	}

}
