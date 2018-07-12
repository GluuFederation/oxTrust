/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api.attributes.test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.api.GluuAttributeApi;
import org.gluu.oxtrust.api.proxy.AttributeProxy;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @author Yuriy Movchan
 *
 * @author Yuriy Movchan Date: 07/12/2018
 */
public class AttributeWebResourceTest extends BaseTest {

    @ArquillianResource
    private URI url;

    /**
     * Get all attributes via WebTarget
     */
    @RunAsClient
    @Parameters({ "webTarget" })
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void loadAttributes1(@Optional @ArquillianResteasyResource("restv1") final WebTarget webTarget) {

        Invocation.Builder builder= webTarget.path("/api/attributes").request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        List<GluuAttributeApi> allAttributes = builder.get(List.class);
        
        assertNotNull(allAttributes, "Failed to get all attributes");
        assertTrue(allAttributes.size() > 0, "List with all attributes should be not empty");
    }

    /**
     * Get all attributes via ResteasyWebTarget
     */
    @RunAsClient
    @Parameters({ "webTarget" })
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void loadAttributes2(@Optional @ArquillianResteasyResource("restv1") final ResteasyWebTarget webTarget) {
        AttributeProxy attributeProxy = webTarget.proxy(AttributeProxy.class);
        List<GluuAttributeApi> allAttributes = attributeProxy.getAllAttributes();

        assertNotNull(allAttributes, "Failed to get all attributes");
        assertTrue(allAttributes.size() > 0, "List with all attributes should be not empty");
    }

    /**
     * Get all attributes via Proxy
     */
    @RunAsClient
    @Parameters({ "attributeProxy" })
    @Consumes(MediaType.APPLICATION_JSON)
    @Test
    public void loadAttributes3(@Optional @ArquillianResteasyResource("restv1") final AttributeProxy attributeProxy) {
        List<GluuAttributeApi> allAttributes = attributeProxy.getAllAttributes();

        assertNotNull(allAttributes, "Failed to get all attributes");
        assertTrue(allAttributes.size() > 0, "List with all attributes should be not empty");
    }

}