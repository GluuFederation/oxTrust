package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassportConfig implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2324224944740305480L;
	@JsonProperty("passportUmaClientId")
	private String passportUmaClientId;
	@JsonProperty("passportUmaClientKeyId")
	private String passportUmaClientKeyId;
	@JsonProperty("passportUmaResourceId")
	private String passportUmaResourceId;
	@JsonProperty("passportUmaScope")
	private String passportUmaScope;
	@JsonProperty("passportUmaClientKeyStoreFile")
	private String passportUmaClientKeyStoreFile;
	@JsonProperty("passportUmaClientKeyStorePassword")
	private String passportUmaClientKeyStorePassword;

	@JsonProperty("passportUmaClientId")
	public String getPassportUmaClientId() {
		return passportUmaClientId;
	}

	@JsonProperty("passportUmaClientId")
	public void setPassportUmaClientId(String passportUmaClientId) {
		this.passportUmaClientId = passportUmaClientId;
	}

	@JsonProperty("passportUmaClientKeyId")
	public String getPassportUmaClientKeyId() {
		return passportUmaClientKeyId;
	}

	@JsonProperty("passportUmaClientKeyId")
	public void setPassportUmaClientKeyId(String passportUmaClientKeyId) {
		this.passportUmaClientKeyId = passportUmaClientKeyId;
	}

	@JsonProperty("passportUmaResourceId")
	public String getPassportUmaResourceId() {
		return passportUmaResourceId;
	}

	@JsonProperty("passportUmaResourceId")
	public void setPassportUmaResourceId(String passportUmaResourceId) {
		this.passportUmaResourceId = passportUmaResourceId;
	}

	@JsonProperty("passportUmaScope")
	public String getPassportUmaScope() {
		return passportUmaScope;
	}

	@JsonProperty("passportUmaScope")
	public void setPassportUmaScope(String passportUmaScope) {
		this.passportUmaScope = passportUmaScope;
	}

	@JsonProperty("passportUmaClientKeyStoreFile")
	public String getPassportUmaClientKeyStoreFile() {
		return passportUmaClientKeyStoreFile;
	}

	@JsonProperty("passportUmaClientKeyStoreFile")
	public void setPassportUmaClientKeyStoreFile(String passportUmaClientKeyStoreFile) {
		this.passportUmaClientKeyStoreFile = passportUmaClientKeyStoreFile;
	}

	@JsonProperty("passportUmaClientKeyStorePassword")
	public String getPassportUmaClientKeyStorePassword() {
		return passportUmaClientKeyStorePassword;
	}

	@JsonProperty("passportUmaClientKeyStorePassword")
	public void setPassportUmaClientKeyStorePassword(String passportUmaClientKeyStorePassword) {
		this.passportUmaClientKeyStorePassword = passportUmaClientKeyStorePassword;
	}

}
