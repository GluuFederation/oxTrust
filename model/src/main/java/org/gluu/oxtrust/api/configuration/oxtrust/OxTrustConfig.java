package org.gluu.oxtrust.api.configuration.oxtrust;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

public class OxTrustConfig {

    static final String EMAIL_PATTERN = "^\\S+@\\S+$";
    static final String URL_PATTERN = "^(http|https):\\/\\/[^ \"]+$";

    @Required
    @Size(min = 1)
    private String baseDN;

    @Required
    private String orgInum;

    @Required
    private String orgIname;

    @Required
    @Pattern(regexp = EMAIL_PATTERN)
    private String orgSupportEmail;

    @Required
    @Size(min = 1)
    private String applianceInum;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String applianceUrl;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String baseEndpoint;

    @Required
    private String[] personObjectClassTypes;

    @Required
    private String personCustomObjectClass;

    @Required
    private String[] personObjectClassDisplayNames;

    @Required
    private String[] contactObjectClassTypes;

    @Required
    private String[] contactObjectClassDisplayNames;

    @Required
    private String photoRepositoryRootDir;

    private int photoRepositoryThumbWidth;

    private int photoRepositoryThumbHeight;

    private int photoRepositoryCountLeveles;

    private int photoRepositoryCountFoldersPerLevel;

    private String ldifStore;

    private boolean updateApplianceStatus;

    private String svnConfigurationStoreRoot;

    private String svnConfigurationStorePassword;

    @Required
    @Size(min = 1)
    private String keystorePath;

    @Required
    private String keystorePassword;

    private boolean allowPersonModification;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String idpUrl;

    @Required
    private String velocityLog;

    @Required
    private String spMetadataPath;

    private String logoLocation;

    @Required
    private String idpSecurityKey;

    @Required
    private String idpSecurityKeyPassword;

    @Required
    private String idpSecurityCert;

    @Required
    private String[] gluuSpAttributes;

    private boolean configGeneration;

    @Required
    private String idpLdapProtocol;

    @Required
    private String idpLdapServer;

    @Required
    private String idpBindDn;
    @Required

    private String idpBindPassword;

    private String idpUserFields;

    @Required
    private String gluuSpCert;

    @Required
    private String shibboleth3FederationRootDir;

    @Required
    private String caCertsLocation;

    private String caCertsPassphrase;

    private String tempCertDir;

    @Required
    private String certDir;

    @Required
    private String servicesRestartTrigger;

    private boolean persistSVN;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String oxAuthSectorIdentifierUrl;

    @Required
    private String oxAuthClientId;

    @Required
    private String oxAuthClientPassword;

    @Required
    private String oxAuthClientScope;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String loginRedirectUrl;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String logoutRedirectUrl;

    private String[] clusteredInums;

    @Required
    private String clientAssociationAttribute;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String oxAuthIssuer;

    private boolean ignoreValidation;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String umaIssuer;

    private String scimUmaClientId;

    private String scimUmaClientKeyId;

    @Required
    private String scimUmaResourceId;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String scimUmaScope;

    @Required
    private String scimUmaClientKeyStoreFile;

    @Required
    private String scimUmaClientKeyStorePassword;

    @Required
    private String passportUmaClientId;

    private String passportUmaClientKeyId;

    @Required
    private String passportUmaResourceId;

    @Required
    @Pattern(regexp = URL_PATTERN)
    private String passportUmaScope;

    @Required
    private String passportUmaClientKeyStoreFile;

    @Required
    private String passportUmaClientKeyStorePassword;

    private String apiUmaClientId;

    private String apiUmaClientKeyId;

    private String apiUmaResourceId;

    private String apiUmaScope;

    private String apiUmaClientKeyStoreFile;

    private String apiUmaClientKeyStorePassword;

    private String cssLocation;

    private String jsLocation;

    private String recaptchaSiteKey;

    private String recaptchaSecretKey;

    private boolean scimTestMode;

    private boolean rptConnectionPoolUseConnectionPooling;

    private int rptConnectionPoolMaxTotal;

    private int rptConnectionPoolDefaultMaxPerRoute;

    private int rptConnectionPoolValidateAfterInactivity;

    private int rptConnectionPoolCustomKeepAliveTimeout;

    private boolean oxIncommonFlag;

