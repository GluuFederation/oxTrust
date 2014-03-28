package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.validation.constraints.Min;

import lombok.Data;

import org.apache.log4j.Logger;
import org.gluu.oxtrust.model.cert.TrustStoreCertificate;
import org.gluu.oxtrust.model.cert.TrustStoreConfiguration;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.ldap.model.InumEntry;
import org.xdi.util.security.StringEncrypter;

/**
 * GluuAppliance
 * 
 * @author Reda Zerrad Date: 08.10.2012
 */
@LdapEntry
@LdapObjectClass(values = { "top", "gluuAppliance" })
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
				smtpPasswordStr = StringEncrypter.defaultInstance().decrypt(smtpPasswordStr);
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
				smtpPassword = StringEncrypter.defaultInstance().encrypt(smtpPassword);
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

}
