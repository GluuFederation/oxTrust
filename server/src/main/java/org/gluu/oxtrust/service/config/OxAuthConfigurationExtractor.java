package org.gluu.oxtrust.service.config;

import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

public class OxAuthConfigurationExtractor {
    private final PersistenceEntryManager persistenceEntryManager;

    public OxAuthConfigurationExtractor(PersistenceEntryManager persistenceEntryManager) {
        this.persistenceEntryManager = persistenceEntryManager;
    }

    LdapOxAuthConfiguration extract(String configurationDn) {
        configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
        return persistenceEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
    }

}