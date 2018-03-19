/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author "Oleksiy Tataryn"
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationConfiguration {
		
	private List<String> additionalAttributes;

	private boolean isCaptchaDisabled;

    public List<String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(List<String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public boolean isCaptchaDisabled() {
        return isCaptchaDisabled;
    }

    public void setCaptchaDisabled(boolean captchaDisabled) {
        isCaptchaDisabled = captchaDisabled;
    }

}
