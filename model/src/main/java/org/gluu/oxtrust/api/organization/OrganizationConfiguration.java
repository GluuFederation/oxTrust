package org.gluu.oxtrust.api.organization;

import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.persist.model.base.GluuBoolean;
import org.xdi.config.oxauth.WebKeysSettings;

public class OrganizationConfiguration {

    private boolean passwordResetEnabled;
    private boolean scimEnabled;
    private boolean passportEnabled;
    private String applianceDnsServer;
    private long maxLogSize;
    private boolean profileManagementEnabled;
    private String contactEmail;
    private OrganizationOxTrustConfiguration organizationOxTrustConfiguration;
    private String oxAuthServerIP;

    public OrganizationConfiguration() {
    }

    public OrganizationConfiguration(boolean passwordResetEnabled,
                                     boolean scimEnabled,
                                     boolean passportEnabled,
                                     String applianceDnsServer,
                                     long maxLogSize,
                                     boolean profileManagementEnabled,
                                     String contactEmail,
                                     OrganizationOxTrustConfiguration organizationOxTrustConfiguration,
                                     String oxAuthServerIP) {
        this.passwordResetEnabled = passwordResetEnabled;
        this.scimEnabled = scimEnabled;
        this.passportEnabled = passportEnabled;
        this.applianceDnsServer = applianceDnsServer;
        this.maxLogSize = maxLogSize;
        this.profileManagementEnabled = profileManagementEnabled;
        this.contactEmail = contactEmail;
        this.organizationOxTrustConfiguration = organizationOxTrustConfiguration;
        this.oxAuthServerIP = oxAuthServerIP;
    }

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

    public OrganizationOxTrustConfiguration getOrganizationOxTrustConfiguration() {
        return organizationOxTrustConfiguration;
    }

    public void setOrganizationOxTrustConfiguration(OrganizationOxTrustConfiguration organizationOxTrustConfiguration) {
        this.organizationOxTrustConfiguration = organizationOxTrustConfiguration;
    }

    public String getOxAuthServerIP() {
        return oxAuthServerIP;
    }

    public void setOxAuthServerIP(String oxAuthServerIP) {
        this.oxAuthServerIP = oxAuthServerIP;
    }

    public void populate(GluuAppliance appliance) {
        appliance.setPasswordResetAllowed(passwordResetEnabled ? GluuBoolean.ENABLED : GluuBoolean.DISABLED);
        appliance.setPassportEnabled(passportEnabled ? GluuBoolean.ENABLED : GluuBoolean.DISABLED);
        appliance.setScimEnabled(scimEnabled ? GluuBoolean.ENABLED : GluuBoolean.DISABLED);
        appliance.setProfileManagment(profileManagementEnabled ? GluuBoolean.ENABLED : GluuBoolean.DISABLED);

        appliance.setApplianceDnsServer(applianceDnsServer);
        appliance.setMaxLogSize(String.valueOf(maxLogSize));
        appliance.setContactEmail(contactEmail);
    }

    public void populate(WebKeysSettings webKeysSettings) {
        webKeysSettings.setServerIP(oxAuthServerIP);
    }
}
