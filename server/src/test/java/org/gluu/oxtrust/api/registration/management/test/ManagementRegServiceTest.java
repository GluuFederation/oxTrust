package org.gluu.oxtrust.api.registration.management.test;


import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.api.client.RegistrationManagementResponse;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.gluu.oxtrust.api.client.RegistrationManagementResponse.fromJson;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Registration management service' test class
 *
 * @author Shoeb Khan
 * @version 19 July, 2018
 */
public class ManagementRegServiceTest extends BaseTest {

    @Inject
    private ArquillianResource url;

    /**
     * Test case for getting the registration configuration info from the {@link org.gluu.oxtrust.api.RegistrationManagementService}
     *
     *
     * @param webTarget
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RunAsClient
    @Parameters({"webTarget"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void readConfiguration(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) throws ExecutionException, InterruptedException, IOException {

        Invocation.Builder builder = webTarget
                .path(OxTrustApiConstants.BASE_API_URL + "/configurations/registration")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
        AsyncInvoker asyncInvoker = builder.async();
        Future<Response> futureResponse = asyncInvoker.get();
        Response r1 = futureResponse.get(); // blocked till the response is completed.
        assertEquals(r1.getStatus(), 200);
        final String json = r1.readEntity(String.class);
        assertNotNull(json);
        RegistrationManagementResponse managementResponseObj = fromJson(json);
        assertNotNull(managementResponseObj);
        assertNotNull(managementResponseObj.getAttributes());
        assertNotNull(managementResponseObj.getCaptchaDisabled());

    }
}
