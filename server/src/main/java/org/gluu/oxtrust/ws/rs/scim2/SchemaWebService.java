/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import javax.inject.Named;
import javax.ws.rs.Path;

import com.wordnik.swagger.annotations.Api;

/**
 * Web service for the /Schemas endpoint.
 *
 * @author Val Pecaoco
 * Updated by jgomer on 2017-09-27.
 */
@Named("scim2SchemaEndpoint")
@Path("/scim/v2/Schemas")
@Api(value = "/v2/Schemas", description = "SCIM 2.0 Schema Endpoint (https://tools.ietf.org/html/rfc7643#section-4)")
public class SchemaWebService extends BaseScimWebService {
    //TODO: turn code back later
}
