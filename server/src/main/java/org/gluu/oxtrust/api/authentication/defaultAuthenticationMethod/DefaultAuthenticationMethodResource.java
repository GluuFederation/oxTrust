package org.gluu.oxtrust.api.authentication.defaultAuthenticationMethod;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.AuthenticationMethodDTO;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethod;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethodService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DefaultAuthenticationMethodResource {

    @Inject
    private AuthenticationMethodService authenticationMethodService;

    @Inject
    private AuthenticationMethodDtoAssembly authenticationMethodDtoAssembly;

    @GET
    @ApiOperation("Get the existing configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = AuthenticationMethodDTO.class, message = "Success")}
    )
    public Response read() {
        AuthenticationMethod authenticationMode = authenticationMethodService.findAuthenticationMode();
        AuthenticationMethodDTO dto = authenticationMethodDtoAssembly.toDto(authenticationMode);
        return Response.ok(dto).build();
    }

    @PUT
    @ApiOperation("Update the existing configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = AuthenticationMethodDTO.class, message = "Success")}
    )
    public Response update(@Valid AuthenticationMethodDTO authenticationMethodDto) {
        AuthenticationMethod newAuthenticationMethod = authenticationMethodDtoAssembly.fromDto(authenticationMethodDto);
        AuthenticationMethod authenticationMode = authenticationMethodService.findAuthenticationMode();
        AuthenticationMethod result = authenticationMode.mergeWith(newAuthenticationMethod);
        authenticationMethodService.save(result);

        return read();
    }

}
