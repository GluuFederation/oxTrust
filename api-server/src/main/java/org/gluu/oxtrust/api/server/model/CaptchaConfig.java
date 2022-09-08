package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptchaConfig implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2349398646365846965L;
	@JsonProperty("recaptchaSiteKey")
	private String recaptchaSiteKey;
	@JsonProperty("recaptchaSecretKey")
	private String recaptchaSecretKey;
	@JsonProperty("authenticationRecaptchaEnabled")
	private Boolean authenticationRecaptchaEnabled;
	
	
	
	
	@JsonProperty("recaptchaSiteKey")
	public String getRecaptchaSiteKey() {
		return recaptchaSiteKey;
	}

	@JsonProperty("recaptchaSiteKey")
	public void setRecaptchaSiteKey(String recaptchaSiteKey) {
		this.recaptchaSiteKey = recaptchaSiteKey;
	}

	@JsonProperty("recaptchaSecretKey")
	public String getRecaptchaSecretKey() {
		return recaptchaSecretKey;
	}

	@JsonProperty("recaptchaSecretKey")
	public void setRecaptchaSecretKey(String recaptchaSecretKey) {
		this.recaptchaSecretKey = recaptchaSecretKey;
	}

	@JsonProperty("authenticationRecaptchaEnabled")
	public Boolean getAuthenticationRecaptchaEnabled() {
		return authenticationRecaptchaEnabled;
	}

	@JsonProperty("authenticationRecaptchaEnabled")
	public void setAuthenticationRecaptchaEnabled(Boolean authenticationRecaptchaEnabled) {
		this.authenticationRecaptchaEnabled = authenticationRecaptchaEnabled;
	}

}
