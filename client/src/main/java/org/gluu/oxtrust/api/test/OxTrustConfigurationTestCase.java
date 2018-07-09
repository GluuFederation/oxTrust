package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.configuration.oxauth.OxAuthConfig;
import org.gluu.oxtrust.api.configuration.oxtrust.OxTrustConfig;

public class OxTrustConfigurationTestCase {

    private final OxTrustClient client;

    public OxTrustConfigurationTestCase(OxTrustClient client) {
        this.client = client;
    }

    public void run() throws APITestException {
        OxTrustConfig oxTrustConfig = client.getOxTrustConfClient().read();
        if (oxTrustConfig == null) {
            throw new APITestException("read conf failed!");
        }

        // assure no-errors.
        client.getOxTrustConfClient().formDefinition();
    }

}
