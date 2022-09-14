package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.gluu.config.oxtrust.ScimMode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5623097199628878764L;

	@JsonProperty("scimUmaClientId")
	private String scimUmaClientId;
	@JsonProperty("scimUmaClientKeyId")
	private String scimUmaClientKeyId;
	@JsonProperty("scimUmaResourceId")
	private String scimUmaResourceId;
	@JsonProperty("scimUmaScope")
	private String scimUmaScope;
	@JsonProperty("scimUmaClientKeyStoreFile")
	private String scimUmaClientKeyStoreFile;
	@JsonProperty("scimUmaClientKeyStorePassword")
	private String scimUmaClientKeyStorePassword;
	@JsonProperty("scimMaxCount")
	private Integer scimMaxCount;
	@JsonProperty("scimProtectionMode")
	private ScimMode scimProtectionMode;
	@JsonProperty("userExtensionSchemaURI")
	private String userExtensionSchemaURI;

	@JsonProperty("scimUmaClientId")
	public String getScimUmaClientId() {
		return scimUmaClientId;
	}

	@JsonProperty("scimUmaClientId")
	public void setScimUmaClientId(String scimUmaClientId) {
		this.scimUmaClientId = scimUmaClientId;
	}

	@JsonProperty("scimUmaClientKeyId")
	public String getScimUmaClientKeyId() {
		return scimUmaClientKeyId;
	}

	@JsonProperty("scimUmaClientKeyId")
	public void setScimUmaClientKeyId(String scimUmaClientKeyId) {
		this.scimUmaClientKeyId = scimUmaClientKeyId;
	}

	@JsonProperty("scimUmaResourceId")
	public String getScimUmaResourceId() {
		return scimUmaResourceId;
	}

	@JsonProperty("scimUmaResourceId")
	public void setScimUmaResourceId(String scimUmaResourceId) {
		this.scimUmaResourceId = scimUmaResourceId;
	}

	@JsonProperty("scimUmaScope")
	public String getScimUmaScope() {
		return scimUmaScope;
	}

	@JsonProperty("scimUmaScope")
	public void setScimUmaScope(String scimUmaScope) {
		this.scimUmaScope = scimUmaScope;
	}

	@JsonProperty("scimUmaClientKeyStoreFile")
	public String getScimUmaClientKeyStoreFile() {
		return scimUmaClientKeyStoreFile;
	}

	@JsonProperty("scimUmaClientKeyStoreFile")
	public void setScimUmaClientKeyStoreFile(String scimUmaClientKeyStoreFile) {
		this.scimUmaClientKeyStoreFile = scimUmaClientKeyStoreFile;
	}

	@JsonProperty("scimUmaClientKeyStorePassword")
	public String getScimUmaClientKeyStorePassword() {
		return scimUmaClientKeyStorePassword;
	}

	@JsonProperty("scimUmaClientKeyStorePassword")
	public void setScimUmaClientKeyStorePassword(String scimUmaClientKeyStorePassword) {
		this.scimUmaClientKeyStorePassword = scimUmaClientKeyStorePassword;
	}
	
	public ScimMode getScimProtectionMode() {
		return scimProtectionMode;
	}

	public void setScimProtectionMode(ScimMode scimProtectionMode) {
		this.scimProtectionMode = scimProtectionMode;
	}
	
	public Integer getScimMaxCount() {
		return scimMaxCount;
	}

	public void setScimMaxCount(int scimMaxCount) {
		this.scimMaxCount = scimMaxCount;
	}

	public String getUserExtensionSchemaURI() {
		return userExtensionSchemaURI;
	}

	public void setUserExtensionSchemaURI(String userExtensionSchemaURI) {
		this.userExtensionSchemaURI = userExtensionSchemaURI;
	}

}
