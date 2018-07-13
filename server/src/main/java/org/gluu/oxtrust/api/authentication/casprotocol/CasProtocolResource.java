package org.gluu.oxtrust.api.authentication.casprotocol;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.authorization.casprotocol.CasProtocolDTO;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.service.config.cas.CASProtocolConfiguration;
import org.gluu.oxtrust.service.config.cas.CASProtocolConfigurationProvider;
import org.gluu.oxtrust.service.config.cas.ShibbolethService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CasProtocolResource {

    @Inject
    private CASProtocolConfigurationProvider casProtocolConfigurationProvider;

    @Inject
    private CasProtocolDtoAssembly casProtocolDtoAssembly;

    @Inject
    private CASService casService;

    @Inject
    private ShibbolethService shibbolethService;

    @GET
    @ApiOperation("Get the existing configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = CasProtocolDTO.class, message = "Success")}
    )
    public Response read() {
        CASProtocolConfiguration casProtocolConfiguration = casProtocolConfigurationProvider.get();
        CasProtocolDTO casProtocolDto = casProtocolDtoAssembly.toDto(casProtocolConfiguration);
        return Response.ok(casProtocolDto).build();
    }

    @PUT
    @ApiOperation("Update the configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = CasProtocolDTO.class, message = "Success")}
    )
    public Response update(@Valid CasProtocolDTO casProtocol) {
        CASProtocolConfiguration casProtocolConfiguration = casProtocolDtoAssembly.fromDto(casProtocol);
        casProtocolConfiguration.save(casService);
        shibbolethService.update(casProtocolConfiguration);
        return read();
    }

}
