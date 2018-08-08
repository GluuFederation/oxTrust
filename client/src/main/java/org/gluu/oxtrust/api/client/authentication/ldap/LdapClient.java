package org.gluu.oxtrust.api.client.authentication.ldap;

import org.gluu.oxtrust.api.authorization.ldap.ConnectionStatusDTO;
import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.gluu.oxtrust.api.client.util.AbstractClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.util.List;

public class LdapClient extends AbstractClient<LdapConfigurationDTO> {

    private static final String PATH = "/restv1/api/configurations/auth/ldap";

    public LdapClient(Client client, String baseURI) {
        super(LdapConfigurationDTO.class, client, baseURI, PATH);
    }

    public List<LdapConfigurationDTO> read() {
        GenericType<List<LdapConfigurationDTO>> responseType = new GenericType<List<LdapConfigurationDTO>>() {
        };
        return webTarget.request().get(responseType);
    }

    public LdapConfigurationDTO update(LdapConfigurationDTO ldapConfigurationDTO) {
        GenericType<LdapConfigurationDTO> responseType = new GenericType<LdapConfigurationDTO>() {
        };
        return webTarget.request().put(Entity.json(ldapConfigurationDTO), responseType);
    }

    public LdapConfigurationDTO addConfiguration(LdapConfigurationDTO ldapConfigurationDTO) {
        GenericType<LdapConfigurationDTO> responseType = new GenericType<LdapConfigurationDTO>() {
        };
        return webTarget.request().post(Entity.json(ldapConfigurationDTO), responseType);
    }

    public List<LdapConfigurationDTO> removeConfiguration(String name) {
        GenericType<List<LdapConfigurationDTO>> responseType = new GenericType<List<LdapConfigurationDTO>>() {
        };
        return webTarget.path("/{name}").resolveTemplate("name", name).request().delete(responseType);
    }

    public ConnectionStatusDTO status(String name) {
        GenericType<ConnectionStatusDTO> responseType = new GenericType<ConnectionStatusDTO>() {
        };
        return webTarget.path("/{name}/status").resolveTemplate("name", name).request().get(responseType);
    }
}
