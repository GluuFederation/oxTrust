package org.gluu.oxtrust.api.authentication.ldap;

import com.google.common.base.Function;
import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.xdi.model.ldap.GluuLdapConfiguration;

class LdapConfigurationDTOTransformer implements Function<GluuLdapConfiguration, LdapConfigurationDTO> {
    private LdapConfigurationDtoAssembly ldapConfigurationDtoAssembly = new LdapConfigurationDtoAssembly();

    static LdapConfigurationDTOTransformer INSTANCE = new LdapConfigurationDTOTransformer();

    @Override
    public LdapConfigurationDTO apply(GluuLdapConfiguration ldapConfiguration) {
        return ldapConfigurationDtoAssembly.toDto(ldapConfiguration);
    }
}