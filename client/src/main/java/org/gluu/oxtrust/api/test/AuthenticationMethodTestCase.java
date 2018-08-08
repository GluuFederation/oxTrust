package org.gluu.oxtrust.api.test;

import org.apache.commons.collections.CollectionUtils;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.AuthenticationMethodDTO;
import org.gluu.oxtrust.api.authorization.ldap.ConnectionStatusDTO;
import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.logs.LogFileApi;
import org.gluu.oxtrust.api.logs.LogFileDefApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuthenticationMethodTestCase {

    private final OxTrustClient client;

    public AuthenticationMethodTestCase(OxTrustClient client) {
        this.client = client;
    }

    public void run() throws APITestException {
        List<LdapConfigurationDTO> ldapConfigurations = client.getLdapClient().read();
        if (CollectionUtils.isEmpty(ldapConfigurations)) {
            throw new APITestException("configuration is null");
        }

        ldapConfigurations.get(0).setBindPassword("nimda");
        LdapConfigurationDTO updatedLdapConfiguration = client.getLdapClient().update(ldapConfigurations.get(0));
        if (updatedLdapConfiguration == null) {
            throw new APITestException("configuration is null");
        }

        LdapConfigurationDTO toInsert = ldapConfigurations.get(0);
        toInsert.setBindPassword("nimda");
        toInsert.setConfigId("couchbase");
        toInsert.setServers(Collections.singletonList("localhost:9999"));
        LdapConfigurationDTO insertedLdapConfiguration = client.getLdapClient().addConfiguration(toInsert);
        if (insertedLdapConfiguration == null) {
            throw new APITestException("configuration is null");
        }

        ldapConfigurations = client.getLdapClient().read();
        if (ldapConfigurations.size() != 2) {
            throw new APITestException("Error inserting");
        }

        ldapConfigurations = client.getLdapClient().removeConfiguration(insertedLdapConfiguration.getConfigId());
        if (ldapConfigurations.size() != 1) {
            throw new APITestException("Error deleting");
        }

        ldapConfigurations = client.getLdapClient().read();
        ConnectionStatusDTO status = client.getLdapClient().status(ldapConfigurations.get(0).getConfigId());
        if (!status.isUp()) {
            throw new APITestException("Ldap is down");
        }


        AuthenticationMethodDTO authenticationMethodDTO = client.getDefaultAuthenticationClient().read();
        if (authenticationMethodDTO == null) {
            throw new APITestException("configuration is null");
        }

        AuthenticationMethodDTO updatedAuthenticationMethodDTO = client.getDefaultAuthenticationClient().update(authenticationMethodDTO);
        if (updatedAuthenticationMethodDTO == null) {
            throw new APITestException("configuration is null");
        }
    }
}
