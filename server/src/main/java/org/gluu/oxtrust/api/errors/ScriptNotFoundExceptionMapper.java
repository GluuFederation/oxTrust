package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.exception.ScriptNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Handler for non existent script exception
 *
 * @author Shoeb Khan
 */

@Provider
public class ScriptNotFoundExceptionMapper implements ExceptionMapper<ScriptNotFoundException> {

    @Override
    public Response toResponse(ScriptNotFoundException e) {
        return Response.
                status(Response.Status.NOT_FOUND).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }
}
