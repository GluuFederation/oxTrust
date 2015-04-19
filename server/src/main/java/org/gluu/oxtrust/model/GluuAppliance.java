/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.log4j.Logger;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.model.cert.TrustStoreConfiguration;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.ldap.model.InumEntry;
import org.xdi.model.SmtpConfiguration;
import org.xdi.util.security.StringEncrypter;

import javax.persistence.Transient;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * GluuAppliance
 * 
 * @author Reda Zerrad Date: 08.10.2012
 * @author Yuriy Movchan Date: 04/20/2014
 */
@LdapEntry
@LdapObjectClass(values = { "top", "gluuAppliance" })
@EqualsAndHashCode(callSuper=false)
@Data
public class GluuAppliance extends InumEntry implements Serializable {

	private static final long serialVersionUID = -1817003894646725601L;

	private static final Logger log = Logger.getLogger(GluuAppliance.class);

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inumFN;

	@LdapAttribute
	private String iname;

	@LdapAttribute
	private String description;

	@LdapAttribute
	private String displayName;

	@LdapAttribute(name = "o")
	private String owner;

	@LdapAttribute(name = "c")
	private String country;

	@LdapAttribute(name = "gluuFreeDiskSpace", updateOnly = true)
	private String freeDiskSpace;

	@LdapAttribute(name = "gluuFreeMemory", updateOnly = true)
	private String freeMemory;

	@LdapAttribute(name = "gluuFreeSwap", updateOnly = true)
	private String freeSwap;

	@LdapAttribute(name = "gluuGroupCount", updateOnly = true)
	private String groupCount;

	@LdapAttribute(name = "gluuHostname", updateOnly = true)
	private String hostname;

	@LdapAttribute(name = "gluuIpAddress", updateOnly = true)
	private String ipAddress;

	@LdapAttribute(name = "gluuPersonCount", updateOnly = true)
	private String personCount;

	@LdapAttribute(name = "gluuShibAssertionsIssued")
	private String shibAssertionsIssued;

	@LdapAttribute(name = "gluuShibFailedAuth")
	private String shibFailedAuth;

	@LdapAttribute(name = "gluuShibSecurityEvents")
	private String shibSecurityEvents;

	@LdapAttribute(name = "gluuShibSuccessfulAuths")
	private String shibSuccessfulAuths;

	@LdapAttribute(name = "gluuSystemUptime", updateOnly = true)
	private String systemUptime;

	@LdapAttribute(name = "gluuLastUpdate", updateOnly = true)
	private String lastUpdate;

	@LdapAttribute(name = "gluuAppliancePollingInterval")
	private String pollingInterval;

	@LdapAttribute(name = "gluuStatus", updateOnly = true)
	private GluuStatus status;

	@LdapAttribute(name = "userPassword", ignoreDuringRead = true)
	private String userPassword;

	@LdapAttribute(name = "blowfishPassword")
	private String blowfishPassword;

	@LdapAttribute(name = "gluuHTTPstatus", updateOnly = true)
	private String gluuHttpStatus;

	@LdapAttribute(name = "gluuDSstatus", updateOnly = true)
	private String gluuDSStatus;

	@LdapAttribute(name = "gluuVDSstatus", updateOnly = true)
	private String gluuVDSStatus;

	@LdapAttribute(name = "gluuBandwidthTX", updateOnly = true)
	private String gluuBandwidthTX;

	@LdapAttribute(name = "gluuBandwidthRX", updateOnly = true)
	private String gluuBandwidthRX;

	@LdapAttribute(name = "gluuSPTR")
	private String gluuSPTR;

	@LdapAttribute(name = "gluuSslExpiry", updateOnly = true)
	private String sslExpiry;

	@LdapAttribute(name = "gluuOrgProfileMgt")
	private GluuBoolean profileManagment;

	@LdapAttribute(name = "gluuWhitePagesEnabled")
	private GluuBoolean whitePagesEnabled;

	@LdapAttribute(name = "gluuFederationHostingEnabled")
	private GluuBoolean federationHostingEnabled;

	@LdapAttribute(name = "gluuManageIdentityPermission")
	private GluuBoolean manageIdentityPermission;

	@LdapAttribute(name = "gluuVdsCacheRefreshEnabled")
	private GluuBoolean vdsCacheRefreshEnabled;

