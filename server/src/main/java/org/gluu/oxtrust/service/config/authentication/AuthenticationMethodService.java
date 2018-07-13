package org.gluu.oxtrust.service.config.authentication;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.persist.model.base.GluuBoolean;

import javax.inject.Inject;

public class AuthenticationMethodService {

    @Inject
    private ApplianceService applianceService;

    @Inject
    private PassportService passportService;

    public AuthenticationMethod findAuthenticationMode() {
        GluuAppliance appliance = applianceService.getAppliance();
        return new AuthenticationMethod(appliance.getAuthenticationMode(),
                appliance.getOxTrustAuthenticationMode(),
                passportAuthenticationMethod(appliance));
    }

    private PassportAuthenticationMethod passportAuthenticationMethod(GluuAppliance appliance) {
        GluuBoolean passportEnabled = appliance.getPassportEnabled();
        return PassportAuthenticationMethod.from(passportEnabled, passportService.loadConfigurationFromLdap());
    }

    public void save(AuthenticationMethod authenticationMethod) {
        GluuAppliance appliance = applianceService.getAppliance();

        appliance.setAuthenticationMode(authenticationMethod.getAuthenticationMode());
        appliance.setOxTrustAuthenticationMode(authenticationMethod.getOxTrustAuthenticationMode());
        appliance.setPassportEnabled(authenticationMethod.getPassportAuthenticationMethod().asGluuBoolean());

        applianceService.updateAppliance(appliance);
        passportService.updateLdapOxPassportConfiguration(authenticationMethod.getPassportAuthenticationMethod().getLdapOxPassportConfiguration());
    }

    public void change(AuthenticationMethod authenticationMethod, GluuBoolean passportEnable) {
        authenticationMethod.setPassportAuthenticationMethod(
                PassportAuthenticationMethod.from(passportEnable, passportService.loadConfigurationFromLdap()));
    }
}
