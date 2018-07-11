package org.gluu.oxtrust.service.config.oxauth;

import org.gluu.oxtrust.api.configuration.oxauth.OxAuthConfig;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import javax.inject.Inject;
import java.io.IOException;

public class OxAuthConfigurationService {

    @Inject
    private OxAuthConfigurationExtractor oxAuthConfigurationExtractor;
    @Inject
    private OxAuthConfigObjectMapper oxAuthConfigObjectMapper;
    @Inject
    private OxAuthConfigDynamicExtractor oxAuthConfigDynamicExtractor;
    @Inject
    private ConfigurationFactory configurationFactory;
    @Inject
    private PersistenceEntryManager persistenceEntryManager;

    public void save(OxAuthConfig oxAuthConfig) throws IOException {
        String configurationDn = configurationFactory.getConfigurationDn();

        LdapOxAuthConfiguration ldapOxAuthConfiguration = oxAuthConfigurationExtractor.extract(configurationDn);
        ldapOxAuthConfiguration.setOxAuthConfigDynamic(oxAuthConfigObjectMapper.serialize(oxAuthConfig));
        ldapOxAuthConfiguration.setRevision(revisited(ldapOxAuthConfiguration));
        persistenceEntryManager.merge(ldapOxAuthConfiguration);
    }

    private long revisited(LdapOxAuthConfiguration ldapOxAuthConfiguration) {
        return ldapOxAuthConfiguration.getRevision() + 1;
    }

    public OxAuthConfig find() throws IOException {
        return oxAuthConfigDynamicExtractor.extract();
    }

}