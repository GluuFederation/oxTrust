package org.gluu.oxtrust.api.organization;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.errors.ApiError;
import org.gluu.oxtrust.service.config.organization.OrganizationConfigurationService;
import org.gluu.oxtrust.util.OxTrustApiConstants;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(OxTrustApiConstants.BASE_API_URL + "/organization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + "/organization", description = "Organization webservice")
public class OrganizationResource {

    @Inject
    private SmtpResource smtpResource;

    @Inject
    private OrganizationImageResource organizationImageResource;

    @Inject
    private OrganizationConfigurationService organizationConfigurationService;

    @Inject
    private OrganizationConfigurationDtoAssembly organizationConfigurationDtoAssembly;

    @Path("/smtp")
    public SmtpResource smtpResource() {
        return smtpResource;
    }

    @Path("/images")
    public OrganizationImageResource organizationImageResource() {
        return organizationImageResource;
    }

    @GET
    @ApiOperation(value = "Get the Organization's configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OrganizationConfigurationDTO.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response read() {
        OrganizationConfiguration organizationConfiguration = organizationConfigurationService.find();
        return Response.ok(organizationConfigurationDtoAssembly.toDto(organizationConfiguration)).build();
    }

    @PUT
    @ApiOperation(value = "Update the Organization's configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OrganizationConfigurationDTO.class, message = "Success"),
                    @ApiResponse(code = 400, response = ApiError.class, message = "invalid configuration"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response update(@Valid OrganizationConfigurationDTO organizationConfigurationDto) {
        OrganizationConfiguration organizationConfiguration = organizationConfigurationDtoAssembly.fromDto(organizationConfigurationDto);
        organizationConfigurationService.save(organizationConfiguration);
        return Response.noContent().build();
    }

}
