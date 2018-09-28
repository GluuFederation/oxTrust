package org.gluu.oxtrust.api.organization;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

class OrganizationConfigurationDTO {
    private boolean passwordResetEnabled;

    private boolean scimEnabled;

    private boolean passportEnabled;

    @NotNull
    @Size(min = 1)
    private String applianceDnsServer;

    private long maxLogSize;

    private boolean profileManagementEnabled;
    @Email
    private String contactEmail;

    @NotNull
    private OrganizationOxTrustConfigurationDTO organizationOxTrustConfiguration;

    @NotNull
    @Size(min = 1)
    private String oxAuthServerIP;

    public boolean isPasswordResetEnabled() {
        return passwordResetEnabled;
    }

    public void setPasswordResetEnabled(boolean passwordResetEnabled) {
        this.passwordResetEnabled = passwordResetEnabled;
    }

    public boolean isScimEnabled() {
        return scimEnabled;
    }

    public void setScimEnabled(boolean scimEnabled) {
        this.scimEnabled = scimEnabled;
    }

    public boolean isPassportEnabled() {
        return passportEnabled;
    }

    public void setPassportEnabled(boolean passportEnabled) {
        this.passportEnabled = passportEnabled;
    }

    public String getApplianceDnsServer() {
        return applianceDnsServer;
    }

    public void setApplianceDnsServer(String applianceDnsServer) {
        this.applianceDnsServer = applianceDnsServer;
    }

    public long getMaxLogSize() {
        return maxLogSize;
    }

    public void setMaxLogSize(long maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public boolean isProfileManagementEnabled() {
        return profileManagementEnabled;
    }

    public void setProfileManagementEnabled(boolean profileManagementEnabled) {
        this.profileManagementEnabled = profileManagementEnabled;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public OrganizationOxTrustConfigurationDTO getOrganizationOxTrustConfiguration() {
        return organizationOxTrustConfiguration;
    }

    public void setOrganizationOxTrustConfiguration(OrganizationOxTrustConfigurationDTO organizationOxTrustConfiguration) {
        this.organizationOxTrustConfiguration = organizationOxTrustConfiguration;
    }

    public String getOxAuthServerIP() {
        return oxAuthServerIP;
    }

    public void setOxAuthServerIP(String oxAuthServerIP) {
        this.oxAuthServerIP = oxAuthServerIP;
    }
}
