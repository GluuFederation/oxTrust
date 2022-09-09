package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdpConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7181794652648330155L;
	@JsonProperty("idpLdapProtocol")
	private String idpLdapProtocol;
	@JsonProperty("idpLdapServer")
	private String idpLdapServer;
	@JsonProperty("idpBindDn")
	private String idpBindDn;
	@JsonProperty("idpBindPassword")
	private String idpBindPassword;
	@JsonProperty("idpUserFields")
	private String idpUserFields;
	@JsonProperty("idpUrl")
	private String idpUrl;
	@JsonProperty("idpSecurityKey")
	private String idpSecurityKey;
	@JsonProperty("idpSecurityKeyPassword")
	private String idpSecurityKeyPassword;
	@JsonProperty("idpSecurityCert")
	private String idpSecurityCert;
	@JsonProperty("idp3SigningCert")
	private String idp3SigningCert;
	@JsonProperty("idp3EncryptionCert")
	private String idp3EncryptionCert;
	@JsonProperty("shibbolethVersion")
	private String shibbolethVersion;
	@JsonProperty("shibboleth3IdpRootDir")
	private String shibboleth3IdpRootDir;
	@JsonProperty("shibboleth3SpConfDir")
	private String shibboleth3SpConfDir;
	@JsonProperty("shibboleth3FederationRootDir")
	private String shibboleth3FederationRootDir;
	@JsonProperty("idpLdapProtocol")
	public String getIdpLdapProtocol() {
		return idpLdapProtocol;
	}

	@JsonProperty("idpLdapProtocol")
	public void setIdpLdapProtocol(String idpLdapProtocol) {
		this.idpLdapProtocol = idpLdapProtocol;
	}

	@JsonProperty("idpLdapServer")
	public String getIdpLdapServer() {
		return idpLdapServer;
	}

	@JsonProperty("idpLdapServer")
	public void setIdpLdapServer(String idpLdapServer) {
		this.idpLdapServer = idpLdapServer;
	}

	@JsonProperty("idpBindDn")
	public String getIdpBindDn() {
		return idpBindDn;
	}

	@JsonProperty("idpBindDn")
	public void setIdpBindDn(String idpBindDn) {
		this.idpBindDn = idpBindDn;
	}

	@JsonProperty("idpBindPassword")
	public String getIdpBindPassword() {
		return idpBindPassword;
	}

	@JsonProperty("idpBindPassword")
	public void setIdpBindPassword(String idpBindPassword) {
		this.idpBindPassword = idpBindPassword;
	}

	@JsonProperty("idpUserFields")
	public String getIdpUserFields() {
		return idpUserFields;
	}

	@JsonProperty("idpUserFields")
	public void setIdpUserFields(String idpUserFields) {
		this.idpUserFields = idpUserFields;
	}

	@JsonProperty("idpSecurityKey")
	public String getIdpSecurityKey() {
		return idpSecurityKey;
	}

	@JsonProperty("idpSecurityKey")
	public void setIdpSecurityKey(String idpSecurityKey) {
		this.idpSecurityKey = idpSecurityKey;
	}

	@JsonProperty("idpSecurityKeyPassword")
	public String getIdpSecurityKeyPassword() {
		return idpSecurityKeyPassword;
	}

	@JsonProperty("idpSecurityKeyPassword")
	public void setIdpSecurityKeyPassword(String idpSecurityKeyPassword) {
		this.idpSecurityKeyPassword = idpSecurityKeyPassword;
	}

	@JsonProperty("idpSecurityCert")
	public String getIdpSecurityCert() {
		return idpSecurityCert;
	}

	@JsonProperty("idpSecurityCert")
	public void setIdpSecurityCert(String idpSecurityCert) {
		this.idpSecurityCert = idpSecurityCert;
	}

	@JsonProperty("idpUrl")
	public String getIdpUrl() {
		return idpUrl;
	}

	@JsonProperty("idpUrl")
	public void setIdpUrl(String idpUrl) {
		this.idpUrl = idpUrl;
	}

	@JsonProperty("idp3SigningCert")
	public String getIdp3SigningCert() {
		return idp3SigningCert;
	}

	@JsonProperty("idp3SigningCert")
	public void setIdp3SigningCert(String idp3SigningCert) {
		this.idp3SigningCert = idp3SigningCert;
	}

	@JsonProperty("idp3EncryptionCert")
	public String getIdp3EncryptionCert() {
		return idp3EncryptionCert;
	}

	@JsonProperty("idp3EncryptionCert")
	public void setIdp3EncryptionCert(String idp3EncryptionCert) {
		this.idp3EncryptionCert = idp3EncryptionCert;
	}

	@JsonProperty("shibbolethVersion")
	public String getShibbolethVersion() {
		return shibbolethVersion;
	}

	@JsonProperty("shibbolethVersion")
	public void setShibbolethVersion(String shibbolethVersion) {
		this.shibbolethVersion = shibbolethVersion;
	}

	@JsonProperty("shibboleth3IdpRootDir")
	public String getShibboleth3IdpRootDir() {
		return shibboleth3IdpRootDir;
	}

	@JsonProperty("shibboleth3IdpRootDir")
	public void setShibboleth3IdpRootDir(String shibboleth3IdpRootDir) {
		this.shibboleth3IdpRootDir = shibboleth3IdpRootDir;
	}

	@JsonProperty("shibboleth3SpConfDir")
	public String getShibboleth3SpConfDir() {
		return shibboleth3SpConfDir;
	}

	@JsonProperty("shibboleth3SpConfDir")
	public void setShibboleth3SpConfDir(String shibboleth3SpConfDir) {
		this.shibboleth3SpConfDir = shibboleth3SpConfDir;
	}
	@JsonProperty("shibboleth3FederationRootDir")
	public String getShibboleth3FederationRootDir() {
		return shibboleth3FederationRootDir;
	}

	@JsonProperty("shibboleth3FederationRootDir")
	public void setShibboleth3FederationRootDir(String shibboleth3FederationRootDir) {
		this.shibboleth3FederationRootDir = shibboleth3FederationRootDir;
	}

}
