package org.gluu.oxtrust.service.config.authentication;

import org.gluu.persist.model.base.GluuBoolean;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;

public class PassportAuthenticationMethod {

    private boolean enabled;
    private LdapOxPassportConfiguration ldapOxPassportConfiguration;

    public PassportAuthenticationMethod() {
        this(false, new LdapOxPassportConfiguration());
    }

    private PassportAuthenticationMethod(boolean enabled, LdapOxPassportConfiguration ldapOxPassportConfiguration) {
        this.enabled = enabled;
        this.ldapOxPassportConfiguration = ldapOxPassportConfiguration;
    }

    public static PassportAuthenticationMethod from(GluuBoolean enabled, LdapOxPassportConfiguration ldapOxPassportConfiguration) {
        return new PassportAuthenticationMethod(enabled == GluuBoolean.ENABLED, ldapOxPassportConfiguration);
    }

    public static PassportAuthenticationMethod disabled() {
        return new PassportAuthenticationMethod(false, new LdapOxPassportConfiguration());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LdapOxPassportConfiguration getLdapOxPassportConfiguration() {
        return ldapOxPassportConfiguration;
    }

    public void setLdapOxPassportConfiguration(LdapOxPassportConfiguration ldapOxPassportConfiguration) {
        this.ldapOxPassportConfiguration = ldapOxPassportConfiguration;
    }

    public GluuBoolean asGluuBoolean() {
        return enabled ? GluuBoolean.ENABLED : GluuBoolean.DISABLED;
    }

    PassportAuthenticationMethod mergeWith(PassportAuthenticationMethod other) {
        LdapOxPassportConfiguration ldapOxPassportConfiguration = new LdapOxPassportConfiguration();
        ldapOxPassportConfiguration.setDn(this.ldapOxPassportConfiguration.getDn());
        ldapOxPassportConfiguration.setBaseDn(this.ldapOxPassportConfiguration.getBaseDn());
        ldapOxPassportConfiguration.setStatus(this.ldapOxPassportConfiguration.getStatus());
        ldapOxPassportConfiguration.setPassportConfigurations(other.ldapOxPassportConfiguration.getPassportConfigurations());
        return new PassportAuthenticationMethod(other.enabled, ldapOxPassportConfiguration);
    }

}
