/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

import lombok.Data;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;

import java.util.List;

/**
 * @author "Oleksiy Tataryn"
 *
 */

public @Data class RegistrationConfiguration {
	
	@LdapJsonObject
	private List<RegistrationInterceptorScript> registrationInterceptorScripts;
	
	@LdapJsonObject
	private boolean registrationInterceptorsConfigured;
	
	@LdapJsonObject
	private boolean invitationCodesManagementEnabled;
	
	@LdapJsonObject
	private boolean uninvitedRegistrationAllowed;
	
	//unused
	@LdapJsonObject
	private boolean inboundSAMLRegistrationAllowed;
	
	@LdapJsonObject
	private boolean accountsTimeLimited;
	
	@LdapJsonObject
	private String accountsExpirationPeriod;

	@LdapJsonObject
	private String accountsExpirationServiceFrequency;
	
	@LdapJsonObject
	private String linksExpirationFrequency;
	
	@LdapJsonObject
	private List<String> additionalAttributes;

	
	@LdapJsonObject
	private boolean isCaptchaDisabled;

    public String getAccountsExpirationPeriod() {
        return accountsExpirationPeriod;
    }

    public void setAccountsExpirationPeriod(String accountsExpirationPeriod) {
        this.accountsExpirationPeriod = accountsExpirationPeriod;
    }

    public String getAccountsExpirationServiceFrequency() {
        return accountsExpirationServiceFrequency;
    }

    public void setAccountsExpirationServiceFrequency(String accountsExpirationServiceFrequency) {
        this.accountsExpirationServiceFrequency = accountsExpirationServiceFrequency;
    }

    public boolean isAccountsTimeLimited() {
        return accountsTimeLimited;
    }

    public void setAccountsTimeLimited(boolean accountsTimeLimited) {
        this.accountsTimeLimited = accountsTimeLimited;
    }

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

    public boolean isInvitationCodesManagementEnabled() {
        return invitationCodesManagementEnabled;
    }

    public void setInvitationCodesManagementEnabled(boolean invitationCodesManagementEnabled) {
        this.invitationCodesManagementEnabled = invitationCodesManagementEnabled;
    }

    public boolean isCaptchaDisabled() {
        return isCaptchaDisabled;
    }

    public void setCaptchaDisabled(boolean captchaDisabled) {
        isCaptchaDisabled = captchaDisabled;
    }

    public String getLinksExpirationFrequency() {
        return linksExpirationFrequency;
    }

    public void setLinksExpirationFrequency(String linksExpirationFrequency) {
        this.linksExpirationFrequency = linksExpirationFrequency;
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

    public boolean isUninvitedRegistrationAllowed() {
        return uninvitedRegistrationAllowed;
    }

    public void setUninvitedRegistrationAllowed(boolean uninvitedRegistrationAllowed) {
        this.uninvitedRegistrationAllowed = uninvitedRegistrationAllowed;
    }
}