    @Required
    private List<String> clientWhiteList;

    @Required
    private List<String> clientBlackList;

    @Required
    private String loggingLevel;

    @Required
    private String shibbolethVersion;

    @Required
    private String shibboleth3IdpRootDir;

    @Required
    private String shibboleth3SpConfDir;

    @Required
    private String organizationName;

    @Required
    private String idp3SigningCert;

    @Required
    private String idp3EncryptionCert;

    private int metricReporterInterval;

    @Required
    private ScimPropertiesApi scimProperties;

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getOrgInum() {
        return orgInum;
    }

    public void setOrgInum(String orgInum) {
        this.orgInum = orgInum;
    }

    public String getOrgIname() {
        return orgIname;
    }

    public void setOrgIname(String orgIname) {
        this.orgIname = orgIname;
    }

    public String getOrgSupportEmail() {
        return orgSupportEmail;
    }

    public void setOrgSupportEmail(String orgSupportEmail) {
        this.orgSupportEmail = orgSupportEmail;
    }

    public String getApplianceInum() {
        return applianceInum;
    }

    public void setApplianceInum(String applianceInum) {
        this.applianceInum = applianceInum;
    }

    public String getApplianceUrl() {
        return applianceUrl;
    }

    public void setApplianceUrl(String applianceUrl) {
        this.applianceUrl = applianceUrl;
    }

    public String getBaseEndpoint() {
        return baseEndpoint;
    }

    public void setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    public String[] getPersonObjectClassTypes() {
        return personObjectClassTypes;
    }

    public void setPersonObjectClassTypes(String[] personObjectClassTypes) {
        this.personObjectClassTypes = personObjectClassTypes;
    }

    public String getPersonCustomObjectClass() {
        return personCustomObjectClass;
    }

    public void setPersonCustomObjectClass(String personCustomObjectClass) {
        this.personCustomObjectClass = personCustomObjectClass;
    }

    public String[] getPersonObjectClassDisplayNames() {
        return personObjectClassDisplayNames;
    }

    public void setPersonObjectClassDisplayNames(String[] personObjectClassDisplayNames) {
        this.personObjectClassDisplayNames = personObjectClassDisplayNames;
    }

    public String[] getContactObjectClassTypes() {
        return contactObjectClassTypes;
    }

    public void setContactObjectClassTypes(String[] contactObjectClassTypes) {
        this.contactObjectClassTypes = contactObjectClassTypes;
    }

    public String[] getContactObjectClassDisplayNames() {
        return contactObjectClassDisplayNames;
    }

    public void setContactObjectClassDisplayNames(String[] contactObjectClassDisplayNames) {
        this.contactObjectClassDisplayNames = contactObjectClassDisplayNames;
    }

    public String getPhotoRepositoryRootDir() {
        return photoRepositoryRootDir;
    }

    public void setPhotoRepositoryRootDir(String photoRepositoryRootDir) {
        this.photoRepositoryRootDir = photoRepositoryRootDir;
    }

    public int getPhotoRepositoryThumbWidth() {
        return photoRepositoryThumbWidth;
    }

    public void setPhotoRepositoryThumbWidth(int photoRepositoryThumbWidth) {
        this.photoRepositoryThumbWidth = photoRepositoryThumbWidth;
    }

    public int getPhotoRepositoryThumbHeight() {
        return photoRepositoryThumbHeight;
    }

    public void setPhotoRepositoryThumbHeight(int photoRepositoryThumbHeight) {
        this.photoRepositoryThumbHeight = photoRepositoryThumbHeight;
    }

    public int getPhotoRepositoryCountLeveles() {
        return photoRepositoryCountLeveles;
    }

    public void setPhotoRepositoryCountLeveles(int photoRepositoryCountLeveles) {
        this.photoRepositoryCountLeveles = photoRepositoryCountLeveles;
    }

    public int getPhotoRepositoryCountFoldersPerLevel() {
        return photoRepositoryCountFoldersPerLevel;
    }

    public void setPhotoRepositoryCountFoldersPerLevel(int photoRepositoryCountFoldersPerLevel) {
        this.photoRepositoryCountFoldersPerLevel = photoRepositoryCountFoldersPerLevel;
    }

    public String getLdifStore() {
        return ldifStore;
    }

