package org.gluu.oxtrust.api.organization;

import org.gluu.oxtrust.service.config.smtp.SmtpConfigurationService;
import org.gluu.oxtrust.service.organization.SmtpStatusFactory;
import org.xdi.model.SmtpConfiguration;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SmtpResource {

    @Inject
    private SmtpConfigurationService smtpConfigurationService;

    @Inject
    private SmtpConfigurationDtoAssembly smtpConfigurationDtoAssembly;

    @Inject
    private SmtpStatusFactory smtpStatusFactory;

    @GET
    public Response read() {
        SmtpConfiguration smtpConfiguration = smtpConfigurationService.findSmtpConfiguration();
        SmtpConfigurationDTO smtpConfigurationDTO = smtpConfigurationDtoAssembly.toDto(smtpConfiguration);
        return Response.ok(smtpConfigurationDTO).build();
    }

    @PUT
    public Response update(@Valid SmtpConfigurationDTO smtpConfigurationDTO) {
        SmtpConfiguration smtpConfiguration = smtpConfigurationDtoAssembly.fromDto(smtpConfigurationDTO);
        smtpConfigurationService.save(smtpConfiguration);
        return read();
    }

    @GET
    @Path("status")
    public Response status() {
        return Response.ok(smtpStatusFactory.create()).build();
    }

}