	@LdapAttribute(name = "oxTrustCacheRefreshServerIpAddress")
	private String cacheRefreshServerIpAddress;

	@LdapAttribute(name = "gluuVdsCacheRefreshPollingInterval")
	private String vdsCacheRefreshPollingInterval;

	@LdapAttribute(name = "gluuVdsCacheRefreshLastUpdate")
	private String vdsCacheRefreshLastUpdate;

	@LdapAttribute(name = "gluuVdsCacheRefreshLastUpdateCount")
	private String vdsCacheRefreshLastUpdateCount;

	@LdapAttribute(name = "gluuVdsCacheRefreshProblemCount")
	private String vdsCacheRefreshProblemCount;

	@LdapAttribute(name = "gluuScimEnabled")
	private GluuBoolean scimEnabled;

	@LdapAttribute(name = "gluuCentreonEmail")
	private String centreonEmail;

	@LdapAttribute(name = "gluuJiraEmail")
	private String jiraEmail;

	@LdapAttribute(name = "gluuBillingEmail")
	private String billingEmail;

	@LdapAttribute(name = "gluuPrivacyEmail")
	private String privacyEmail;

	@LdapAttribute(name = "gluuSvnEmail")
	private String svnEmail;

	@LdapAttribute(name = "gluuSmtpHost")
	private String smtpHost;

	@LdapAttribute(name = "gluuSmtpFromName")
	private String smtpFromName;

	@LdapAttribute(name = "gluuSmtpFromEmailAddress")
	private String smtpFromEmailAddress;

	@LdapAttribute(name = "gluuSmtpRequiresAuthentication")
	private String smtpRequiresAuthentication;

	@LdapAttribute(name = "gluuSmtpUserName")
	private String smtpUserName;

	@LdapAttribute(name = "gluuSmtpPassword")
	private String smtpPassword;

	@Transient
	private String smtpPasswordStr;

	@LdapAttribute(name = "gluuSmtpRequiresSsl")
	private String smtpRequiresSsl;

	@LdapAttribute(name = "gluuSmtpPort")
	private String smtpPort;

	@LdapAttribute(name = "oxSmtpConfiguration")
	@LdapJsonObject
	private SmtpConfiguration smtpConfiguration;

	@LdapAttribute(name = "gluuApplianceDnsServer")
	private String applianceDnsServer;

	@Min(value = 200)
	@LdapAttribute(name = "gluuMaxLogSize")
	private String maxLogSize;

	@LdapAttribute(name = "gluuLoadAvg", updateOnly = true)
	private String loadAvg;

	@LdapAttribute(name = "oxIDPAuthentication")
	@LdapJsonObject
	private List<OxIDPAuthConf> oxIDPAuthentication;

	@LdapAttribute(name = "oxAuthenticationMode")
	private String authenticationMode;

	@LdapAttribute(name = "oxAuthenticationLevel")
	private String authenticationLevel;

	@LdapAttribute(name = "oxClusteredServers")
	private List<String> oxClusterPartners;

	@LdapAttribute(name = "oxClusterType")
	private String clusterType;

	@LdapAttribute(name = "oxMemcachedServerAddress")	
	private String memcachedServerAddress;

	@LdapAttribute(name = "oxLogViewerConfig")
	private String oxLogViewerConfig;

	@LdapAttribute(name = "passwordResetAllowed")
	private GluuBoolean passwordResetAllowed;

	@LdapAttribute(name = "oxTrustStoreConf")
	@LdapJsonObject
	private TrustStoreConfiguration trustStoreConfiguration;

	@LdapAttribute(name = "oxTrustStoreCert")
	@LdapJsonObject
	private List<TrustStoreCertificate> trustStoreCertificates;

	public Date getLastUpdateDate() {
		try {
			return (lastUpdate == null) ? null : new Date(Long.valueOf(lastUpdate) * 1000);
		} catch (NumberFormatException ex) {
		}
		

		return null;
	}

	public Date getVdsCacheRefreshLastUpdateDate() {
		try {
			return (vdsCacheRefreshLastUpdate == null) ? null : new Date(Long.valueOf(vdsCacheRefreshLastUpdate) * 1000);
		} catch (NumberFormatException ex) {
		}

		return null;
	}

