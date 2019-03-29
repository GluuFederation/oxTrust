package org.gluu.oxtrust.api.server.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "attemptExpiration", "maximumAllowedAttemptsWithoutDelay", "delayTime",
		"bruteForceProtectionEnabled" })
public class AuthenticationProtectionConfiguration {

	@JsonProperty("attemptExpiration")
	private Integer attemptExpiration;
	@JsonProperty("maximumAllowedAttemptsWithoutDelay")
	private Integer maximumAllowedAttemptsWithoutDelay;
	@JsonProperty("delayTime")
	private Integer delayTime;
	@JsonProperty("bruteForceProtectionEnabled")
	private Boolean bruteForceProtectionEnabled;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("attemptExpiration")
	public Integer getAttemptExpiration() {
		return attemptExpiration;
	}

	@JsonProperty("attemptExpiration")
	public void setAttemptExpiration(Integer attemptExpiration) {
		this.attemptExpiration = attemptExpiration;
	}

	@JsonProperty("maximumAllowedAttemptsWithoutDelay")
	public Integer getMaximumAllowedAttemptsWithoutDelay() {
		return maximumAllowedAttemptsWithoutDelay;
	}

	@JsonProperty("maximumAllowedAttemptsWithoutDelay")
	public void setMaximumAllowedAttemptsWithoutDelay(Integer maximumAllowedAttemptsWithoutDelay) {
		this.maximumAllowedAttemptsWithoutDelay = maximumAllowedAttemptsWithoutDelay;
	}

	@JsonProperty("delayTime")
	public Integer getDelayTime() {
		return delayTime;
	}

	@JsonProperty("delayTime")
	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	@JsonProperty("bruteForceProtectionEnabled")
	public Boolean getBruteForceProtectionEnabled() {
		return bruteForceProtectionEnabled;
	}

	@JsonProperty("bruteForceProtectionEnabled")
	public void setBruteForceProtectionEnabled(Boolean bruteForceProtectionEnabled) {
		this.bruteForceProtectionEnabled = bruteForceProtectionEnabled;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
