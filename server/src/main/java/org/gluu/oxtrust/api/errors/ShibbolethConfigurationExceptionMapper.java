package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.service.config.cas.ShibbolethConfigurationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class ShibbolethConfigurationExceptionMapper implements ExceptionMapper<ShibbolethConfigurationException> {

    @Override
    public Response toResponse(ShibbolethConfigurationException e) {
        return Response.status(INTERNAL_SERVER_ERROR)
                .entity(ApiError.of(e))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
