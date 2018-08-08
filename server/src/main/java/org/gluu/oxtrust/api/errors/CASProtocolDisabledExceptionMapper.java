package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.service.config.cas.CASProtocolDisabledException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Provider
public class CASProtocolDisabledExceptionMapper implements ExceptionMapper<CASProtocolDisabledException> {

    @Override
    public Response toResponse(CASProtocolDisabledException e) {
        return Response.status(FORBIDDEN)
                .entity(ApiError.of(e))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
