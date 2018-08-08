package org.gluu.oxtrust.service.config.cas;

import org.gluu.oxtrust.ldap.service.CASService;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;

public class CASProtocolConfiguration {
    private String casBaseURL;
    private ShibbolethCASProtocolConfiguration configuration;

    public CASProtocolConfiguration(String casBaseURL, ShibbolethCASProtocolConfiguration configuration) {
        this.casBaseURL = casBaseURL;
        this.configuration = configuration;
    }

    public String getCasBaseURL() {
        return casBaseURL;
    }

    public void setCasBaseURL(String casBaseURL) {
        this.casBaseURL = casBaseURL;
    }

    public ShibbolethCASProtocolConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ShibbolethCASProtocolConfiguration configuration) {
        this.configuration = configuration;
    }

    public void save(CASService casService) {
        CASProtocolAvailability casProtocolAvailability = CASProtocolAvailability.get();
        if (!casProtocolAvailability.isAvailable()) {
            throw new CASProtocolDisabledException();
        }

        if (configuration.getInum() == null || configuration.getInum().isEmpty()) {
            casService.addCASConfiguration(configuration);
        } else {
            casService.updateCASConfiguration(configuration);
        }
    }

    boolean isShibbolethEnabled() {
        return configuration.isEnabled();
    }
}