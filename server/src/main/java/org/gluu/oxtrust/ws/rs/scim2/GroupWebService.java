/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.ws.rs.Path;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.Authorization;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Named("scim2GroupEndpoint")
@Path("/scim/v2/Groups")
@Api(value = "/v2/Groups", description = "SCIM 2.0 Group Endpoint (https://tools.ietf.org/html/rfc7644#section-3.2)",
        authorizations = {@Authorization(value = "Authorization", type = "uma")})
public class GroupWebService extends BaseScimWebService {
    //TODO: turn code back later


    @PostConstruct
    public void setup(){
        //Do not use getClass() here... a typical weld issue...
        endpointUrl=appConfiguration.getBaseEndpoint() + GroupWebService.class.getAnnotation(Path.class).value();
    }

}
