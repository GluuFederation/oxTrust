/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2;

import javax.inject.Named;
import javax.ws.rs.Path;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.Authorization;

/**
 * SCIM Bulk Endpoint Implementation
 * 
 * @author Rahat ALi Date: 05.08.2015
 * Updated by jgomer on 2017-09-27.
 */
@Named("scim2BulkEndpoint")
@Path("/scim/v2/Bulk")
@Api(value = "/v2/Bulk", description = "SCIM 2.0 Bulk Endpoint (https://tools.ietf.org/html/rfc7644#section-3.7)", authorizations = {
		@Authorization(value = "Authorization", type = "uma") })
public class BulkWebService extends BaseScimWebService {
//TODO: turn code back later
}
