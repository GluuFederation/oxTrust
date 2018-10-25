package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.exception.InvalidScriptDataException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper class for handling illegal argument exceptions
 *
 * @author Shoeb Khan
 */

@Provider
public class InvalidScriptDataExceptionMapper implements ExceptionMapper<InvalidScriptDataException> {

    @Override
    public Response toResponse(InvalidScriptDataException e) {
        return Response.
                status(Response.Status.BAD_REQUEST).
                entity(e.getMessage()).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }
}
