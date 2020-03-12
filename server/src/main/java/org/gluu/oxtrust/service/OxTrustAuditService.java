package org.gluu.oxtrust.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.slf4j.Logger;
@Stateless
public class OxTrustAuditService implements Serializable {

	private static final long serialVersionUID = -3495894016120236517L;

	@Inject
	private Logger log;

    public void audit(String message, GluuCustomPerson user, HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR")
                : request.getRemoteAddr();
        String fullMessage = message.concat(" BY USER " + user.getDisplayName() + " FROM IP ADDRESS " + ipAddress);
        log.info(fullMessage);
    }

    public void audit(String message) {
        log.info(message);
    }

}
