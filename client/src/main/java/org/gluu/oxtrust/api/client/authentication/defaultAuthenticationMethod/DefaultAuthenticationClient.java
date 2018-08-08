package org.gluu.oxtrust.api.client.authentication.defaultAuthenticationMethod;

import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.AuthenticationMethodDTO;
import org.gluu.oxtrust.api.client.util.AbstractClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

public class DefaultAuthenticationClient extends AbstractClient<AuthenticationMethodDTO> {

    private static final String PATH = "/restv1/api/configurations/auth/auth-method";

    public DefaultAuthenticationClient(Client client, String baseURI) {
        super(AuthenticationMethodDTO.class, client, baseURI, PATH);
    }

    public AuthenticationMethodDTO read() {
        GenericType<AuthenticationMethodDTO> responseType = new GenericType<AuthenticationMethodDTO>() {
        };
        return webTarget.request().get(responseType);
    }

    public AuthenticationMethodDTO update(AuthenticationMethodDTO ldapConfigurationDTO) {
        GenericType<AuthenticationMethodDTO> responseType = new GenericType<AuthenticationMethodDTO>() {
        };
        return webTarget.request().put(Entity.json(ldapConfigurationDTO), responseType);
    }

}
