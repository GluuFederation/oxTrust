package org.gluu.oxtrust.service.config.ldap;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.security.StringEncrypter;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;

public class LdapConfigurationServiceTest {

    private LdapConfigurationService ldapConfigurationService;

    @BeforeMethod
    public void init() {
        EncryptionService encryptionServiceStub = new EncryptionService() {
            @Override
            public String encrypt(String unencryptedString) throws StringEncrypter.EncryptionException {
                return unencryptedString + ".encrypted";
            }
        };

        ApplianceService applianceServiceStub = new ApplianceService() {

            GluuAppliance gluuAppliance = initGluuAppliance();

            private GluuAppliance initGluuAppliance() {
                GluuAppliance gluuAppliance = new GluuAppliance();
                List<OxIDPAuthConf> oxIDPAuthConfs = Arrays.asList(
                        oxIDPAuthConf("auth_ldap_server"),
                        oxIDPAuthConf("auth_couchbase_server")
                );
                gluuAppliance.setOxIDPAuthentication(oxIDPAuthConfs);
                return gluuAppliance;
            }

            private OxIDPAuthConf oxIDPAuthConf(String name) {
                OxIDPAuthConf oxIDPAuthConf = new OxIDPAuthConf();
                oxIDPAuthConf.setType("auth");
                oxIDPAuthConf.setConfig("{\"configId\":\"" + name + "\",\"bindDN\":\"cn=directory manager\",\"bindPassword\":\"+mY9R2cYNIE=\",\"servers\":[\"localhost:1636\"],\"maxConnections\":1000,\"useSSL\":true,\"baseDNs\":[\"o=gluu\"],\"primaryKey\":\"uid\",\"localPrimaryKey\":\"uid\",\"useAnonymousBind\":false,\"enabled\":true,\"version\":0,\"level\":0}");
                return oxIDPAuthConf;
            }

            @Override
            public GluuAppliance getAppliance() {
                return gluuAppliance;
            }

            @Override
            public void updateAppliance(GluuAppliance appliance) {
                this.gluuAppliance = appliance;
            }

        };

        ldapConfigurationService = new LdapConfigurationService();
        try {
            writeField(ldapConfigurationService, "applianceService", applianceServiceStub, true);
            writeField(ldapConfigurationService, "encryptionService", encryptionServiceStub, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void find_by_name_should_return_first_matching_configuration() {
        assertEquals(2, ldapConfigurationService.findLdapConfigurations().size());

        assertNotNull(ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server"));
        assertEquals("auth_ldap_server", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getConfigId());

        assertNotNull(ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server"));
        assertEquals("auth_couchbase_server", ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server").getConfigId());
    }

    @Test(expectedExceptions = {LdapConfigurationNotFoundException.class})
    public void find_by_name_should_fail_for_no_matching_configuration() {
        assertNull(ldapConfigurationService.findLdapConfigurationByName("auth_mongo_server"));
    }

    @Test
    public void should_update_only_one_configuration() {
        GluuLdapConfiguration gluuLdapConfiguration = new GluuLdapConfiguration();
        gluuLdapConfiguration.setConfigId("auth_ldap_server");
        gluuLdapConfiguration.setBindDN("cn=directory manager");
        gluuLdapConfiguration.setBindPassword("+mY9R2cYNIE=");
        gluuLdapConfiguration.setServersStringsList(Arrays.asList("localhost:1636"));
        gluuLdapConfiguration.setBaseDNsStringsList(Arrays.asList("o=gluu"));
        gluuLdapConfiguration.setMaxConnections(1000);
        gluuLdapConfiguration.setUseSSL(true);
        gluuLdapConfiguration.setPrimaryKey("newUid");
        gluuLdapConfiguration.setLocalPrimaryKey("newUid");
        gluuLdapConfiguration.setUseAnonymousBind(false);
        gluuLdapConfiguration.setEnabled(true);
        gluuLdapConfiguration.setVersion(0);
        gluuLdapConfiguration.setLevel(0);
        ldapConfigurationService.update(gluuLdapConfiguration);

        assertEquals(2, ldapConfigurationService.findLdapConfigurations().size());
        assertNotNull(ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server"));
        assertNotNull(ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server"));
        assertEquals("newUid", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getLocalPrimaryKey());
        assertEquals("newUid", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getPrimaryKey());
    }

    @Test
    public void should_not_update_password_if_not_changed() {
        GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server");
        gluuLdapConfiguration.setBindPassword("+mY9R2cYNIE=");

        ldapConfigurationService.update(gluuLdapConfiguration);

        assertEquals("+mY9R2cYNIE=", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getBindPassword());
    }

    @Test
    public void should_encrypt_password_if_changed() {
        GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server");
        gluuLdapConfiguration.setBindPassword("password");

        ldapConfigurationService.update(gluuLdapConfiguration);

        assertEquals("password.encrypted", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getBindPassword());
        assertEquals("+mY9R2cYNIE=", ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server").getBindPassword());
    }

    @Test
    public void should_encrypt_password_when_new_configuration() {
        GluuLdapConfiguration gluuLdapConfiguration = new GluuLdapConfiguration();
        gluuLdapConfiguration.setConfigId("auth_mongo_server");
        gluuLdapConfiguration.setBindDN("cn=directory manager");
        gluuLdapConfiguration.setBindPassword("password");
        gluuLdapConfiguration.setServersStringsList(Arrays.asList("localhost:1636"));
        gluuLdapConfiguration.setBaseDNsStringsList(Arrays.asList("o=gluu"));
        gluuLdapConfiguration.setMaxConnections(1000);
        gluuLdapConfiguration.setUseSSL(true);
        gluuLdapConfiguration.setPrimaryKey("uid");
        gluuLdapConfiguration.setLocalPrimaryKey("uid");
        gluuLdapConfiguration.setUseAnonymousBind(false);
        gluuLdapConfiguration.setEnabled(true);
        gluuLdapConfiguration.setVersion(0);
        gluuLdapConfiguration.setLevel(0);
        ldapConfigurationService.save(gluuLdapConfiguration);

        assertEquals(3, ldapConfigurationService.findLdapConfigurations().size());
        assertEquals("+mY9R2cYNIE=", ldapConfigurationService.findLdapConfigurationByName("auth_ldap_server").getBindPassword());
        assertEquals("+mY9R2cYNIE=", ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server").getBindPassword());
        assertEquals("password.encrypted", ldapConfigurationService.findLdapConfigurationByName("auth_mongo_server").getBindPassword());
    }

    @Test(expectedExceptions = {LdapConfigurationNotFoundException.class})
    public void should_fail_when_remove_non_existing_configuration() {
        ldapConfigurationService.remove("auth_mongo_server");
    }

    @Test
    public void should_remove_configuration_by_name() {
        assertEquals(2, ldapConfigurationService.findLdapConfigurations().size());

        ldapConfigurationService.remove("auth_ldap_server");
        assertEquals(1, ldapConfigurationService.findLdapConfigurations().size());
        assertNotNull(ldapConfigurationService.findLdapConfigurationByName("auth_couchbase_server").getBindPassword());
    }

}