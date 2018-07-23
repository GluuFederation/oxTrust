/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.registration.management.test;


import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.api.client.RegistrationManagementRequest;
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
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.gluu.oxtrust.api.client.RegistrationManagementResponse.fromJson;
import static org.testng.Assert.*;

/**
 * Registration management service' test class  {@link org.gluu.oxtrust.api.RegistrationManagementService}
 *
 * @author Shoeb Khan
 * @version 19 July, 2018
 */
public class ManagementRegServiceTest extends BaseTest {

    public static final String API_URL = OxTrustApiConstants.BASE_API_URL + "/configurations/registration";
    @Inject
    private ArquillianResource url;

    /**
     * Test case for getting the registration configuration info, without search pattern
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
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE);
        Response r1 = builder.get();
        assertEquals(r1.getStatus(), 200);
        final String responseJson = r1.readEntity(String.class);
        assertNotNull(responseJson);
        RegistrationManagementResponse managementResponseObj = fromJson(responseJson);
        assertNotNull(managementResponseObj);
        assertNotNull(managementResponseObj.getAttributes());
        assertNotNull(managementResponseObj.getCaptchaDisabled());
    }

    /**
     * Test case for getting the registration configuration info, WITH search pattern
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
    public void readConfigurationWithQueryParam(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) throws IOException {
        webTarget = webTarget.queryParam("searchPattern", "active");
        Invocation.Builder builder = webTarget
                .path(API_URL)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE);

        Response r1 = builder.get();
        assertEquals(r1.getStatus(), 200);
        final  String responseJson = r1.readEntity(String.class);
        assertNotNull(responseJson);
        RegistrationManagementResponse managementResponseObj = fromJson(responseJson);
        assertNotNull(managementResponseObj);
        assertNotNull(managementResponseObj.getAttributes());
        assertNotNull(managementResponseObj.getSelectedAttributes());
        assertTrue(managementResponseObj.getSelectedAttributes().size() > 0);

    }

    @RunAsClient
    @Parameters({"webTarget"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void saveConfiguration(@Optional @ArquillianResteasyResource("restv1") WebTarget webTarget) throws IOException {
        Invocation.Builder builder = webTarget
                .path(API_URL)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE);
        Response r1 = builder.get();
        assertEquals(r1.getStatus(), 200);
        final String responseJson = r1.readEntity(String.class);
        RegistrationManagementResponse managementResponseObj = fromJson(responseJson);
        Boolean prevCaptcha = managementResponseObj.getCaptchaDisabled();
        System.out.println("Captcha Status:" + managementResponseObj.getCaptchaDisabled());
        RegistrationManagementRequest request = new RegistrationManagementRequest();
        request.setCaptchaDisabled(!managementResponseObj.getCaptchaDisabled());
        builder = webTarget
                .path(API_URL)
                .request(MediaType.APPLICATION_JSON_TYPE);

        final Entity<String> requestEntity = Entity.entity(request.toJsonString(), MediaType.APPLICATION_JSON_TYPE);

        r1 = builder.put(requestEntity);
        assertEquals(r1.getStatus(), 200);
        builder = webTarget
                .path(API_URL)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE);
        r1 = builder.get();
        managementResponseObj = fromJson(r1.readEntity(String.class));
        Boolean newCaptcha = managementResponseObj.getCaptchaDisabled();
        assertNotEquals(newCaptcha, prevCaptcha);
    }
}
