package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.configuration.OxAuthConfig;

public class OxAuthConfigurationTestCase {

    private final OxTrustClient client;

    public OxAuthConfigurationTestCase(OxTrustClient client) {
        this.client = client;
    }

    public void run() throws APITestException {
        OxAuthConfig oxAuthConfiguration = client.getOxAuthConfClient().read();
        if (oxAuthConfiguration == null) {
            throw new APITestException("read conf failed!");
        }

        // assure no-errors.
        client.getOxAuthConfClient().formDefinition();
    }

}
