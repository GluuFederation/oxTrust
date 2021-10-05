package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OxTrustJsonSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5109486261025785946L;

	private String orgName;
	private String supportEmail;
	private boolean authenticationRecaptchaEnabled;
	private boolean enforceEmailUniqueness;
	private String loggingLevel;
	private int passwordResetRequestExpirationTime;
	private int cleanServiceInterval;

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	public boolean isAuthenticationRecaptchaEnabled() {
		return authenticationRecaptchaEnabled;
	}

	public void setAuthenticationRecaptchaEnabled(boolean authenticationRecaptchaEnabled) {
		this.authenticationRecaptchaEnabled = authenticationRecaptchaEnabled;
	}

	public boolean isEnforceEmailUniqueness() {
		return enforceEmailUniqueness;
	}

	public void setEnforceEmailUniqueness(boolean enforceEmailUniqueness) {
		this.enforceEmailUniqueness = enforceEmailUniqueness;
	}

	public String getLoggingLevel() {
		return loggingLevel;
	}

	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	public int getPasswordResetRequestExpirationTime() {
		return passwordResetRequestExpirationTime;
	}

	public void setPasswordResetRequestExpirationTime(int passwordResetRequestExpirationTime) {
		this.passwordResetRequestExpirationTime = passwordResetRequestExpirationTime;
	}

	public int getCleanServiceInterval() {
		return cleanServiceInterval;
	}

	public void setCleanServiceInterval(int cleanServiceInterval) {
		this.cleanServiceInterval = cleanServiceInterval;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@Override
	public String toString() {
		return "OxTrustJsonSetting [orgName=" + orgName + ", supportEmail=" + supportEmail
				+ ", authenticationRecaptchaEnabled=" + authenticationRecaptchaEnabled
				+ ", enforceEmailUniqueness=" + enforceEmailUniqueness + ", loggingLevel=" + loggingLevel
				+ ", passwordResetRequestExpirationTime=" + passwordResetRequestExpirationTime
				+ ", cleanServiceInterval=" + cleanServiceInterval + "]";
	}

}
