package org.gluu.oxtrust.api.errors;

import org.gluu.persist.exception.BasePersistenceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class BasePersistenceExceptionMapper implements ExceptionMapper<BasePersistenceException> {

    @Override
    public Response toResponse(BasePersistenceException e) {
        return Response.status(BAD_REQUEST)
                .entity(ApiError.of(e))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
