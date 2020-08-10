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
@JsonPropertyOrder({ "filterName", "corsAllowedOrigins", "corsAllowedMethods", "corsAllowedHeaders",
		"corsExposedHeaders", "corsSupportCredentials", "corsLoggingEnabled", "corsPreflightMaxAge",
		"corsRequestDecorate" })
public class CorsConfigurationFilter {

	@JsonProperty("filterName")
	private String filterName;
	@JsonProperty("corsAllowedOrigins")
	private String corsAllowedOrigins;
	@JsonProperty("corsAllowedMethods")
	private String corsAllowedMethods;
	@JsonProperty("corsAllowedHeaders")
	private String corsAllowedHeaders;
	@JsonProperty("corsExposedHeaders")
	private String corsExposedHeaders;
	@JsonProperty("corsSupportCredentials")
	private Boolean corsSupportCredentials;
	@JsonProperty("corsLoggingEnabled")
	private Boolean corsLoggingEnabled;
	@JsonProperty("corsPreflightMaxAge")
	private Integer corsPreflightMaxAge;
	@JsonProperty("corsRequestDecorate")
	private Boolean corsRequestDecorate;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("filterName")
	public String getFilterName() {
		return filterName;
	}

	@JsonProperty("filterName")
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	@JsonProperty("corsAllowedOrigins")
	public String getCorsAllowedOrigins() {
		return corsAllowedOrigins;
	}

	@JsonProperty("corsAllowedOrigins")
	public void setCorsAllowedOrigins(String corsAllowedOrigins) {
		this.corsAllowedOrigins = corsAllowedOrigins;
	}

	@JsonProperty("corsAllowedMethods")
	public String getCorsAllowedMethods() {
		return corsAllowedMethods;
	}

	@JsonProperty("corsAllowedMethods")
	public void setCorsAllowedMethods(String corsAllowedMethods) {
		this.corsAllowedMethods = corsAllowedMethods;
	}

	@JsonProperty("corsAllowedHeaders")
	public String getCorsAllowedHeaders() {
		return corsAllowedHeaders;
	}

	@JsonProperty("corsAllowedHeaders")
	public void setCorsAllowedHeaders(String corsAllowedHeaders) {
		this.corsAllowedHeaders = corsAllowedHeaders;
	}

	@JsonProperty("corsExposedHeaders")
	public String getCorsExposedHeaders() {
		return corsExposedHeaders;
	}

	@JsonProperty("corsExposedHeaders")
	public void setCorsExposedHeaders(String corsExposedHeaders) {
		this.corsExposedHeaders = corsExposedHeaders;
	}

	@JsonProperty("corsSupportCredentials")
	public Boolean getCorsSupportCredentials() {
		return corsSupportCredentials;
	}

	@JsonProperty("corsSupportCredentials")
	public void setCorsSupportCredentials(Boolean corsSupportCredentials) {
		this.corsSupportCredentials = corsSupportCredentials;
	}

	@JsonProperty("corsLoggingEnabled")
	public Boolean getCorsLoggingEnabled() {
		return corsLoggingEnabled;
	}

	@JsonProperty("corsLoggingEnabled")
	public void setCorsLoggingEnabled(Boolean corsLoggingEnabled) {
		this.corsLoggingEnabled = corsLoggingEnabled;
	}

	@JsonProperty("corsPreflightMaxAge")
	public Integer getCorsPreflightMaxAge() {
		return corsPreflightMaxAge;
	}

	@JsonProperty("corsPreflightMaxAge")
	public void setCorsPreflightMaxAge(Integer corsPreflightMaxAge) {
		this.corsPreflightMaxAge = corsPreflightMaxAge;
	}

	@JsonProperty("corsRequestDecorate")
	public Boolean getCorsRequestDecorate() {
		return corsRequestDecorate;
	}

	@JsonProperty("corsRequestDecorate")
	public void setCorsRequestDecorate(Boolean corsRequestDecorate) {
		this.corsRequestDecorate = corsRequestDecorate;
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