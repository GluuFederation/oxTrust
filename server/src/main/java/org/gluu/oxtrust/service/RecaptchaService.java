/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.util.RecaptchaUtil;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

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

		String recaptchaSecretKey = getRecaptchaSecretKey();
		boolean result = recaptchaUtil.verifyGoogleRecaptchaFromServletContext(recaptchaSecretKey);
		return result;
	}

	public boolean verifyRecaptchaResponse(String response) {
		boolean enabled = isEnabled();
		if (!enabled) {
			return false;
		}
		String recaptchaSecretKey = getRecaptchaSecretKey();
		boolean result = recaptchaUtil.verify(response, recaptchaSecretKey);
		log.info("Result:" + result);
		return result;
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
