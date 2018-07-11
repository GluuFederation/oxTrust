package org.gluu.oxtrust.service.config.oxtrust;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;

import javax.inject.Inject;

public class OxTrustConfigurationExtractor {

    @Inject
    private ConfigurationFactory configurationFactory;
    @Inject
    private PersistenceEntryManager ldapEntryManager;

    public LdapOxTrustConfiguration extract() {
        String configurationDn = configurationFactory.getConfigurationDn();
        return ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn);
    }
}