package org.gluu.oxtrust.service.config.ldap;

import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.security.StringEncrypter;

import java.util.List;

class LdapConfigurationLookup {

    private final List<GluuLdapConfiguration> ldapConfigurations;

    LdapConfigurationLookup(List<GluuLdapConfiguration> ldapConfigurations) {
        this.ldapConfigurations = ldapConfigurations;
    }

    boolean shouldEncryptPassword(GluuLdapConfiguration ldapConfiguration) {
        try {
            GluuLdapConfiguration oldConfiguration = findByName(ldapConfiguration.getConfigId());
            String encryptedOldPassword = oldConfiguration.getBindPassword();
            return !StringUtils.equals(encryptedOldPassword, ldapConfiguration.getBindPassword());
        } catch (LdapConfigurationNotFoundException e) {
            return true;
        }
    }

    GluuLdapConfiguration findByName(final String name) {
        return FluentIterable.from(ldapConfigurations)
                .filter(new LdapConfigurationNamePredicate(name))
                .first()
                .or(notFound(name));
    }

    private Supplier<GluuLdapConfiguration> notFound(final String name) {
        return new Supplier<GluuLdapConfiguration>() {
            @Override
            public GluuLdapConfiguration get() {
                throw new LdapConfigurationNotFoundException(name);
            }
        };
    }

    private String encrypt(EncryptionService encryptionService, String data) {
        try {
            return encryptionService.encrypt(data);
        } catch (StringEncrypter.EncryptionException e) {
            throw new LdapConfigurationException(e);
        }
    }

}