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
@JsonPropertyOrder({ "authenticatorCertsFolder", "mdsCertsFolder", "mdsTocsFolder", "serverMetadataFolder",
		"checkU2fAttestations", "userAutoEnrollment", "unfinishedRequestExpiration", "authenticationHistoryExpiration",
		"disableFido2" })
public class Fido2Configuration {

	@JsonProperty("authenticatorCertsFolder")
	private String authenticatorCertsFolder;
	@JsonProperty("mdsCertsFolder")
	private String mdsCertsFolder;
	@JsonProperty("mdsTocsFolder")
	private String mdsTocsFolder;
	@JsonProperty("serverMetadataFolder")
	private String serverMetadataFolder;
	@JsonProperty("checkU2fAttestations")
	private Boolean checkU2fAttestations;
	@JsonProperty("userAutoEnrollment")
	private Boolean userAutoEnrollment;
	@JsonProperty("unfinishedRequestExpiration")
	private Integer unfinishedRequestExpiration;
	@JsonProperty("authenticationHistoryExpiration")
	private Integer authenticationHistoryExpiration;
	@JsonProperty("disableFido2")
	private Boolean disableFido2;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("authenticatorCertsFolder")
	public String getAuthenticatorCertsFolder() {
		return authenticatorCertsFolder;
	}

	@JsonProperty("authenticatorCertsFolder")
	public void setAuthenticatorCertsFolder(String authenticatorCertsFolder) {
		this.authenticatorCertsFolder = authenticatorCertsFolder;
	}

	@JsonProperty("mdsCertsFolder")
	public String getMdsCertsFolder() {
		return mdsCertsFolder;
	}

	@JsonProperty("mdsCertsFolder")
	public void setMdsCertsFolder(String mdsCertsFolder) {
		this.mdsCertsFolder = mdsCertsFolder;
	}

	@JsonProperty("mdsTocsFolder")
	public String getMdsTocsFolder() {
		return mdsTocsFolder;
	}

	@JsonProperty("mdsTocsFolder")
	public void setMdsTocsFolder(String mdsTocsFolder) {
		this.mdsTocsFolder = mdsTocsFolder;
	}

	@JsonProperty("serverMetadataFolder")
	public String getServerMetadataFolder() {
		return serverMetadataFolder;
	}

	@JsonProperty("serverMetadataFolder")
	public void setServerMetadataFolder(String serverMetadataFolder) {
		this.serverMetadataFolder = serverMetadataFolder;
	}

	@JsonProperty("checkU2fAttestations")
	public Boolean getCheckU2fAttestations() {
		return checkU2fAttestations;
	}

	@JsonProperty("checkU2fAttestations")
	public void setCheckU2fAttestations(Boolean checkU2fAttestations) {
		this.checkU2fAttestations = checkU2fAttestations;
	}

	@JsonProperty("userAutoEnrollment")
	public Boolean getUserAutoEnrollment() {
		return userAutoEnrollment;
	}

	@JsonProperty("userAutoEnrollment")
	public void setUserAutoEnrollment(Boolean userAutoEnrollment) {
		this.userAutoEnrollment = userAutoEnrollment;
	}

	@JsonProperty("unfinishedRequestExpiration")
	public Integer getUnfinishedRequestExpiration() {
		return unfinishedRequestExpiration;
	}

	@JsonProperty("unfinishedRequestExpiration")
	public void setUnfinishedRequestExpiration(Integer unfinishedRequestExpiration) {
		this.unfinishedRequestExpiration = unfinishedRequestExpiration;
	}

	@JsonProperty("authenticationHistoryExpiration")
	public Integer getAuthenticationHistoryExpiration() {
		return authenticationHistoryExpiration;
	}

	@JsonProperty("authenticationHistoryExpiration")
	public void setAuthenticationHistoryExpiration(Integer authenticationHistoryExpiration) {
		this.authenticationHistoryExpiration = authenticationHistoryExpiration;
	}

	@JsonProperty("disableFido2")
	public Boolean getDisableFido2() {
		return disableFido2;
	}

	@JsonProperty("disableFido2")
	public void setDisableFido2(Boolean disableFido2) {
		this.disableFido2 = disableFido2;
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
