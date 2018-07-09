package org.gluu.oxtrust.service.config.oxauth;

import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import javax.inject.Inject;

public class OxAuthConfigurationExtractor {

    @Inject
    private PersistenceEntryManager persistenceEntryManager;

    LdapOxAuthConfiguration extract(String configurationDn) {
        configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
        return persistenceEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
    }

}