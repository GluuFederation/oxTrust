/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.Authorization;
import org.gluu.oxtrust.service.scim2.interceptor.RefAdjusted;

import static org.gluu.oxtrust.model.scim2.Constants.QUERY_PARAM_ATTRIBUTES;

/**
 * SCIM Bulk Endpoint Implementation
 * 
 * @author Rahat ALi Date: 05.08.2015
 * Re-engineered by jgomer on 2017-11-27.
 */
@Named("scim2BulkEndpoint")
@Path("/scim/v2/Bulk")
@Api(value = "/v2/Bulk", description = "SCIM 2.0 Bulk Endpoint (https://tools.ietf.org/html/rfc7644#section-3.7)",
        authorizations = {@Authorization(value = "Authorization", type = "uma") })
public class BulkWebService extends BaseScimWebService {
//TODO: turn code back later

    @Inject
    UserWebService userWebService;


    @PostConstruct
    public void setup(){
        //Do not use getClass() here... a typical weld issue...
        endpointUrl=appConfiguration.getBaseEndpoint() + BulkWebService.class.getAnnotation(Path.class).value();
    }

}
