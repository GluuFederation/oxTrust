package org.gluu.oxtrust.api.client.configuration;

import org.gluu.oxtrust.api.client.util.AbstractClient;
import org.gluu.oxtrust.api.configuration.OxAuthConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

public class OxAuthConfClient extends AbstractClient<OxAuthConfig> {

    private static final String PATH = "/restv1/api/oxauth/configuration";

    public OxAuthConfClient(Client client, String baseURI) {
        super(OxAuthConfig.class, client, baseURI, PATH);
    }

    public OxAuthConfig read() {
        GenericType<OxAuthConfig> responseType = new GenericType<OxAuthConfig>() {
        };
        return webTarget.request().get(responseType);
    }

    public Object formDefinition() {
        return webTarget.path("/form-definition")
                .request().get();
    }
}
