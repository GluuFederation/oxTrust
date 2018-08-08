package org.gluu.oxtrust.api.errors;

import org.gluu.oxtrust.service.config.ldap.LdapConfigurationDuplicatedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class LdapConfigurationDuplicatedExceptionMapper implements ExceptionMapper<LdapConfigurationDuplicatedException> {

    @Override
    public Response toResponse(LdapConfigurationDuplicatedException e) {
        return Response.status(BAD_REQUEST)
                .entity(ApiError.of(e))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
