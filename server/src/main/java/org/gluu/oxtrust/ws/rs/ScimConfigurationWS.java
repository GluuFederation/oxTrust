/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.scim.ScimConfiguration;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.UmaConstants;
import org.xdi.service.JsonService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The endpoint at which the requester can obtain SCIM metadata configuration.
 *
 * @author Yuriy Movchan Date: 11/06/2015
 */
@Named("scimConfigurationRestWebService")
@Path("/oxtrust/scim-configuration")
@Api(value = "/.well-known/scim-configuration", description = "The SCIM server endpoint that provides configuration data. ")
public class ScimConfigurationWS {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationFactory configurationFactory;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private JsonService jsonService;

    @GET
    @Produces({UmaConstants.JSON_MEDIA_TYPE})
    @ApiOperation(
            value = "Provides configuration data as json document. It contains options and endpoints supported by the SCIM server.",
            response = UmaConfiguration.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Failed to build SCIM configuration json object.")
    })
    public Response getConfiguration() {
        try {
            final String baseEndpointUri = appConfiguration.getBaseEndpoint();

            final List<ScimConfiguration> cl = new ArrayList<ScimConfiguration>();

//            cl.setScimConfigurationList(new ArrayList<ScimConfiguration>());

            // SCIM 1.0
            final ScimConfiguration c1 = new ScimConfiguration();
            c1.setVersion("1.0");
            c1.setAuthorizationSupported(new String[]{"uma"});
            c1.setUserEndpoint(baseEndpointUri + "/scim/v1/Users");
            c1.setUserSearchEndpoint(baseEndpointUri + "/scim/v1/Users/Search");
            c1.setGroupEndpoint(baseEndpointUri + "/scim/v1/Groups");
            c1.setBulkEndpoint(baseEndpointUri + "/scim/v1/Bulk");

            cl.add(c1);

            // SCIM 2.0
            final ScimConfiguration c2 = new ScimConfiguration();
            c2.setVersion("2.0");
            c2.setAuthorizationSupported(new String[]{"uma"});
            c2.setUserEndpoint(baseEndpointUri + "/scim/v2/Users");
            c2.setUserSearchEndpoint(baseEndpointUri + "/scim/v2/Users/Search");
            c2.setGroupEndpoint(baseEndpointUri + "/scim/v2/Groups");
            c2.setBulkEndpoint(baseEndpointUri + "/scim/v2/Bulk");
            c2.setServiceProviderEndpoint(baseEndpointUri + "/scim/v2/ServiceProviderConfig");
            c2.setResourceTypesEndpoint(baseEndpointUri + "/scim/v2/ResourceTypes");

            cl.add(c2);

            // Convert manually to avoid possible conflicts between resteasy providers, e.g. jettison, jackson
            final String entity = jsonService.objectToPerttyJson(cl);
            log.trace("SCIM configuration: {0}", entity);

            return Response.ok(entity).build();
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to generate SCIM configuration").build());
        }
    }

}
