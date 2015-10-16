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

import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;

/**
 * @author "Oleksiy Tataryn"
 *
 */

public class RegistrationConfiguration {
	
	@LdapJsonObject
	private List<RegistrationInterceptorScript> registrationInterceptorScripts;
	
	@LdapJsonObject
	private boolean registrationInterceptorsConfigured;
	
	//unused
	@LdapJsonObject
	private boolean inboundSAMLRegistrationAllowed;
		
	@LdapJsonObject
	private List<String> additionalAttributes;

	
	@LdapJsonObject
	private boolean isCaptchaDisabled;

    public List<String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(List<String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public boolean isInboundSAMLRegistrationAllowed() {
        return inboundSAMLRegistrationAllowed;
    }

    public void setInboundSAMLRegistrationAllowed(boolean inboundSAMLRegistrationAllowed) {
        this.inboundSAMLRegistrationAllowed = inboundSAMLRegistrationAllowed;
    }

    public boolean isCaptchaDisabled() {
        return isCaptchaDisabled;
    }

    public void setCaptchaDisabled(boolean captchaDisabled) {
        isCaptchaDisabled = captchaDisabled;
    }

    public boolean isRegistrationInterceptorsConfigured() {
        return registrationInterceptorsConfigured;
    }

    public void setRegistrationInterceptorsConfigured(boolean registrationInterceptorsConfigured) {
        this.registrationInterceptorsConfigured = registrationInterceptorsConfigured;
    }

    public List<RegistrationInterceptorScript> getRegistrationInterceptorScripts() {
        return registrationInterceptorScripts;
    }

    public void setRegistrationInterceptorScripts(List<RegistrationInterceptorScript> registrationInterceptorScripts) {
        this.registrationInterceptorScripts = registrationInterceptorScripts;
    }

}
