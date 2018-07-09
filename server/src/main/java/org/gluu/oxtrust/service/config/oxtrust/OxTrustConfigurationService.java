package org.gluu.oxtrust.service.config.oxtrust;

import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.persist.PersistenceEntryManager;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.ImportPersonConfig;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.util.security.StringEncrypter;

import javax.inject.Inject;

public class OxTrustConfigurationService {

    public static final String HIDDEN_PASSWORD = "hidden";

    @Inject
    private OxTrustConfigurationExtractor oxTrustConfigurationExtractor;

    @Inject
    private PersistenceEntryManager persistenceEntryManager;

    @Inject
    private EncryptionService encryptionService;

    public AppConfiguration find() {
        LdapOxTrustConfiguration ldapOxTrustConfiguration = oxTrustConfigurationExtractor.extract();
        return ldapOxTrustConfiguration.getApplication();
    }

    public void save(AppConfiguration appConfiguration) {
        LdapOxTrustConfiguration ldapOxTrustConfiguration = oxTrustConfigurationExtractor.extract();
        handlePassword(ldapOxTrustConfiguration.getApplication(), appConfiguration);
        ldapOxTrustConfiguration.setApplication(appConfiguration);
        ldapOxTrustConfiguration.setRevision(revisited(ldapOxTrustConfiguration));
        persistenceEntryManager.merge(ldapOxTrustConfiguration);
    }

    private void handlePassword(AppConfiguration source, AppConfiguration result) {
        if (HIDDEN_PASSWORD.equals(result.getSvnConfigurationStorePassword())) {
            result.setSvnConfigurationStorePassword(source.getSvnConfigurationStorePassword());
        } else {
            String encrypted = encrypt(result.getSvnConfigurationStorePassword());
            result.setSvnConfigurationStorePassword(encrypted);
        }

        if (HIDDEN_PASSWORD.equals(result.getKeystorePassword())) {
            result.setKeystorePassword(source.getKeystorePassword());
        } else {
            String encrypted = encrypt(result.getKeystorePassword());
            result.setKeystorePassword(encrypted);
        }

        if (HIDDEN_PASSWORD.equals(result.getIdpSecurityKeyPassword())) {
            result.setIdpSecurityKeyPassword(source.getIdpSecurityKeyPassword());
        } else {
            String encrypted = encrypt(result.getIdpSecurityKeyPassword());
            result.setIdpSecurityKeyPassword(encrypted);
        }

        if (HIDDEN_PASSWORD.equals(result.getIdpBindPassword())) {
            result.setIdpBindPassword(source.getIdpBindPassword());
        } else {
            String encrypted = encrypt(result.getIdpBindPassword());
            result.setIdpBindPassword(encrypted);
        }

        if (HIDDEN_PASSWORD.equals(result.getCaCertsPassphrase())) {
            result.setCaCertsPassphrase(source.getCaCertsPassphrase());
        } else {
            String encrypted = encrypt(result.getCaCertsPassphrase());
            result.setCaCertsPassphrase(encrypted);
        }

        if (HIDDEN_PASSWORD.equals(result.getOxAuthClientPassword())) {
            result.setOxAuthClientPassword(source.getOxAuthClientPassword());
        } else {
            String encrypted = encrypt(result.getOxAuthClientPassword());
            result.setOxAuthClientPassword(encrypted);
        }
    }

    private String encrypt(String svnConfigurationStorePassword) {
        try {
            return encryptionService.encrypt(svnConfigurationStorePassword);
        } catch (StringEncrypter.EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(ImportPersonConfig importPersonConfig) {
        LdapOxTrustConfiguration ldapOxTrustConfiguration = oxTrustConfigurationExtractor.extract();
        ldapOxTrustConfiguration.setImportPersonConfig(importPersonConfig);
        ldapOxTrustConfiguration.setRevision(revisited(ldapOxTrustConfiguration));
        persistenceEntryManager.merge(ldapOxTrustConfiguration);
    }

    public void save(CacheRefreshConfiguration cacheRefreshConfiguration) {
        LdapOxTrustConfiguration ldapOxTrustConfiguration = oxTrustConfigurationExtractor.extract();
        ldapOxTrustConfiguration.setCacheRefresh(cacheRefreshConfiguration);
        ldapOxTrustConfiguration.setRevision(revisited(ldapOxTrustConfiguration));
        persistenceEntryManager.merge(ldapOxTrustConfiguration);
    }

    private long revisited(LdapOxTrustConfiguration ldapOxTrustConfiguration) {
        return ldapOxTrustConfiguration.getRevision() + 1;
    }
}