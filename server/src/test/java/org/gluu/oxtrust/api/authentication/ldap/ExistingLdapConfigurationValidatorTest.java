package org.gluu.oxtrust.api.authentication.ldap;

import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.gluu.oxtrust.service.config.ldap.LdapConfigurationService;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xdi.model.ldap.GluuLdapConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.testng.Assert.*;

public class ExistingLdapConfigurationValidatorTest {

    private LdapConfigurationService ldapConfigurationService;
    private ExistingLdapConfigurationValidator validator;

    @BeforeTest
    public void init() {
        ldapConfigurationService = new LdapConfigurationService() {
            @Override
            public List<GluuLdapConfiguration> findLdapConfigurations() {
                return Arrays.asList(
                        gluuLdapConfiguration("ldap_config_0", Arrays.asList("localhost:8888")),
                        gluuLdapConfiguration("ldap_config_1", Arrays.asList("localhost:8889")),
                        gluuLdapConfiguration("ldap_config_2", Arrays.asList("localhost:8890")),
                        gluuLdapConfiguration("ldap_config_3", Arrays.asList("localhost:8891")),
                        gluuLdapConfiguration("ldap_config_4", Arrays.asList("localhost:8892")),
                        gluuLdapConfiguration("ldap_config_4", Arrays.asList("localhost:8893", "localhost:8894"))
                );
            }

            private GluuLdapConfiguration gluuLdapConfiguration(String name, List<String> servers) {
                GluuLdapConfiguration ldapConfiguration = new GluuLdapConfiguration();
                ldapConfiguration.setConfigId(name);
                ldapConfiguration.setServersStringsList(servers);
                return ldapConfiguration;
            }
        };

        validator = new ExistingLdapConfigurationValidator();
        try {
            writeField(validator, "ldapConfigurationService", ldapConfigurationService, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_be_valid_for_non_existing_configuration() {
        assertFalse(validator.isInvalid(ldapConfigurationDto("ldap_config", Arrays.asList("localhost"))));
        assertFalse(validator.isInvalid(ldapConfigurationDto("ldap_config_x", Arrays.asList("gluu.ldap:8888"))));
        assertFalse(validator.isInvalid(ldapConfigurationDto("ldap_config_y", Arrays.asList("localhost:9991"))));
    }

    @Test
    public void should_be_invalid_for_existing_names() {
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_0", Arrays.asList("localhost"))));
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_1", Arrays.asList("gluu.ldap:8888"))));
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_2", Arrays.asList("localhost:9991"))));
    }

    @Test
    public void should_be_invalid_for_existing_servers() {
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config", Arrays.asList("localhost:8889"))));
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_x", Arrays.asList("localhost:8890"))));
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_y", Arrays.asList("localhost:8891"))));
        assertTrue(validator.isInvalid(ldapConfigurationDto("ldap_config_z", Arrays.asList("localhost:8893", "localhost:8894"))));
    }

    private LdapConfigurationDTO ldapConfigurationDto(String name, List<String> servers) {
        LdapConfigurationDTO ldapConfiguration = new LdapConfigurationDTO();
        ldapConfiguration.setConfigId(name);
        ldapConfiguration.setServers(servers);
        return ldapConfiguration;
    }

}