package org.gluu.oxtrust.api.authentication.ldap;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.codec.binary.StringUtils;
import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.gluu.oxtrust.service.config.ldap.LdapConfigurationService;
import org.xdi.model.ldap.GluuLdapConfiguration;

import javax.inject.Inject;

import static org.gluu.oxtrust.util.CollectionsUtil.equalsUnordered;

class ExistingLdapConfigurationValidator {

    @Inject
    private LdapConfigurationService ldapConfigurationService;

    boolean isInvalid(LdapConfigurationDTO ldapConfiguration) {
        return FluentIterable.from(ldapConfigurationService.findLdapConfigurations())
                .anyMatch(havingSamePropertiesAs(ldapConfiguration));
    }

    private Predicate<GluuLdapConfiguration> havingSamePropertiesAs(final LdapConfigurationDTO ldapConfiguration) {
        return new Predicate<GluuLdapConfiguration>() {
            @Override
            public boolean apply(GluuLdapConfiguration gluuLdapConfiguration) {
                return StringUtils.equals(ldapConfiguration.getConfigId(), gluuLdapConfiguration.getConfigId()) ||
                        equalsUnordered(ldapConfiguration.getServers(), gluuLdapConfiguration.getServersStringsList());
            }
        };
    }

}