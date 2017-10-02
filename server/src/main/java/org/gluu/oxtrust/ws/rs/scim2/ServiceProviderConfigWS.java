/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.util.*;

import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.model.scim2.provider.AuthenticationScheme;
import org.gluu.oxtrust.model.scim2.provider.ServiceProviderConfig;
import org.gluu.oxtrust.model.scim2.user.Meta;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * @author Rahat Ali Date: 05.08.2015
 * Updated by jgomer2001 on 2017-09-23
 */
@Named("serviceProviderConfig")
@Path("/scim/v2/ServiceProviderConfig")
public class ServiceProviderConfigWS extends BaseScimWebService {

    @GET
    @Produces(MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT)
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    public Response serve() throws Exception {

        ServiceProviderConfig serviceProviderConfig = new ServiceProviderConfig();

        Meta meta = new Meta();
        meta.setLocation(appConfiguration.getBaseEndpoint() + getClass().getAnnotation(Path.class).value());
        meta.setResourceType("ServiceProviderConfig");
        serviceProviderConfig.setMeta(meta);

        boolean onTestMode=appConfiguration.isScimTestMode();
        serviceProviderConfig.setAuthenticationSchemes(Arrays.asList(
                AuthenticationScheme.createOAuth2(onTestMode), AuthenticationScheme.createUma(!onTestMode)));

        return Response.ok(resourceSerializer.serialize(serviceProviderConfig)).build();

    }

}