	public boolean isRequiresAuthentication() {
		return Boolean.parseBoolean(smtpRequiresAuthentication);
	}

	public void setRequiresAuthentication(boolean requiresAuthentication) {
		this.smtpRequiresAuthentication = Boolean.toString(requiresAuthentication);
	}

	public void setSmtpPassword(String smtpPassword) {
		if (smtpPassword != null && !smtpPassword.equals("")) {
			this.smtpPassword = smtpPassword;
			smtpPasswordStr = smtpPassword;
			try {
				CryptoConfigurationFile cryptoConfiguration = OxTrustConfiguration.instance().getCryptoConfiguration();
				smtpPasswordStr = StringEncrypter.defaultInstance().decrypt(smtpPasswordStr, cryptoConfiguration.getEncodeSalt());
			} catch (Exception ex) {
				log.error("Failed to decrypt password: " + smtpPassword, ex);
			}
		}
	}

	public void setSmtpPasswordStr(String smtpPasswordStr) {
		if (smtpPasswordStr != null && !smtpPasswordStr.equals("")) {
			this.smtpPasswordStr = smtpPasswordStr;
			smtpPassword = smtpPasswordStr;
			try {
				CryptoConfigurationFile cryptoConfiguration = OxTrustConfiguration.instance().getCryptoConfiguration();
				smtpPassword = StringEncrypter.defaultInstance().encrypt(smtpPassword, cryptoConfiguration.getEncodeSalt());
			} catch (Exception ex) {
				log.error("Failed to encrypt password: " + smtpPassword, ex);
			}
		}
	}

	public boolean isRequiresSsl() {
		return Boolean.parseBoolean(smtpRequiresSsl);
	}

	public void setRequiresSsl(boolean requiresSsl) {
		this.smtpRequiresSsl = Boolean.toString(requiresSsl);
	}

    public String getApplianceDnsServer() {
        return applianceDnsServer;
    }

    public void setApplianceDnsServer(String applianceDnsServer) {
        this.applianceDnsServer = applianceDnsServer;
    }

    public String getAuthenticationLevel() {
        return authenticationLevel;
    }

