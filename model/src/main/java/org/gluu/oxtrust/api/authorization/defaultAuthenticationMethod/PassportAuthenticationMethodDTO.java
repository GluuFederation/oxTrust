package org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod;

import java.util.List;

public class PassportAuthenticationMethodDTO {
    private boolean enabled;

    private List<PassportConfigurationDTO> passportConfigurations;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<PassportConfigurationDTO> getPassportConfigurations() {
        return passportConfigurations;
    }

    public void setPassportConfigurations(List<PassportConfigurationDTO> passportConfigurations) {
        this.passportConfigurations = passportConfigurations;
    }
}