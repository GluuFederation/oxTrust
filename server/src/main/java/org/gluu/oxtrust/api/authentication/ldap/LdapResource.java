package org.gluu.oxtrust.api.authentication.ldap;

import com.google.common.collect.FluentIterable;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.authorization.ldap.ConnectionStatusDTO;
import org.gluu.oxtrust.api.authorization.ldap.LdapConfigurationDTO;
import org.gluu.oxtrust.service.config.ldap.ConnectionStatus;
import org.gluu.oxtrust.service.config.ldap.LdapConfigurationDuplicatedException;
import org.gluu.oxtrust.service.config.ldap.LdapConfigurationService;
import org.gluu.oxtrust.service.config.ldap.LdapConnectionData;
import org.xdi.model.ldap.GluuLdapConfiguration;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LdapResource {

    @Inject
    private LdapConfigurationService ldapConfigurationService;

    @Inject
    private LdapConfigurationDtoAssembly ldapConfigurationDtoAssembly;

    @Inject
    private ConnectionStatus connectionStatus;

    @Inject
    private ExistingLdapConfigurationValidator existingLdapConfigurationValidator;

    @GET
    @ApiOperation("Get the existing configuration")
    @ApiResponses(
            value = {@ApiResponse(code = 200, response = LdapConfigurationDTO[].class, message = "Success")}
    )
    public Response read() {
        List<LdapConfigurationDTO> result = FluentIterable.from(ldapConfigurationService.findLdapConfigurations())
                .transform(ldapConfigurationDtoAssembly.toDtoAsFunction())
                .toList();

        return Response.ok(result).build();
    }

    @PUT
    @ApiOperation("Update an existing configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = LdapConfigurationDTO.class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found")
            }
    )
    public Response update(@Valid LdapConfigurationDTO ldapConfiguration) {
        GluuLdapConfiguration gluuLdapConfiguration = withVersion(ldapConfiguration);
        ldapConfigurationService.update(gluuLdapConfiguration);
        return Response.ok(read(ldapConfiguration.getConfigId())).build();
    }

    private LdapConfigurationDTO read(String name) {
        GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);
        return ldapConfigurationDtoAssembly.toDto(gluuLdapConfiguration);
    }

    private GluuLdapConfiguration withVersion(LdapConfigurationDTO ldapConfiguration) {
        GluuLdapConfiguration result = ldapConfigurationDtoAssembly.fromDto(ldapConfiguration);
        setupVersion(ldapConfiguration, result);
        return result;
    }

    private void setupVersion(LdapConfigurationDTO ldapConfiguration, GluuLdapConfiguration result) {
        final String name = ldapConfiguration.getConfigId();
        GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);
        result.setVersion(gluuLdapConfiguration.getVersion());
    }

    @POST
    @ApiOperation("Create a new configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = LdapConfigurationDTO.class, message = "Success")
            }
    )
    public Response create(@Valid LdapConfigurationDTO ldapConfiguration) {
        if (existingLdapConfigurationValidator.isInvalid(ldapConfiguration)) {
            throw new LdapConfigurationDuplicatedException(ldapConfiguration.getConfigId());
        }

        GluuLdapConfiguration gluuLdapConfiguration = ldapConfigurationDtoAssembly.fromDto(ldapConfiguration);
        ldapConfigurationService.save(gluuLdapConfiguration);
        return Response.ok(read(ldapConfiguration.getConfigId())).build();
    }

    @DELETE
    @Path("/{name}")
    @ApiOperation("Delete an existing configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = LdapConfigurationDTO[].class, message = "Success")
            }
    )
    public Response delete(@PathParam("name") String name) {
        ldapConfigurationService.remove(name);
        return read();
    }


    @POST
    @Path("/status")
    @ApiOperation("Check the status of a configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = ConnectionStatusDTO.class, message = "Success")
            }
    )
    public Response status(LdapConnectionData ldapConnectionData) {
        ConnectionStatusDTO connectionStatus = ConnectionStatusDTO.from(this.connectionStatus.isUp(ldapConnectionData));
        return Response.ok(connectionStatus).build();
    }

    @GET
    @Path("/{name}/status")
    @ApiOperation("Check the status of an existing configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = ConnectionStatusDTO.class, message = "Success")
            }
    )
    public Response status(@PathParam("name") String name) {
        GluuLdapConfiguration ldapConfiguration = ldapConfigurationService.findLdapConfigurationByName(name);

        LdapConnectionData ldapConnectionData = LdapConnectionData.from(ldapConfiguration);

        ConnectionStatusDTO connectionStatus = ConnectionStatusDTO.from(this.connectionStatus.isUp(ldapConnectionData));
        return Response.ok(connectionStatus).build();
    }

}
