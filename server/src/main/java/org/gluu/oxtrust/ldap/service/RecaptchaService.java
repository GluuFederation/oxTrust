/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.util.RecaptchaUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.EVENT)
@Name("recaptcha")
public class RecaptchaService implements Serializable {
	
	@In
	private static OxTrustConfiguration oxTrustConfiguration;
	
	@Logger
	private static Log log;

	private static final long serialVersionUID = 7725720511230443399L;

	public String getHtml() throws Exception {
		return RecaptchaUtils.createRecaptchaHtml("Error");
	}
	
	public boolean getRecaptchaResponse() {
		RecaptchaUtils recaptchaUtils = new RecaptchaUtils();
		return recaptchaUtils.getGoogleRecaptchaFromServletContext(oxTrustConfiguration.getApplicationConfiguration().getRecaptchUrl(),oxTrustConfiguration.getApplicationConfiguration().getRecaptchaSecretKey());
	}

}
