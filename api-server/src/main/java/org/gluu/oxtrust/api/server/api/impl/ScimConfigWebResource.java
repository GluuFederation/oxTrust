package org.gluu.oxtrust.api.server.api.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.config.oxtrust.ScimMode;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.ScimProperties;
import org.gluu.oxtrust.api.server.model.ScimConfig;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.CONFIGURATION + ApiConstants.SCIM)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScimConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	@GET
	@Operation(summary = "Retrieve scim configuration", description = "Retrieve scim configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ScimConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SCIM_CONFIG_READ })
	public Response retrieveScimConfiguration() {
		try {
			log(logger, "Retrieving SCIM configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			ScimConfig scimConfig = new ScimConfig();
			scimConfig.setScimUmaClientId(oxTrustappConfiguration.getScimUmaClientId());
			scimConfig.setScimUmaClientKeyId(oxTrustappConfiguration.getScimUmaClientKeyId());
			scimConfig.setScimUmaClientKeyStoreFile(oxTrustappConfiguration.getScimUmaClientKeyStoreFile());
			scimConfig.setScimUmaClientKeyStorePassword(oxTrustappConfiguration.getScimUmaClientKeyStorePassword());
			scimConfig.setScimUmaResourceId(oxTrustappConfiguration.getScimUmaResourceId());
			scimConfig.setScimUmaScope(oxTrustappConfiguration.getScimUmaScope());
			scimConfig.setScimMaxCount(oxTrustappConfiguration.getScimProperties().getMaxCount());
			scimConfig.setScimProtectionMode(oxTrustappConfiguration.getScimProperties().getProtectionMode());
			scimConfig.setUserExtensionSchemaURI(oxTrustappConfiguration.getScimProperties().getUserExtensionSchemaURI());
			return Response.ok(scimConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update Scim configuration", description = "Update Scim configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ScimConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SCIM_CONFIG_WRITE })
	public Response updateScimConfiguration(ScimConfig scimConfig) {
		try {
			log(logger, "Processing scim configuration update");
			Preconditions.checkNotNull(scimConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaClientId())) {
				appConfiguration.setScimUmaClientId(scimConfig.getScimUmaClientId());
			}
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaClientKeyId())) {
				appConfiguration.setScimUmaClientKeyId(scimConfig.getScimUmaClientKeyId());
			}
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaClientKeyStoreFile())) {
				appConfiguration.setScimUmaClientKeyStoreFile(scimConfig.getScimUmaClientKeyStoreFile());
			}
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaClientKeyStorePassword())) {
				appConfiguration.setScimUmaClientKeyStorePassword(scimConfig.getScimUmaClientKeyStorePassword());
			}
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaResourceId())) {
				appConfiguration.setScimUmaResourceId(scimConfig.getScimUmaResourceId());
			}
			if (!Strings.isNullOrEmpty(scimConfig.getScimUmaScope())) {
				appConfiguration.setScimUmaScope(scimConfig.getScimUmaScope());
			}
			Integer scimMaxCount = scimConfig.getScimMaxCount();
			if (scimMaxCount == null || scimMaxCount <= 0) {
				scimMaxCount = appConfiguration.getScimProperties().getMaxCount();
			}
			ScimMode mode = Optional.ofNullable(scimConfig.getScimProtectionMode()).orElse(ScimMode.OAUTH);
			
			ScimProperties scimProperties = new ScimProperties();
			scimProperties.setMaxCount(scimMaxCount);
			scimProperties.setProtectionMode(mode);
			scimProperties.setUserExtensionSchemaURI(scimConfig.getUserExtensionSchemaURI());
			
			appConfiguration.setScimProperties(scimProperties);
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
