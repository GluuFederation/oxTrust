package org.gluu.oxtrust.api.server.api.impl.radius;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.api.server.api.impl.BaseWebResource;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.service.radius.GluuRadiusConfigService;
import org.gluu.oxtrust.util.ProductInstallationChecker;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.service.custom.CustomScriptService;
import org.slf4j.Logger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.BASE_API_URL+ApiConstants.RADIUS_SETTINGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GluuRadiusConfigWebResource extends BaseWebResource {

    @Inject
    private Logger logger;

    @Inject
    private GluuRadiusConfigService gluuRadiusConfigService;

    @Inject
    private ScopeService scopeService;

    @Inject 
    private CustomScriptService customScriptService;

    @Inject
    private ClientService clientService;
    
    @GET
    @Operation(summary = "Get Radius Server Configuration",description = "Get Radius Server Configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServerConfiguration.class)),description="Success"),
        @ApiResponse(responseCode = "403", description="Gluu Radius is not installed"),
        @ApiResponse(responseCode = "404", description="Gluu Radius configuration not found"),
        @ApiResponse(responseCode = "500", description="Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CONFIG_READ})
    public Response getServerConfig() {
        log(logger,"Get radius server configuration");
        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            
            ServerConfiguration config = gluuRadiusConfigService.getServerConfiguration();
            if(config == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(config).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Operation(summary= "Get Radius Server Configuration", description = "Update Radius Server Configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ServerConfiguration.class)),description="Success"),
        @ApiResponse(responseCode = "403", description = "Gluu Radius is not installed"),
        @ApiResponse(responseCode = "404", description = "Gluu Radius configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ProtectedApi(scopes = {ApiScopeConstants.SCOPE_GLUU_RADIUS_CONFIG_WRITE})
    public Response updateServerConfiguration(ServerConfiguration newConfig) {
        log(logger,"Update radius server configuration");
        try {
            if(ProductInstallationChecker.isGluuRadiusInstalled() == false)
                return Response.status(Response.Status.FORBIDDEN).build();
            Objects.requireNonNull(newConfig);

            ServerConfiguration oldConfig = gluuRadiusConfigService.getServerConfiguration();
            if(oldConfig == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            
            if(newConfig.getListenInterface() == null)
                newConfig.setListenInterface(oldConfig.getListenInterface());
            if(newConfig.getAuthPort() == null)
                newConfig.setAuthPort(oldConfig.getAuthPort());
            if(newConfig.getAcctPort() == null)
                newConfig.setAcctPort(oldConfig.getAcctPort());
            if(newConfig.getOpenidBaseUrl() == null)
                newConfig.setOpenidBaseUrl(oldConfig.getOpenidBaseUrl());
            if(newConfig.getOpenidUsername() == null) {
                newConfig.setOpenidUsername(oldConfig.getOpenidUsername());
                newConfig.setOpenidPassword(oldConfig.getOpenidPassword());
            }else {
                OxAuthClient client = clientService.getClientByInum(newConfig.getOpenidUsername());
                if(client == null) {
                    newConfig.setOpenidUsername(oldConfig.getOpenidUsername());
                    newConfig.setOpenidPassword(oldConfig.getOpenidPassword());
                }else {
                    newConfig.setOpenidPassword(client.getEncodedClientSecret());
                }
            }
            String acrValue = newConfig.getAcrValue();
            if(acrValue == null || (acrValue != null && !isValidAcrValue(acrValue)))
                newConfig.setAcrValue(oldConfig.getAcrValue());
            List<String> scopes = newConfig.getScopes();
            if(scopes == null || (scopes != null && scopes.isEmpty()))
                newConfig.setScopes(oldConfig.getScopes());
            else {
                List<String> newscopes = scopes;
                for(String scope : scopes) {
                    // if just one scope is invalid , use the old ones (safe)
                    if(!isValidScope(scope)) {
                        newscopes = oldConfig.getScopes();
                        break;
                    }
                }
                newConfig.setScopes(newscopes);
            }
            if(newConfig.getAuthenticationTimeout() == null || newConfig.getAuthenticationTimeout() <= 0)
                newConfig.setAuthenticationTimeout(oldConfig.getAuthenticationTimeout());
                        
            gluuRadiusConfigService.updateServerConfiguration(newConfig);
            return Response.ok(gluuRadiusConfigService.getServerConfiguration()).build();
        }catch(Exception e) {
            log(logger,e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private final boolean isValidAcrValue(String acrValue) {

        List<CustomScriptType> script_types = new ArrayList<CustomScriptType>();
        script_types.add(CustomScriptType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
        List<CustomScript> allowed_scripts = customScriptService.findCustomScripts(script_types);
        for(CustomScript custom_script : allowed_scripts) {
            if(!custom_script.isEnabled())
                continue;
            if(StringUtils.equals(custom_script.getName(),acrValue))
                return true;
        }
        return false;
    }

    private final boolean isValidScope(String scopeDn) throws Exception {

        return scopeService.getScopeByDn(scopeDn) != null;
    }
}