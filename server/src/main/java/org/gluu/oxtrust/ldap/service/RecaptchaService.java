/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import org.gluu.oxtrust.util.RecaptchaUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.EVENT)
@Name("recaptcha")
public class RecaptchaService implements Serializable {

	private static final long serialVersionUID = 7725720511230443399L;

	public String getHtml() throws Exception {
		return RecaptchaUtils.createRecaptchaHtml("Error");
	}

}
