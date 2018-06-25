package org.gluu.oxtrust.api.oxauth;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.configuration.OxAuthConfig;
import org.gluu.oxtrust.service.config.OxAuthConfigurationDAO;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.xdi.service.security.Secure;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static org.gluu.oxtrust.ldap.service.AppInitializer.OX_AUTH_CONFIGURATION_DAO;

@Path(OxTrustApiConstants.BASE_API_URL + "/oxauth/configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Secure("#{apiPermissionService.hasPermission('configuration', 'access')}")
public class OxAuthConfigurationWebService {

    @Inject
    @Named(OX_AUTH_CONFIGURATION_DAO)
    private OxAuthConfigurationDAO oxAuthConfigurationDAO;

    @Context
    private HttpServletRequest request;

    @GET
    @ApiOperation(value = "Get the existing configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = OxAuthConfig.class, message = "Success")}
    )
    public Response read() throws IOException {
        OxAuthConfig oxAuthConfig = oxAuthConfigurationDAO.find();
        return Response.ok(oxAuthConfig).build();
    }

    @PUT
    @ApiOperation(value = "Update the oxAuth configuration")
    public Response update(@Valid OxAuthConfig oxAuthConfig) throws IOException {
        oxAuthConfigurationDAO.save(oxAuthConfig);
        return read();
    }

    @GET
    @Path("/form-definition")
    @ApiOperation(value = "Read the form definition")
    public Response formDefinition() {
        String formDefinitionUrl = request.getContextPath() + "/schema/oxauth-config.xml.json";
        return Response.status(TEMPORARY_REDIRECT)
                .header(LOCATION, formDefinitionUrl)
                .build();
    }

}
