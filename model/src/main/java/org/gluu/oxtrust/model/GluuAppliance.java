/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.model.cert.TrustStoreConfiguration;
import org.gluu.persist.model.base.GluuBoolean;
import org.gluu.persist.model.base.InumEntry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.model.GluuStatus;
import org.xdi.model.SmtpConfiguration;
import org.xdi.service.cache.CacheConfiguration;

/**
 * GluuAppliance
 * 
 * @author Reda Zerrad Date: 08.10.2012
 * @author Yuriy Movchan Date: 04/20/2014
 */
@LdapEntry
@LdapObjectClass(values = { "top", "gluuAppliance" })
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
	private Date lastUpdate;

	@LdapAttribute(name = "gluuAppliancePollingInterval")
	private String pollingInterval;

	@LdapAttribute(name = "gluuStatus", updateOnly = true)
	private GluuStatus status;

	@LdapAttribute(name = "userPassword", ignoreDuringRead = true)
	private String userPassword;

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

	@LdapAttribute(name = "gluuManageIdentityPermission")
	private GluuBoolean manageIdentityPermission;

	@LdapAttribute(name = "gluuVdsCacheRefreshEnabled")
	private GluuBoolean vdsCacheRefreshEnabled;

	@LdapAttribute(name = "oxTrustCacheRefreshServerIpAddress")
	private String cacheRefreshServerIpAddress;

	@LdapAttribute(name = "gluuVdsCacheRefreshPollingInterval")
	private String vdsCacheRefreshPollingInterval;

	@LdapAttribute(name = "gluuVdsCacheRefreshLastUpdate")
	private Date vdsCacheRefreshLastUpdate;

	@LdapAttribute(name = "gluuVdsCacheRefreshLastUpdateCount")
	private String vdsCacheRefreshLastUpdateCount;

	@LdapAttribute(name = "gluuVdsCacheRefreshProblemCount")
	private String vdsCacheRefreshProblemCount;

	@LdapAttribute(name = "gluuScimEnabled")
	private GluuBoolean scimEnabled;

	@LdapAttribute(name = "gluuPassportEnabled")
	private GluuBoolean passportEnabled;

	@LdapAttribute(name = "oxTrustEmail")
	private String contactEmail;

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

	@LdapAttribute(name = "oxTrustAuthenticationMode")
	private String oxTrustAuthenticationMode;

	@LdapAttribute(name = "oxLogViewerConfig")
	private String oxLogViewerConfig;

    @LdapAttribute(name = "oxLogConfigLocation")
    private String oxLogConfigLocation;

	@LdapAttribute(name = "passwordResetAllowed")
	private GluuBoolean passwordResetAllowed;

	@LdapAttribute(name = "oxTrustStoreConf")
	@LdapJsonObject
	private TrustStoreConfiguration trustStoreConfiguration;

	@LdapAttribute(name = "oxTrustStoreCert")
	@LdapJsonObject
	private List<TrustStoreCertificate> trustStoreCertificates;

    @LdapAttribute(name = "oxCacheConfiguration")
   	@LdapJsonObject
   	private CacheConfiguration cacheConfiguration;

    public String getApplianceDnsServer() {
        return applianceDnsServer;
    }

    public void setApplianceDnsServer(String applianceDnsServer) {
        this.applianceDnsServer = applianceDnsServer;
    }

    public final SmtpConfiguration getSmtpConfiguration() {
		return smtpConfiguration;
	}

	public final void setSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
		this.smtpConfiguration = smtpConfiguration;
	}

	public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getOxTrustAuthenticationMode() {
		return oxTrustAuthenticationMode;
	}

	public void setOxTrustAuthenticationMode(String oxTrustAuthenticationMode) {
		this.oxTrustAuthenticationMode = oxTrustAuthenticationMode;
	}

    public String getCacheRefreshServerIpAddress() {
        return cacheRefreshServerIpAddress;
    }

    public void setCacheRefreshServerIpAddress(String cacheRefreshServerIpAddress) {
        this.cacheRefreshServerIpAddress = cacheRefreshServerIpAddress;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public String getOxLogConfigLocation() {
        return oxLogConfigLocation;
    }

    public void setOxLogConfigLocation(String oxLogConfigLocation) {
        this.oxLogConfigLocation = oxLogConfigLocation;
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
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

    public Date getVdsCacheRefreshLastUpdate() {
        return vdsCacheRefreshLastUpdate;
    }

    public void setVdsCacheRefreshLastUpdate(Date vdsCacheRefreshLastUpdate) {
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

	public GluuBoolean getPassportEnabled() {
		return passportEnabled;
	}

	public void setPassportEnabled(GluuBoolean passportEnabled) {
		this.passportEnabled = passportEnabled;
	}

    public CacheConfiguration getCacheConfiguration() {
   		return cacheConfiguration;
   	}

   	public void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
   		this.cacheConfiguration = cacheConfiguration;
   	}

}
