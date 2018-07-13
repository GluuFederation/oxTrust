package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.service.config.ldap.LdapConfigurationNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class LdapConfigurationNotFoundExceptionMapper implements ExceptionMapper<LdapConfigurationNotFoundException> {

    @Override
    public Response toResponse(LdapConfigurationNotFoundException e) {
        return Response.status(NOT_FOUND)
                .entity(ApiError.of(e))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
