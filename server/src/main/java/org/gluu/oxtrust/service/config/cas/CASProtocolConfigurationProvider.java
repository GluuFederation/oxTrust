package org.gluu.oxtrust.service.config.cas;

import org.gluu.oxtrust.api.authorization.casprotocol.SessionStorageType;
import org.gluu.oxtrust.ldap.service.CASService;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;

import javax.inject.Inject;

public class CASProtocolConfigurationProvider {

    @Inject
    private CASService casService;

    @Inject
    private AppConfiguration appConfiguration;

    public CASProtocolConfiguration get() {
        CASProtocolAvailability casProtocolAvailability = CASProtocolAvailability.get();
        if (!casProtocolAvailability.isAvailable()) {
            throw new CASProtocolDisabledException();
        }
        return new CASProtocolConfiguration(baseUrl(), configuration());
    }

    private String baseUrl() {
        return appConfiguration.getIdpUrl() + "/idp/profile/cas";
    }

    private ShibbolethCASProtocolConfiguration configuration() {
        ShibbolethCASProtocolConfiguration configuration = casService.loadCASConfiguration();
        if (configuration != null) {
            return configuration;
        }
        return create();
    }


    private ShibbolethCASProtocolConfiguration create() {
        ShibbolethCASProtocolConfiguration newConfiguration = new ShibbolethCASProtocolConfiguration();
        newConfiguration.setEnabled(false);
        newConfiguration.setEnableToProxyPatterns(false);
        newConfiguration.setAuthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
        newConfiguration.setUnauthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
        newConfiguration.setSessionStorageType(SessionStorageType.DEFAULT_STORAGE_SERVICE.getName());
        return newConfiguration;
    }
}