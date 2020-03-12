package org.gluu.oxtrust.ws.rs.passport;

import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;
import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.service.JsonService;

import static javax.ws.rs.core.Response.Status;

/**
 * PassportConfigurationEndPoint Implementation
 * 
 * @author Shekhar L.
 * @author Yuriy Movchan Date: 12/06/2016
 */

@Named("PassportConfigurationEndPoint")
@Path("/passport/config")
public class PassportRestWebService {

    @Inject
    private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private JsonService jsonService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
    @ProtectedApi
	public Response getPassportConfig() {

	    Status status;
        String jsonResponse;

        try {
            Object obj = Optional.ofNullable(passportService.loadConfigurationFromLdap())
                    .map(LdapOxPassportConfiguration::getPassportConfiguration)
                    .map(Object.class::cast)
                    .orElse(Collections.emptyMap());

            jsonResponse = jsonService.objectToPerttyJson(obj);
            status = Status.OK;
        } catch (Exception e) {
            jsonResponse = "Failed to prepare configuration: " + e.getMessage();
            status = Status.INTERNAL_SERVER_ERROR;
            log.error(e.getMessage(), e);
        }

        log.trace("Passport endpoint config response is\n{}", jsonResponse);
        return Response.status(status).entity(jsonResponse).build();

	}

}
