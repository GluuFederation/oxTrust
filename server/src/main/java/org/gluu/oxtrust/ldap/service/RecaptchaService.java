/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.util.RecaptchaUtil;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.StringHelper;

/**
 * @author Dejan Maric
 * @author Yuriy Movchan
 */
@Stateless
@Named("recaptchaService")
public class RecaptchaService implements Serializable {

	@Inject
	private AppConfiguration appConfiguration;
	
	@Inject
	private RecaptchaUtil recaptchaUtil;

	@Inject
	private Logger log;

	private static final long serialVersionUID = 7725720511230443399L;

	public boolean verifyRecaptchaResponse() {
		boolean enabled = isEnabled();
		if (!enabled) {
			return false;
		}

		return recaptchaUtil.verifyGoogleRecaptchaFromServletContext(getRecaptchaSecretKey());
	}

	public boolean isEnabled() {
		return StringHelper.isNotEmpty(getRecaptchaSecretKey()) && StringHelper.isNotEmpty(getRecaptchaSiteKey());
	}

	public String getRecaptchaSecretKey() {
		return appConfiguration.getRecaptchaSecretKey();
	}

	public String getRecaptchaSiteKey() {
		return appConfiguration.getRecaptchaSiteKey();
	}

}