    public void setLdifStore(String ldifStore) {
        this.ldifStore = ldifStore;
    }

    public boolean isUpdateApplianceStatus() {
        return updateApplianceStatus;
    }

    public void setUpdateApplianceStatus(boolean updateApplianceStatus) {
        this.updateApplianceStatus = updateApplianceStatus;
    }

    public String getSvnConfigurationStoreRoot() {
        return svnConfigurationStoreRoot;
    }

    public void setSvnConfigurationStoreRoot(String svnConfigurationStoreRoot) {
        this.svnConfigurationStoreRoot = svnConfigurationStoreRoot;
    }

    public String getSvnConfigurationStorePassword() {
        return svnConfigurationStorePassword;
    }

    public void setSvnConfigurationStorePassword(String svnConfigurationStorePassword) {
        this.svnConfigurationStorePassword = svnConfigurationStorePassword;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public boolean isAllowPersonModification() {
        return allowPersonModification;
    }

    public void setAllowPersonModification(boolean allowPersonModification) {
        this.allowPersonModification = allowPersonModification;
    }

    public String getIdpUrl() {
        return idpUrl;
    }

    public void setIdpUrl(String idpUrl) {
        this.idpUrl = idpUrl;
    }

    public String getVelocityLog() {
        return velocityLog;
    }

    public void setVelocityLog(String velocityLog) {
        this.velocityLog = velocityLog;
    }

    public String getSpMetadataPath() {
        return spMetadataPath;
    }

    public void setSpMetadataPath(String spMetadataPath) {
        this.spMetadataPath = spMetadataPath;
    }

    public String getLogoLocation() {
        return logoLocation;
    }

    public void setLogoLocation(String logoLocation) {
        this.logoLocation = logoLocation;
    }

    public String getIdpSecurityKey() {
        return idpSecurityKey;
    }

    public void setIdpSecurityKey(String idpSecurityKey) {
        this.idpSecurityKey = idpSecurityKey;
    }

    public String getIdpSecurityKeyPassword() {
        return idpSecurityKeyPassword;
    }

    public void setIdpSecurityKeyPassword(String idpSecurityKeyPassword) {
        this.idpSecurityKeyPassword = idpSecurityKeyPassword;
    }

    public String getIdpSecurityCert() {
        return idpSecurityCert;
    }

    public void setIdpSecurityCert(String idpSecurityCert) {
        this.idpSecurityCert = idpSecurityCert;
    }

    public String[] getGluuSpAttributes() {
        return gluuSpAttributes;
    }

    public void setGluuSpAttributes(String[] gluuSpAttributes) {
        this.gluuSpAttributes = gluuSpAttributes;
    }

    public boolean isConfigGeneration() {
        return configGeneration;
    }

    public void setConfigGeneration(boolean configGeneration) {
        this.configGeneration = configGeneration;
    }

    public String getIdpLdapProtocol() {
        return idpLdapProtocol;
    }

    public void setIdpLdapProtocol(String idpLdapProtocol) {
        this.idpLdapProtocol = idpLdapProtocol;
    }

    public String getIdpLdapServer() {
        return idpLdapServer;
    }

    public void setIdpLdapServer(String idpLdapServer) {
        this.idpLdapServer = idpLdapServer;
    }

    public String getIdpBindDn() {
        return idpBindDn;
    }

    public void setIdpBindDn(String idpBindDn) {
        this.idpBindDn = idpBindDn;
    }

    public String getIdpBindPassword() {
        return idpBindPassword;
    }

    public void setIdpBindPassword(String idpBindPassword) {
        this.idpBindPassword = idpBindPassword;
    }

    public String getIdpUserFields() {
        return idpUserFields;
    }

    public void setIdpUserFields(String idpUserFields) {
        this.idpUserFields = idpUserFields;
    }

    public String getGluuSpCert() {
        return gluuSpCert;
    }

    public void setGluuSpCert(String gluuSpCert) {
        this.gluuSpCert = gluuSpCert;
    }

    public String getShibboleth3FederationRootDir() {
        return shibboleth3FederationRootDir;
    }

    public void setShibboleth3FederationRootDir(String shibboleth3FederationRootDir) {
        this.shibboleth3FederationRootDir = shibboleth3FederationRootDir;
    }

    public String getCaCertsLocation() {
        return caCertsLocation;
    }

    public void setCaCertsLocation(String caCertsLocation) {
        this.caCertsLocation = caCertsLocation;
    }

    public String getCaCertsPassphrase() {
        return caCertsPassphrase;
    }

    public void setCaCertsPassphrase(String caCertsPassphrase) {
        this.caCertsPassphrase = caCertsPassphrase;
    }

    public String getTempCertDir() {
        return tempCertDir;
    }

    public void setTempCertDir(String tempCertDir) {
        this.tempCertDir = tempCertDir;
    }

    public String getCertDir() {
        return certDir;
    }

    public void setCertDir(String certDir) {
        this.certDir = certDir;
    }

    public String getServicesRestartTrigger() {
        return servicesRestartTrigger;
    }

    public void setServicesRestartTrigger(String servicesRestartTrigger) {
        this.servicesRestartTrigger = servicesRestartTrigger;
    }

    public boolean isPersistSVN() {
        return persistSVN;
    }

    public void setPersistSVN(boolean persistSVN) {
        this.persistSVN = persistSVN;
    }

    public String getOxAuthSectorIdentifierUrl() {
        return oxAuthSectorIdentifierUrl;
    }

    public void setOxAuthSectorIdentifierUrl(String oxAuthSectorIdentifierUrl) {
        this.oxAuthSectorIdentifierUrl = oxAuthSectorIdentifierUrl;
    }

    public String getOxAuthClientId() {
        return oxAuthClientId;
    }

    public void setOxAuthClientId(String oxAuthClientId) {
        this.oxAuthClientId = oxAuthClientId;
    }

    public String getOxAuthClientPassword() {
        return oxAuthClientPassword;
    }

    public void setOxAuthClientPassword(String oxAuthClientPassword) {
        this.oxAuthClientPassword = oxAuthClientPassword;
    }

    public String getOxAuthClientScope() {
        return oxAuthClientScope;
    }

    public void setOxAuthClientScope(String oxAuthClientScope) {
        this.oxAuthClientScope = oxAuthClientScope;
    }

    public String getLoginRedirectUrl() {
        return loginRedirectUrl;
    }

    public void setLoginRedirectUrl(String loginRedirectUrl) {
        this.loginRedirectUrl = loginRedirectUrl;
    }

    public String getLogoutRedirectUrl() {
        return logoutRedirectUrl;
    }

    public void setLogoutRedirectUrl(String logoutRedirectUrl) {
        this.logoutRedirectUrl = logoutRedirectUrl;
    }

    public String[] getClusteredInums() {
        return clusteredInums;
    }

    public void setClusteredInums(String[] clusteredInums) {
        this.clusteredInums = clusteredInums;
    }

    public String getClientAssociationAttribute() {
        return clientAssociationAttribute;
    }

    public void setClientAssociationAttribute(String clientAssociationAttribute) {
        this.clientAssociationAttribute = clientAssociationAttribute;
    }

    public String getOxAuthIssuer() {
        return oxAuthIssuer;
    }

    public void setOxAuthIssuer(String oxAuthIssuer) {
        this.oxAuthIssuer = oxAuthIssuer;
    }

    public boolean isIgnoreValidation() {
        return ignoreValidation;
    }

    public void setIgnoreValidation(boolean ignoreValidation) {
        this.ignoreValidation = ignoreValidation;
    }

    public String getUmaIssuer() {
        return umaIssuer;
    }

    public void setUmaIssuer(String umaIssuer) {
        this.umaIssuer = umaIssuer;
    }

    public String getScimUmaClientId() {
        return scimUmaClientId;
    }

    public void setScimUmaClientId(String scimUmaClientId) {
        this.scimUmaClientId = scimUmaClientId;
    }

    public String getScimUmaClientKeyId() {
        return scimUmaClientKeyId;
    }

    public void setScimUmaClientKeyId(String scimUmaClientKeyId) {
        this.scimUmaClientKeyId = scimUmaClientKeyId;
    }

    public String getScimUmaResourceId() {
        return scimUmaResourceId;
    }

    public void setScimUmaResourceId(String scimUmaResourceId) {
        this.scimUmaResourceId = scimUmaResourceId;
    }

    public String getScimUmaScope() {
        return scimUmaScope;
    }

    public void setScimUmaScope(String scimUmaScope) {
        this.scimUmaScope = scimUmaScope;
    }

    public String getScimUmaClientKeyStoreFile() {
        return scimUmaClientKeyStoreFile;
    }

    public void setScimUmaClientKeyStoreFile(String scimUmaClientKeyStoreFile) {
        this.scimUmaClientKeyStoreFile = scimUmaClientKeyStoreFile;
    }

    public String getScimUmaClientKeyStorePassword() {
        return scimUmaClientKeyStorePassword;
    }

    public void setScimUmaClientKeyStorePassword(String scimUmaClientKeyStorePassword) {
        this.scimUmaClientKeyStorePassword = scimUmaClientKeyStorePassword;
    }

    public String getPassportUmaClientId() {
        return passportUmaClientId;
    }

    public void setPassportUmaClientId(String passportUmaClientId) {
        this.passportUmaClientId = passportUmaClientId;
    }

    public String getPassportUmaClientKeyId() {
        return passportUmaClientKeyId;
    }

    public void setPassportUmaClientKeyId(String passportUmaClientKeyId) {
        this.passportUmaClientKeyId = passportUmaClientKeyId;
    }

    public String getPassportUmaResourceId() {
        return passportUmaResourceId;
    }

    public void setPassportUmaResourceId(String passportUmaResourceId) {
        this.passportUmaResourceId = passportUmaResourceId;
    }

    public String getPassportUmaScope() {
        return passportUmaScope;
    }

    public void setPassportUmaScope(String passportUmaScope) {
        this.passportUmaScope = passportUmaScope;
    }

    public String getPassportUmaClientKeyStoreFile() {
        return passportUmaClientKeyStoreFile;
    }

    public void setPassportUmaClientKeyStoreFile(String passportUmaClientKeyStoreFile) {
        this.passportUmaClientKeyStoreFile = passportUmaClientKeyStoreFile;
    }

    public String getPassportUmaClientKeyStorePassword() {
        return passportUmaClientKeyStorePassword;
    }

    public void setPassportUmaClientKeyStorePassword(String passportUmaClientKeyStorePassword) {
        this.passportUmaClientKeyStorePassword = passportUmaClientKeyStorePassword;
    }

    public String getApiUmaClientId() {
        return apiUmaClientId;
    }

    public void setApiUmaClientId(String apiUmaClientId) {
        this.apiUmaClientId = apiUmaClientId;
    }

    public String getApiUmaClientKeyId() {
        return apiUmaClientKeyId;
    }

    public void setApiUmaClientKeyId(String apiUmaClientKeyId) {
        this.apiUmaClientKeyId = apiUmaClientKeyId;
    }

    public String getApiUmaResourceId() {
        return apiUmaResourceId;
    }

    public void setApiUmaResourceId(String apiUmaResourceId) {
        this.apiUmaResourceId = apiUmaResourceId;
    }

    public String getApiUmaScope() {
        return apiUmaScope;
    }

    public void setApiUmaScope(String apiUmaScope) {
        this.apiUmaScope = apiUmaScope;
    }

    public String getApiUmaClientKeyStoreFile() {
        return apiUmaClientKeyStoreFile;
    }

    public void setApiUmaClientKeyStoreFile(String apiUmaClientKeyStoreFile) {
        this.apiUmaClientKeyStoreFile = apiUmaClientKeyStoreFile;
    }

    public String getApiUmaClientKeyStorePassword() {
        return apiUmaClientKeyStorePassword;
    }

    public void setApiUmaClientKeyStorePassword(String apiUmaClientKeyStorePassword) {
        this.apiUmaClientKeyStorePassword = apiUmaClientKeyStorePassword;
    }

    public String getCssLocation() {
        return cssLocation;
    }

    public void setCssLocation(String cssLocation) {
        this.cssLocation = cssLocation;
    }

    public String getJsLocation() {
        return jsLocation;
    }

    public void setJsLocation(String jsLocation) {
        this.jsLocation = jsLocation;
    }

    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    public void setRecaptchaSiteKey(String recaptchaSiteKey) {
        this.recaptchaSiteKey = recaptchaSiteKey;
    }

    public String getRecaptchaSecretKey() {
        return recaptchaSecretKey;
    }

    public void setRecaptchaSecretKey(String recaptchaSecretKey) {
        this.recaptchaSecretKey = recaptchaSecretKey;
    }

    public boolean isScimTestMode() {
        return scimTestMode;
    }

    public void setScimTestMode(boolean scimTestMode) {
        this.scimTestMode = scimTestMode;
    }

    public boolean isRptConnectionPoolUseConnectionPooling() {
        return rptConnectionPoolUseConnectionPooling;
    }

    public void setRptConnectionPoolUseConnectionPooling(boolean rptConnectionPoolUseConnectionPooling) {
        this.rptConnectionPoolUseConnectionPooling = rptConnectionPoolUseConnectionPooling;
    }

    public int getRptConnectionPoolMaxTotal() {
        return rptConnectionPoolMaxTotal;
    }

    public void setRptConnectionPoolMaxTotal(int rptConnectionPoolMaxTotal) {
        this.rptConnectionPoolMaxTotal = rptConnectionPoolMaxTotal;
    }

    public int getRptConnectionPoolDefaultMaxPerRoute() {
        return rptConnectionPoolDefaultMaxPerRoute;
    }

    public void setRptConnectionPoolDefaultMaxPerRoute(int rptConnectionPoolDefaultMaxPerRoute) {
        this.rptConnectionPoolDefaultMaxPerRoute = rptConnectionPoolDefaultMaxPerRoute;
    }

    public int getRptConnectionPoolValidateAfterInactivity() {
        return rptConnectionPoolValidateAfterInactivity;
    }

    public void setRptConnectionPoolValidateAfterInactivity(int rptConnectionPoolValidateAfterInactivity) {
        this.rptConnectionPoolValidateAfterInactivity = rptConnectionPoolValidateAfterInactivity;
    }

    public int getRptConnectionPoolCustomKeepAliveTimeout() {
        return rptConnectionPoolCustomKeepAliveTimeout;
    }

    public void setRptConnectionPoolCustomKeepAliveTimeout(int rptConnectionPoolCustomKeepAliveTimeout) {
        this.rptConnectionPoolCustomKeepAliveTimeout = rptConnectionPoolCustomKeepAliveTimeout;
    }

    public boolean isOxIncommonFlag() {
        return oxIncommonFlag;
    }

    public void setOxIncommonFlag(boolean oxIncommonFlag) {
        this.oxIncommonFlag = oxIncommonFlag;
    }

    public List<String> getClientWhiteList() {
        return clientWhiteList;
    }

    public void setClientWhiteList(List<String> clientWhiteList) {
        this.clientWhiteList = clientWhiteList;
    }

    public List<String> getClientBlackList() {
        return clientBlackList;
    }

    public void setClientBlackList(List<String> clientBlackList) {
        this.clientBlackList = clientBlackList;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(String loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public String getShibbolethVersion() {
        return shibbolethVersion;
    }

    public void setShibbolethVersion(String shibbolethVersion) {
        this.shibbolethVersion = shibbolethVersion;
    }

    public String getShibboleth3IdpRootDir() {
        return shibboleth3IdpRootDir;
    }

    public void setShibboleth3IdpRootDir(String shibboleth3IdpRootDir) {
        this.shibboleth3IdpRootDir = shibboleth3IdpRootDir;
    }

    public String getShibboleth3SpConfDir() {
        return shibboleth3SpConfDir;
    }

    public void setShibboleth3SpConfDir(String shibboleth3SpConfDir) {
        this.shibboleth3SpConfDir = shibboleth3SpConfDir;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getIdp3SigningCert() {
        return idp3SigningCert;
    }

    public void setIdp3SigningCert(String idp3SigningCert) {
        this.idp3SigningCert = idp3SigningCert;
    }

    public String getIdp3EncryptionCert() {
        return idp3EncryptionCert;
    }

    public void setIdp3EncryptionCert(String idp3EncryptionCert) {
        this.idp3EncryptionCert = idp3EncryptionCert;
    }

    public int getMetricReporterInterval() {
        return metricReporterInterval;
    }

    public void setMetricReporterInterval(int metricReporterInterval) {
        this.metricReporterInterval = metricReporterInterval;
    }

    public ScimPropertiesApi getScimProperties() {
        return scimProperties;
    }

    public void setScimProperties(ScimPropertiesApi scimProperties) {
        this.scimProperties = scimProperties;
    }
}
