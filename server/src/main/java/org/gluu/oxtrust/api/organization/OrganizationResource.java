package org.gluu.oxtrust.api.organization;

import com.wordnik.swagger.annotations.ApiOperation;
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
// TODO Uma
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
    public Response read() {
        OrganizationConfiguration organizationConfiguration = organizationConfigurationService.find();
        return Response.ok(organizationConfigurationDtoAssembly.toDto(organizationConfiguration)).build();
    }

    @PUT
    @ApiOperation(value = "Update the Organization's configuration")
    public Response update(@Valid OrganizationConfigurationDTO organizationConfigurationDto) {
        OrganizationConfiguration organizationConfiguration = organizationConfigurationDtoAssembly.fromDto(organizationConfigurationDto);
        organizationConfigurationService.save(organizationConfiguration);
        return Response.noContent().build();
    }

}
