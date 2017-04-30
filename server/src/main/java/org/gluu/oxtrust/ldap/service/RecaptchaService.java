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
import org.jboss.seam.annotations.AutoCreate;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.log.Log;
import org.xdi.util.StringHelper;

/**
 * @author Dejan Maric
 * @author Yuriy Movchan
 */
@Scope(ScopeType.STATELESS)
@Named("recaptchaService")
@AutoCreate
public class RecaptchaService implements Serializable {
	
	@Inject
	private static OxTrustConfiguration oxTrustConfiguration;
	
	@Logger
	private static Log log;

	private static final long serialVersionUID = 7725720511230443399L;

	public boolean verifyRecaptchaResponse() {
		boolean enabled = isEnabled();
		if (!enabled) {
			return false;
		}

		return RecaptchaUtils.verifyGoogleRecaptchaFromServletContext(getRecaptchaSecretKey());
	}

	public boolean isEnabled() {
		return StringHelper.isNotEmpty(getRecaptchaSecretKey()) && StringHelper.isNotEmpty(getRecaptchaSiteKey());
	}

	public String getRecaptchaSecretKey() {
		return oxTrustConfiguration.getApplicationConfiguration().getRecaptchaSecretKey();
	}

	public String getRecaptchaSiteKey() {
		return oxTrustConfiguration.getApplicationConfiguration().getRecaptchaSiteKey();
	}

}
