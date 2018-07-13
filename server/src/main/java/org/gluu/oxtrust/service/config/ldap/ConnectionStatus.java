package org.gluu.oxtrust.service.config.ldap;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.persist.ldap.operation.impl.LdapConnectionProvider;
import org.xdi.util.properties.FileConfiguration;
import org.xdi.util.security.PropertiesDecrypter;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class ConnectionStatus {
    @Inject
    private ConfigurationFactory configurationFactory;

    public boolean isUp(LdapConnectionData ldapConnectionData) {
        FileConfiguration configuration = loadFileConfiguration();
        Properties properties = configuration.getProperties();
        properties.setProperty("bindDN", ldapConnectionData.getBindDN());
        properties.setProperty("bindPassword", ldapConnectionData.getBindPassword());
        properties.setProperty("servers", buildServersString(ldapConnectionData.getServers()));
        properties.setProperty("useSSL", Boolean.toString(ldapConnectionData.isUseSSL()));

        LdapConnectionProvider connectionProvider = new LdapConnectionProvider(
                PropertiesDecrypter.decryptProperties(properties, configurationFactory.getCryptoConfigurationSalt()));
        if (connectionProvider.getConnectionPool() != null) {
            boolean isConnected = connectionProvider.isConnected();
            connectionProvider.closeConnectionPool();
            return isConnected;
        }
        return false;
    }

    private FileConfiguration loadFileConfiguration() {
        FileConfiguration configuration = new FileConfiguration(ConfigurationFactory.LDAP_PROPERTIES_FILE);
        if (!configuration.isLoaded()) {
            configuration = new FileConfiguration(ConfigurationFactory.LDAP_DEFAULT_PROPERTIES_FILE);
        }
        return configuration;
    }

    private String buildServersString(List<String> servers) {
        if (servers == null) {
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (String server : servers) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append(server);
        }

        return sb.toString();
    }
}