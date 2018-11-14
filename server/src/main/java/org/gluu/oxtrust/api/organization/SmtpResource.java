package org.gluu.oxtrust.api.organization;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.errors.ApiError;
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
    @ApiOperation(value = "Get the SMTP configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = SmtpConfigurationDTO.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response read() {
        SmtpConfiguration smtpConfiguration = smtpConfigurationService.findSmtpConfiguration();
        SmtpConfigurationDTO smtpConfigurationDTO = smtpConfigurationDtoAssembly.toDto(smtpConfiguration);
        return Response.ok(smtpConfigurationDTO).build();
    }

    @PUT
    @ApiOperation(value = "Update the SMTP configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = SmtpConfigurationDTO.class, message = "Success"),
                    @ApiResponse(code = 400, response = ApiError.class, message = "invalid configuration"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response update(@Valid SmtpConfigurationDTO smtpConfigurationDTO) {
        SmtpConfiguration smtpConfiguration = smtpConfigurationDtoAssembly.fromDto(smtpConfigurationDTO);
        smtpConfigurationService.save(smtpConfiguration);
        return read();
    }

    @GET
    @Path("status")
    @ApiOperation(value = "Check the status of the SMTP configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = SmtpStatus.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response status() {
        return Response.ok(smtpStatusFactory.create()).build();
    }

}
