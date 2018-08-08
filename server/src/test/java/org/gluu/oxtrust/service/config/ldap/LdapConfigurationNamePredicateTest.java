package org.gluu.oxtrust.service.config.ldap;

import org.testng.annotations.Test;
import org.xdi.model.ldap.GluuLdapConfiguration;

import static org.testng.Assert.*;

public class LdapConfigurationNamePredicateTest {

    @Test
    public void should_match_name() {
        final String name = "Mehdi";

        LdapConfigurationNamePredicate ldapConfigurationNamePredicate = new LdapConfigurationNamePredicate(name);
        GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
        ldapConfiguration.setConfigId(name);
        assertTrue(ldapConfigurationNamePredicate.apply(ldapConfiguration));
    }

    @Test
    public void should_not_match_name() {
        LdapConfigurationNamePredicate ldapConfigurationNamePredicate = new LdapConfigurationNamePredicate("Mehdi");
        GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
        ldapConfiguration.setConfigId("AREZKI");
        assertFalse(ldapConfigurationNamePredicate.apply(ldapConfiguration));
    }

}