    public void setAuthenticationLevel(String authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getBlowfishPassword() {
        return blowfishPassword;
    }

    public void setBlowfishPassword(String blowfishPassword) {
        this.blowfishPassword = blowfishPassword;
    }

    public String getCacheRefreshServerIpAddress() {
        return cacheRefreshServerIpAddress;
    }

    public void setCacheRefreshServerIpAddress(String cacheRefreshServerIpAddress) {
        this.cacheRefreshServerIpAddress = cacheRefreshServerIpAddress;
    }

    public String getCentreonEmail() {
        return centreonEmail;
    }

    public void setCentreonEmail(String centreonEmail) {
        this.centreonEmail = centreonEmail;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public GluuBoolean getFederationHostingEnabled() {
        return federationHostingEnabled;
    }

    public void setFederationHostingEnabled(GluuBoolean federationHostingEnabled) {
        this.federationHostingEnabled = federationHostingEnabled;
    }

    public String getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public void setFreeDiskSpace(String freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }

    public String getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(String freeMemory) {
        this.freeMemory = freeMemory;
    }

    public String getFreeSwap() {
        return freeSwap;
    }

    public void setFreeSwap(String freeSwap) {
        this.freeSwap = freeSwap;
    }

    public String getGluuBandwidthRX() {
        return gluuBandwidthRX;
    }

    public void setGluuBandwidthRX(String gluuBandwidthRX) {
        this.gluuBandwidthRX = gluuBandwidthRX;
    }

    public String getGluuBandwidthTX() {
        return gluuBandwidthTX;
    }

    public void setGluuBandwidthTX(String gluuBandwidthTX) {
        this.gluuBandwidthTX = gluuBandwidthTX;
    }

    public String getGluuDSStatus() {
        return gluuDSStatus;
    }

    public void setGluuDSStatus(String gluuDSStatus) {
        this.gluuDSStatus = gluuDSStatus;
    }

    public String getGluuHttpStatus() {
        return gluuHttpStatus;
    }

    public void setGluuHttpStatus(String gluuHttpStatus) {
        this.gluuHttpStatus = gluuHttpStatus;
    }

    public String getGluuSPTR() {
        return gluuSPTR;
    }

    public void setGluuSPTR(String gluuSPTR) {
        this.gluuSPTR = gluuSPTR;
    }

    public String getGluuVDSStatus() {
        return gluuVDSStatus;
    }

    public void setGluuVDSStatus(String gluuVDSStatus) {
        this.gluuVDSStatus = gluuVDSStatus;
    }

    public String getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(String groupCount) {
        this.groupCount = groupCount;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIname() {
        return iname;
    }

    public void setIname(String iname) {
        this.iname = iname;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public String getInumFN() {
        return inumFN;
    }

    public void setInumFN(String inumFN) {
        this.inumFN = inumFN;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getJiraEmail() {
        return jiraEmail;
    }

    public void setJiraEmail(String jiraEmail) {
        this.jiraEmail = jiraEmail;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLoadAvg() {
        return loadAvg;
    }

    public void setLoadAvg(String loadAvg) {
        this.loadAvg = loadAvg;
    }

    public GluuBoolean getManageIdentityPermission() {
        return manageIdentityPermission;
    }

    public void setManageIdentityPermission(GluuBoolean manageIdentityPermission) {
        this.manageIdentityPermission = manageIdentityPermission;
    }

    public String getMaxLogSize() {
        return maxLogSize;
    }

    public void setMaxLogSize(String maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public String getMemcachedServerAddress() {
        return memcachedServerAddress;
    }

    public void setMemcachedServerAddress(String memcachedServerAddress) {
        this.memcachedServerAddress = memcachedServerAddress;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getOxClusterPartners() {
        return oxClusterPartners;
    }

    public void setOxClusterPartners(List<String> oxClusterPartners) {
        this.oxClusterPartners = oxClusterPartners;
    }

    public List<OxIDPAuthConf> getOxIDPAuthentication() {
        return oxIDPAuthentication;
    }

    public void setOxIDPAuthentication(List<OxIDPAuthConf> oxIDPAuthentication) {
        this.oxIDPAuthentication = oxIDPAuthentication;
    }

    public String getOxLogViewerConfig() {
        return oxLogViewerConfig;
    }

    public void setOxLogViewerConfig(String oxLogViewerConfig) {
        this.oxLogViewerConfig = oxLogViewerConfig;
    }

    public GluuBoolean getPasswordResetAllowed() {
        return passwordResetAllowed;
    }

    public void setPasswordResetAllowed(GluuBoolean passwordResetAllowed) {
        this.passwordResetAllowed = passwordResetAllowed;
    }

    public String getPersonCount() {
        return personCount;
    }

    public void setPersonCount(String personCount) {
        this.personCount = personCount;
    }

    public String getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(String pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getPrivacyEmail() {
        return privacyEmail;
    }

    public void setPrivacyEmail(String privacyEmail) {
        this.privacyEmail = privacyEmail;
    }

    public GluuBoolean getProfileManagment() {
        return profileManagment;
    }

    public void setProfileManagment(GluuBoolean profileManagment) {
        this.profileManagment = profileManagment;
    }

    public GluuBoolean getScimEnabled() {
        return scimEnabled;
    }

    public void setScimEnabled(GluuBoolean scimEnabled) {
        this.scimEnabled = scimEnabled;
    }

    public String getShibAssertionsIssued() {
        return shibAssertionsIssued;
    }

    public void setShibAssertionsIssued(String shibAssertionsIssued) {
        this.shibAssertionsIssued = shibAssertionsIssued;
    }

    public String getShibFailedAuth() {
        return shibFailedAuth;
    }

    public void setShibFailedAuth(String shibFailedAuth) {
        this.shibFailedAuth = shibFailedAuth;
    }

    public String getShibSecurityEvents() {
        return shibSecurityEvents;
    }

    public void setShibSecurityEvents(String shibSecurityEvents) {
        this.shibSecurityEvents = shibSecurityEvents;
    }

    public String getShibSuccessfulAuths() {
        return shibSuccessfulAuths;
    }

    public void setShibSuccessfulAuths(String shibSuccessfulAuths) {
        this.shibSuccessfulAuths = shibSuccessfulAuths;
    }

    public SmtpConfiguration getSmtpConfiguration() {
        return smtpConfiguration;
    }

    public void setSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
        this.smtpConfiguration = smtpConfiguration;
    }

    public String getSmtpFromEmailAddress() {
        return smtpFromEmailAddress;
    }

    public void setSmtpFromEmailAddress(String smtpFromEmailAddress) {
        this.smtpFromEmailAddress = smtpFromEmailAddress;
    }

    public String getSmtpFromName() {
        return smtpFromName;
    }

    public void setSmtpFromName(String smtpFromName) {
        this.smtpFromName = smtpFromName;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpRequiresAuthentication() {
        return smtpRequiresAuthentication;
    }

    public void setSmtpRequiresAuthentication(String smtpRequiresAuthentication) {
        this.smtpRequiresAuthentication = smtpRequiresAuthentication;
    }

    public String getSmtpRequiresSsl() {
        return smtpRequiresSsl;
    }

    public void setSmtpRequiresSsl(String smtpRequiresSsl) {
        this.smtpRequiresSsl = smtpRequiresSsl;
    }

    public String getSmtpUserName() {
        return smtpUserName;
    }

    public void setSmtpUserName(String smtpUserName) {
        this.smtpUserName = smtpUserName;
    }

    public String getSslExpiry() {
        return sslExpiry;
    }

    public void setSslExpiry(String sslExpiry) {
        this.sslExpiry = sslExpiry;
    }

    public GluuStatus getStatus() {
        return status;
    }

    public void setStatus(GluuStatus status) {
        this.status = status;
    }

    public String getSvnEmail() {
        return svnEmail;
    }

    public void setSvnEmail(String svnEmail) {
        this.svnEmail = svnEmail;
    }

    public String getSystemUptime() {
        return systemUptime;
    }

    public void setSystemUptime(String systemUptime) {
        this.systemUptime = systemUptime;
    }

    public List<TrustStoreCertificate> getTrustStoreCertificates() {
        return trustStoreCertificates;
    }

    public void setTrustStoreCertificates(List<TrustStoreCertificate> trustStoreCertificates) {
        this.trustStoreCertificates = trustStoreCertificates;
    }

    public TrustStoreConfiguration getTrustStoreConfiguration() {
        return trustStoreConfiguration;
    }

    public void setTrustStoreConfiguration(TrustStoreConfiguration trustStoreConfiguration) {
        this.trustStoreConfiguration = trustStoreConfiguration;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public GluuBoolean getVdsCacheRefreshEnabled() {
        return vdsCacheRefreshEnabled;
    }

    public void setVdsCacheRefreshEnabled(GluuBoolean vdsCacheRefreshEnabled) {
        this.vdsCacheRefreshEnabled = vdsCacheRefreshEnabled;
    }

    public String getVdsCacheRefreshLastUpdate() {
        return vdsCacheRefreshLastUpdate;
    }

    public void setVdsCacheRefreshLastUpdate(String vdsCacheRefreshLastUpdate) {
        this.vdsCacheRefreshLastUpdate = vdsCacheRefreshLastUpdate;
    }

    public String getVdsCacheRefreshLastUpdateCount() {
        return vdsCacheRefreshLastUpdateCount;
    }

    public void setVdsCacheRefreshLastUpdateCount(String vdsCacheRefreshLastUpdateCount) {
        this.vdsCacheRefreshLastUpdateCount = vdsCacheRefreshLastUpdateCount;
    }

    public String getVdsCacheRefreshPollingInterval() {
        return vdsCacheRefreshPollingInterval;
    }

    public void setVdsCacheRefreshPollingInterval(String vdsCacheRefreshPollingInterval) {
        this.vdsCacheRefreshPollingInterval = vdsCacheRefreshPollingInterval;
    }

    public String getVdsCacheRefreshProblemCount() {
        return vdsCacheRefreshProblemCount;
    }

    public void setVdsCacheRefreshProblemCount(String vdsCacheRefreshProblemCount) {
        this.vdsCacheRefreshProblemCount = vdsCacheRefreshProblemCount;
    }

    public GluuBoolean getWhitePagesEnabled() {
        return whitePagesEnabled;
    }

    public void setWhitePagesEnabled(GluuBoolean whitePagesEnabled) {
        this.whitePagesEnabled = whitePagesEnabled;
    }
}
