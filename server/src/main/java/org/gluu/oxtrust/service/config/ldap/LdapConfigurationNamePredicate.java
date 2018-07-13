package org.gluu.oxtrust.service.config.ldap;

import com.google.common.base.Predicate;
import org.apache.commons.codec.binary.StringUtils;
import org.xdi.model.ldap.GluuLdapConfiguration;

class LdapConfigurationNamePredicate implements Predicate<GluuLdapConfiguration> {

    private final String name;

    LdapConfigurationNamePredicate(GluuLdapConfiguration ldapConfiguration) {
        this(ldapConfiguration.getConfigId());
    }

    LdapConfigurationNamePredicate(String name) {
        this.name = name;
    }

    @Override
    public boolean apply(GluuLdapConfiguration ldapConfiguration) {
        return StringUtils.equals(ldapConfiguration.getConfigId(), name);
    }
}