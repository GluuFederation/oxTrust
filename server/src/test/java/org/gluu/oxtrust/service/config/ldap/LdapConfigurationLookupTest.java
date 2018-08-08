package org.gluu.oxtrust.service.config.ldap;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xdi.model.ldap.GluuLdapConfiguration;

import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LdapConfigurationLookupTest {

    private LdapConfigurationLookup ldapConfigurationLookup;

    @BeforeTest
    public void init() {
        ldapConfigurationLookup = new LdapConfigurationLookup(Arrays.asList(
                createGluuLdapConfiguration("C0", "pass0.encrypted"),
                createGluuLdapConfiguration("C1", "pass1.encrypted"),
                createGluuLdapConfiguration("C2", "pass2.encrypted")
        ));
    }

    @Test
    public void should_encrypt_password_when_changed() {
        assertTrue(ldapConfigurationLookup.shouldEncryptPassword(createGluuLdapConfiguration("C0", "newPass")));
    }

    @Test
    public void should_not_encrypt_password_when_already_encrypted() {
        assertFalse(ldapConfigurationLookup.shouldEncryptPassword(createGluuLdapConfiguration("C0", "pass0.encrypted")));
    }

    private GluuLdapConfiguration createGluuLdapConfiguration(String configId, String bindPassword) {
        GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
        ldapConfiguration.setConfigId(configId);
        ldapConfiguration.setBindPassword(bindPassword);
        return ldapConfiguration;
    }

}