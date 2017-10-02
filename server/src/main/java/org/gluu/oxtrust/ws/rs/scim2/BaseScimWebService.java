package org.gluu.oxtrust.ws.rs.scim2;

import org.gluu.oxtrust.model.scim2.ErrorResponse;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.service.scim2.serialization.ScimResourceSerializer;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Base methods for SCIM web services
 *
 * @author Yuriy Movchan Date: 08/23/2013
 * Updated by jgomer on 2017-09-14.
 */
public class BaseScimWebService {

    @Inject
    Logger log;

    @Inject
    AppConfiguration appConfiguration;

    @Inject
    ScimResourceSerializer resourceSerializer;

    String endpointUrl;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public Response getErrorResponse(Response.Status status, String detail) {
        return getErrorResponse(status.getStatusCode(), null, detail);
    }

    public Response getErrorResponse(Response.Status status, ErrorScimType scimType, String detail) {
        return getErrorResponse(status.getStatusCode(), scimType, detail);
    }

    public Response getErrorResponse(int statusCode, String detail) {
        return getErrorResponse(statusCode, null, detail);
    }

    public Response getErrorResponse(int statusCode, ErrorScimType scimType, String detail) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(String.valueOf(statusCode));
        errorResponse.setScimType(scimType);
        errorResponse.setDetail(detail);

        return Response.status(statusCode).entity(errorResponse).build();
    }

}
