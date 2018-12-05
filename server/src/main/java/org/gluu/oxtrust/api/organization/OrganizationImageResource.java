package org.gluu.oxtrust.api.organization;

import com.google.common.collect.ImmutableMap;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.action.UpdateOrganizationAction;
import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationImageResource {

    @Inject
    private OrganizationService organizationService;

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get the Organization's logo and favicon")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OrganizationImageDTO.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response read(@PathParam(value = "id") String id) {
        GluuOrganization organization = organizationService.getOrganization();
        return Response.ok(dto(organization)).build();
    }

    private OrganizationImageDTO dto(GluuOrganization organization) {
        OrganizationImageDTO result = new OrganizationImageDTO();
        result.setLogo(organization.getLogoImage());
        result.setFavicon(organization.getFaviconImage());
        return result;
    }

}
