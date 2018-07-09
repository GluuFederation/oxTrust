package org.gluu.oxtrust.service.config.oxauth;

import org.gluu.oxtrust.api.configuration.oxauth.OxAuthConfig;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import javax.inject.Inject;
import java.io.IOException;

public class OxAuthConfigDynamicExtractor {

    @Inject
    private ConfigurationFactory configurationFactory;
    @Inject
    private OxAuthConfigurationExtractor oxAuthConfigurationExtractor;
    @Inject
    private OxAuthConfigObjectMapper oxAuthConfigObjectMapper;

    OxAuthConfig extract() throws IOException {
        String configurationDn = configurationFactory.getConfigurationDn();
        LdapOxAuthConfiguration ldapOxAuthConfiguration = oxAuthConfigurationExtractor.extract(configurationDn);
        return oxAuthConfigObjectMapper.deserialize(ldapOxAuthConfiguration);
    }

}