package org.gluu.oxtrust.api.organization;

import com.google.common.collect.ImmutableMap;
import org.gluu.oxtrust.action.UpdateOrganizationAction;
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
    public Response read(@PathParam(value = "id") String id) {
        GluuOrganization organization = organizationService.getOrganization();
        return Response.ok(ImmutableMap.of("logo", organization.getLogoImage(), "favicon", organization.getFaviconImage())).build();
    }

}
