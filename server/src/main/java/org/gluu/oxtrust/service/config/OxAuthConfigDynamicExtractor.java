package org.gluu.oxtrust.service.config;

import org.gluu.oxtrust.api.configuration.OxAuthConfig;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import java.io.IOException;

public class OxAuthConfigDynamicExtractor {

    private final ConfigurationFactory configurationFactory;
    private final OxAuthConfigurationExtractor oxAuthConfigurationExtractor;
    private final OxAuthConfigObjectMapper oxAuthConfigObjectMapper;

    public OxAuthConfigDynamicExtractor(ConfigurationFactory configurationFactory,
                                        OxAuthConfigurationExtractor oxAuthConfigurationExtractor,
                                        OxAuthConfigObjectMapper oxAuthConfigObjectMapper) {
        this.configurationFactory = configurationFactory;
        this.oxAuthConfigurationExtractor = oxAuthConfigurationExtractor;
        this.oxAuthConfigObjectMapper = oxAuthConfigObjectMapper;
    }

    OxAuthConfig extract() throws IOException {
        String configurationDn = configurationFactory.getConfigurationDn();
        LdapOxAuthConfiguration ldapOxAuthConfiguration = oxAuthConfigurationExtractor.extract(configurationDn);
        return oxAuthConfigObjectMapper.deserialize(ldapOxAuthConfiguration);
    }

}