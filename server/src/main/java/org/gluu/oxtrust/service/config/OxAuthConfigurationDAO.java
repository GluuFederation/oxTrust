package org.gluu.oxtrust.service.config;

import org.gluu.oxtrust.api.configuration.OxAuthConfig;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import java.io.IOException;

public class OxAuthConfigurationDAO {

    private final OxAuthConfigurationExtractor oxAuthConfigurationExtractor;
    private final OxAuthConfigObjectMapper oxAuthConfigObjectMapper;
    private final OxAuthConfigDynamicExtractor oxAuthConfigDynamicExtractor;
    private final ConfigurationFactory configurationFactory;
    private final PersistenceEntryManager persistenceEntryManager;

    public OxAuthConfigurationDAO() {
        // required for proxying.
        this(null, null, null, null, null);
    }

    public OxAuthConfigurationDAO(OxAuthConfigurationExtractor oxAuthConfigurationExtractor,
                                  OxAuthConfigObjectMapper oxAuthConfigObjectMapper,
                                  OxAuthConfigDynamicExtractor oxAuthConfigDynamicExtractor,
                                  ConfigurationFactory configurationFactory,
                                  PersistenceEntryManager persistenceEntryManager) {
        this.oxAuthConfigurationExtractor = oxAuthConfigurationExtractor;
        this.oxAuthConfigObjectMapper = oxAuthConfigObjectMapper;
        this.oxAuthConfigDynamicExtractor = oxAuthConfigDynamicExtractor;
        this.configurationFactory = configurationFactory;
        this.persistenceEntryManager = persistenceEntryManager;
    }

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