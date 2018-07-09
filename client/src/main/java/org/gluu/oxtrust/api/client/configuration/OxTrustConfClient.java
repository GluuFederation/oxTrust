package org.gluu.oxtrust.api.client.configuration;

import org.gluu.oxtrust.api.client.util.AbstractClient;
import org.gluu.oxtrust.api.configuration.oxtrust.OxTrustConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

public class OxTrustConfClient extends AbstractClient<OxTrustConfig> {

    private static final String PATH = "/restv1/api/configurations/oxtrust";

    public OxTrustConfClient(Client client, String baseURI) {
        super(OxTrustConfig.class, client, baseURI, PATH);
    }

    public OxTrustConfig read() {
        GenericType<OxTrustConfig> responseType = new GenericType<OxTrustConfig>() {
        };
        return webTarget.request().get(responseType);
    }

    public Object formDefinition() {
        return webTarget.path("/form-definition")
                .request().get();
    }
}
