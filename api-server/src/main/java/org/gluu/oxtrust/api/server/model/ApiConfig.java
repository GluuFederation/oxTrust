package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiConfig implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6623237663074231672L;
	@JsonProperty("apiUmaClientId")
	private String apiUmaClientId;
	@JsonProperty("apiUmaClientKeyId")
	private String apiUmaClientKeyId;
	@JsonProperty("apiUmaResourceId")
	private String apiUmaResourceId;
	@JsonProperty("apiUmaScopes")
	private String[] apiUmaScopes = null;
	@JsonProperty("apiUmaClientKeyStoreFile")
	private String apiUmaClientKeyStoreFile;
	@JsonProperty("apiUmaClientKeyStorePassword")
	private String apiUmaClientKeyStorePassword;
	@JsonProperty("oxTrustApiTestMode")
	private Boolean oxTrustApiTestMode;
	
	
	
	@JsonProperty("apiUmaClientId")
	public String getApiUmaClientId() {
		return apiUmaClientId;
	}

	@JsonProperty("apiUmaClientId")
	public void setApiUmaClientId(String apiUmaClientId) {
		this.apiUmaClientId = apiUmaClientId;
	}

	@JsonProperty("apiUmaClientKeyId")
	public String getApiUmaClientKeyId() {
		return apiUmaClientKeyId;
	}

	@JsonProperty("apiUmaClientKeyId")
	public void setApiUmaClientKeyId(String apiUmaClientKeyId) {
		this.apiUmaClientKeyId = apiUmaClientKeyId;
	}

	@JsonProperty("apiUmaResourceId")
	public String getApiUmaResourceId() {
		return apiUmaResourceId;
	}

	@JsonProperty("apiUmaResourceId")
	public void setApiUmaResourceId(String apiUmaResourceId) {
		this.apiUmaResourceId = apiUmaResourceId;
	}

	@JsonProperty("apiUmaScopes")
	public String[] getApiUmaScopes() {
		return apiUmaScopes;
	}

	@JsonProperty("apiUmaScopes")
	public void setApiUmaScopes(String[] strings) {
		this.apiUmaScopes = strings;
	}

	@JsonProperty("apiUmaClientKeyStoreFile")
	public String getApiUmaClientKeyStoreFile() {
		return apiUmaClientKeyStoreFile;
	}
	@JsonProperty("apiUmaClientKeyStoreFile")
	public void setApiUmaClientKeyStoreFile(String apiUmaClientKeyStoreFile) {
		this.apiUmaClientKeyStoreFile = apiUmaClientKeyStoreFile;
	}

	@JsonProperty("apiUmaClientKeyStorePassword")
	public String getApiUmaClientKeyStorePassword() {
		return apiUmaClientKeyStorePassword;
	}

	@JsonProperty("apiUmaClientKeyStorePassword")
	public void setApiUmaClientKeyStorePassword(String apiUmaClientKeyStorePassword) {
		this.apiUmaClientKeyStorePassword = apiUmaClientKeyStorePassword;
	}
	@JsonProperty("oxTrustApiTestMode")
	public Boolean getOxTrustApiTestMode() {
		return oxTrustApiTestMode;
	}

	@JsonProperty("oxTrustApiTestMode")
	public void setOxTrustApiTestMode(Boolean oxTrustApiTestMode) {
		this.oxTrustApiTestMode = oxTrustApiTestMode;
	}

}
