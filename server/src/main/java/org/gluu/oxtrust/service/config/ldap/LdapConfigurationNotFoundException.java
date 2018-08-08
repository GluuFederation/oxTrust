package org.gluu.oxtrust.service.config.ldap;

import org.apache.commons.lang3.StringUtils;

public class LdapConfigurationNotFoundException extends RuntimeException {

    private String name;

    LdapConfigurationNotFoundException(String name) {
        this.name = name;
    }

    LdapConfigurationNotFoundException() {
        this(StringUtils.EMPTY);
    }

    public String getName() {
        return name;
    }
}
