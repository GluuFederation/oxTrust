package org.gluu.oxtrust.service.config.organization;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.xdi.config.oxauth.WebKeysSettings;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

@ApplicationScoped
public class WebKeySettingsService {

    @Inject
    private ConfigurationFactory configurationFactory;

    @Inject
    private OrganizationService organizationService;

    public WebKeysSettings find() {
        LdapOxAuthConfiguration ldapOxAuthConfiguration = ldapOxAuthConfiguration();
        WebKeysSettings webKeysSettings = ldapOxAuthConfiguration.getOxWebKeysSettings();

        if (webKeysSettings == null) {
            return new WebKeysSettings();
        }
        return webKeysSettings;
    }

    private LdapOxAuthConfiguration ldapOxAuthConfiguration() {
        String configurationDn = configurationFactory.getConfigurationDn();
        return organizationService.getOxAuthSetting(configurationDn);
    }

    public void save(WebKeysSettings webKeysSettings) {
        LdapOxAuthConfiguration ldapOxAuthConfiguration = ldapOxAuthConfiguration();

        webKeysSettings.setUpdateAt(new Date());
        ldapOxAuthConfiguration.setOxWebKeysSettings(webKeysSettings);

        organizationService.saveLdapOxAuthConfiguration(ldapOxAuthConfiguration);
    }

}

