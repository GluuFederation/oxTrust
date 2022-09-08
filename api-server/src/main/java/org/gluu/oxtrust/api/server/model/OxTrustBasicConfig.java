package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OxTrustBasicConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3011359447811602714L;
	@JsonProperty("baseDN")
	private String baseDN;
	@JsonProperty("orgSupportEmail")
	private String orgSupportEmail;
	@JsonProperty("applicationUrl")
	private String applicationUrl;
	@JsonProperty("baseEndpoint")
	private String baseEndpoint;
	@JsonProperty("ldifStore")
	private String ldifStore;
	@JsonProperty("updateStatus")
	private Boolean updateStatus;
	@JsonProperty("keystorePath")
	private String keystorePath;
	@JsonProperty("allowPersonModification")
	private Boolean allowPersonModification;
	@JsonProperty("configGeneration")
	private Boolean configGeneration;

	@JsonProperty("gluuSpCert")
	private String gluuSpCert;
	@JsonProperty("certDir")
	private String certDir;
	@JsonProperty("servicesRestartTrigger")
	private String servicesRestartTrigger;

	@JsonProperty("loginRedirectUrl")
	private String loginRedirectUrl;
	@JsonProperty("logoutRedirectUrl")
	private String logoutRedirectUrl;
	@JsonProperty("clientAssociationAttribute")
	private String clientAssociationAttribute;
	@JsonProperty("ignoreValidation")
	private Boolean ignoreValidation;
	@JsonProperty("umaIssuer")
	private String umaIssuer;

	@JsonProperty("cssLocation")
	private String cssLocation;
	@JsonProperty("jsLocation")
	private String jsLocation;
	@JsonProperty("enableUpdateNotification")
	private Boolean enableUpdateNotification;

	@JsonProperty("oxIncommonFlag")
	private Boolean oxIncommonFlag;
	@JsonProperty("clientWhiteList")
	private List<String> clientWhiteList = null;
	@JsonProperty("clientBlackList")
	private List<String> clientBlackList = null;
	@JsonProperty("loggingLevel")
	private String loggingLevel;
	@JsonProperty("organizationName")
	private String organizationName;

	@JsonProperty("disableJdkLogger")
	private Boolean disableJdkLogger;
	@JsonProperty("passwordResetRequestExpirationTime")
	private Integer passwordResetRequestExpirationTime;
	@JsonProperty("cleanServiceInterval")
	private Integer cleanServiceInterval;
	@JsonProperty("enforceEmailUniqueness")
	private Boolean enforceEmailUniqueness;
	@JsonProperty("useLocalCache")
	private Boolean useLocalCache;

	@JsonProperty("baseDN")
	public String getBaseDN() {
		return baseDN;
	}

	@JsonProperty("baseDN")
	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}

	@JsonProperty("orgSupportEmail")
	public String getOrgSupportEmail() {
		return orgSupportEmail;
	}

	@JsonProperty("orgSupportEmail")
	public void setOrgSupportEmail(String orgSupportEmail) {
		this.orgSupportEmail = orgSupportEmail;
	}

	@JsonProperty("applicationUrl")
	public String getApplicationUrl() {
		return applicationUrl;
	}

	@JsonProperty("applicationUrl")
	public void setApplicationUrl(String applicationUrl) {
		this.applicationUrl = applicationUrl;
	}

	@JsonProperty("baseEndpoint")
	public String getBaseEndpoint() {
		return baseEndpoint;
	}

	@JsonProperty("baseEndpoint")
	public void setBaseEndpoint(String baseEndpoint) {
		this.baseEndpoint = baseEndpoint;
	}

	@JsonProperty("ldifStore")
	public String getLdifStore() {
		return ldifStore;
	}

	@JsonProperty("ldifStore")
	public void setLdifStore(String ldifStore) {
		this.ldifStore = ldifStore;
	}

	@JsonProperty("updateStatus")
	public Boolean getUpdateStatus() {
		return updateStatus;
	}

	@JsonProperty("updateStatus")
	public void setUpdateStatus(Boolean updateStatus) {
		this.updateStatus = updateStatus;
	}

	@JsonProperty("keystorePath")
	public String getKeystorePath() {
		return keystorePath;
	}

	@JsonProperty("keystorePath")
	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	@JsonProperty("allowPersonModification")
	public Boolean getAllowPersonModification() {
		return allowPersonModification;
	}

	@JsonProperty("allowPersonModification")
	public void setAllowPersonModification(Boolean allowPersonModification) {
		this.allowPersonModification = allowPersonModification;
	}

	@JsonProperty("configGeneration")
	public Boolean getConfigGeneration() {
		return configGeneration;
	}

	@JsonProperty("configGeneration")
	public void setConfigGeneration(Boolean configGeneration) {
		this.configGeneration = configGeneration;
	}

	@JsonProperty("gluuSpCert")
	public String getGluuSpCert() {
		return gluuSpCert;
	}

	@JsonProperty("gluuSpCert")
	public void setGluuSpCert(String gluuSpCert) {
		this.gluuSpCert = gluuSpCert;
	}

	@JsonProperty("certDir")
	public String getCertDir() {
		return certDir;
	}

	@JsonProperty("certDir")
	public void setCertDir(String certDir) {
		this.certDir = certDir;
	}

	@JsonProperty("servicesRestartTrigger")
	public String getServicesRestartTrigger() {
		return servicesRestartTrigger;
	}

	@JsonProperty("servicesRestartTrigger")
	public void setServicesRestartTrigger(String servicesRestartTrigger) {
		this.servicesRestartTrigger = servicesRestartTrigger;
	}

	@JsonProperty("loginRedirectUrl")
	public String getLoginRedirectUrl() {
		return loginRedirectUrl;
	}

	@JsonProperty("loginRedirectUrl")
	public void setLoginRedirectUrl(String loginRedirectUrl) {
		this.loginRedirectUrl = loginRedirectUrl;
	}

	@JsonProperty("logoutRedirectUrl")
	public String getLogoutRedirectUrl() {
		return logoutRedirectUrl;
	}

	@JsonProperty("logoutRedirectUrl")
	public void setLogoutRedirectUrl(String logoutRedirectUrl) {
		this.logoutRedirectUrl = logoutRedirectUrl;
	}

	@JsonProperty("clientAssociationAttribute")
	public String getClientAssociationAttribute() {
		return clientAssociationAttribute;
	}

	@JsonProperty("clientAssociationAttribute")
	public void setClientAssociationAttribute(String clientAssociationAttribute) {
		this.clientAssociationAttribute = clientAssociationAttribute;
	}

	@JsonProperty("ignoreValidation")
	public Boolean getIgnoreValidation() {
		return ignoreValidation;
	}

	@JsonProperty("ignoreValidation")
	public void setIgnoreValidation(Boolean ignoreValidation) {
		this.ignoreValidation = ignoreValidation;
	}

	@JsonProperty("umaIssuer")
	public String getUmaIssuer() {
		return umaIssuer;
	}

	@JsonProperty("umaIssuer")
	public void setUmaIssuer(String umaIssuer) {
		this.umaIssuer = umaIssuer;
	}

	@JsonProperty("cssLocation")
	public String getCssLocation() {
		return cssLocation;
	}

	@JsonProperty("cssLocation")
	public void setCssLocation(String cssLocation) {
		this.cssLocation = cssLocation;
	}

	@JsonProperty("jsLocation")
	public String getJsLocation() {
		return jsLocation;
	}

	@JsonProperty("jsLocation")
	public void setJsLocation(String jsLocation) {
		this.jsLocation = jsLocation;
	}

	@JsonProperty("enableUpdateNotification")
	public Boolean getEnableUpdateNotification() {
		return enableUpdateNotification;
	}

	@JsonProperty("enableUpdateNotification")
	public void setEnableUpdateNotification(Boolean enableUpdateNotification) {
		this.enableUpdateNotification = enableUpdateNotification;
	}

	@JsonProperty("oxIncommonFlag")
	public Boolean getOxIncommonFlag() {
		return oxIncommonFlag;
	}

	@JsonProperty("oxIncommonFlag")
	public void setOxIncommonFlag(Boolean oxIncommonFlag) {
		this.oxIncommonFlag = oxIncommonFlag;
	}

	@JsonProperty("clientWhiteList")
	public List<String> getClientWhiteList() {
		return clientWhiteList;
	}

	@JsonProperty("clientWhiteList")
	public void setClientWhiteList(List<String> clientWhiteList) {
		this.clientWhiteList = clientWhiteList;
	}

	@JsonProperty("clientBlackList")
	public List<String> getClientBlackList() {
		return clientBlackList;
	}

	@JsonProperty("clientBlackList")
	public void setClientBlackList(List<String> clientBlackList) {
		this.clientBlackList = clientBlackList;
	}

	@JsonProperty("loggingLevel")
	public String getLoggingLevel() {
		return loggingLevel;
	}

	@JsonProperty("loggingLevel")
	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	@JsonProperty("organizationName")
	public String getOrganizationName() {
		return organizationName;
	}

	@JsonProperty("organizationName")
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	@JsonProperty("disableJdkLogger")
	public Boolean getDisableJdkLogger() {
		return disableJdkLogger;
	}

	@JsonProperty("disableJdkLogger")
	public void setDisableJdkLogger(Boolean disableJdkLogger) {
		this.disableJdkLogger = disableJdkLogger;
	}

	@JsonProperty("passwordResetRequestExpirationTime")
	public Integer getPasswordResetRequestExpirationTime() {
		return passwordResetRequestExpirationTime;
	}

	@JsonProperty("passwordResetRequestExpirationTime")
	public void setPasswordResetRequestExpirationTime(Integer passwordResetRequestExpirationTime) {
		this.passwordResetRequestExpirationTime = passwordResetRequestExpirationTime;
	}

	@JsonProperty("cleanServiceInterval")
	public Integer getCleanServiceInterval() {
		return cleanServiceInterval;
	}

	@JsonProperty("cleanServiceInterval")
	public void setCleanServiceInterval(Integer cleanServiceInterval) {
		this.cleanServiceInterval = cleanServiceInterval;
	}

	@JsonProperty("enforceEmailUniqueness")
	public Boolean getEnforceEmailUniqueness() {
		return enforceEmailUniqueness;
	}

	@JsonProperty("enforceEmailUniqueness")
	public void setEnforceEmailUniqueness(Boolean enforceEmailUniqueness) {
		this.enforceEmailUniqueness = enforceEmailUniqueness;
	}

	@JsonProperty("useLocalCache")
	public Boolean getUseLocalCache() {
		return useLocalCache;
	}

	@JsonProperty("useLocalCache")
	public void setUseLocalCache(Boolean useLocalCache) {
		this.useLocalCache = useLocalCache;
	}
}
