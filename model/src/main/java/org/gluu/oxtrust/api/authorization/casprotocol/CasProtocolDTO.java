package org.gluu.oxtrust.api.authorization.casprotocol;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CasProtocolDTO {

    private static final String URL_PATTERN = "^(ftp|http|https):\\/\\/[^ \"]+$";

    @Size(min = 1)
    @Pattern(regexp = URL_PATTERN)
    private String casBaseURL;

    private ShibbolethCASProtocolConfigurationDTO shibbolethCASProtocolConfiguration;

    public String getCasBaseURL() {
        return casBaseURL;
    }

    public void setCasBaseURL(String casBaseURL) {
        this.casBaseURL = casBaseURL;
    }

    public ShibbolethCASProtocolConfigurationDTO getShibbolethCASProtocolConfiguration() {
        return shibbolethCASProtocolConfiguration;
    }

    public void setShibbolethCASProtocolConfiguration(ShibbolethCASProtocolConfigurationDTO shibbolethCASProtocolConfiguration) {
        this.shibbolethCASProtocolConfiguration = shibbolethCASProtocolConfiguration;
    }
}
