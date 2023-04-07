package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.api.server.model.ApiConfig;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.EncryptionService;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Path(ApiConstants.CONFIGURATION + ApiConstants.API)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ApiConfigWebRecource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;
	@Inject
	private EncryptionService encryptionService;

	@GET
	@Operation(summary = "Retrieve api configuration", description = "Retrieve api configuration" ,
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_APICONFIG_READ}))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApiConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_APICONFIG_READ })
	public Response retrieveApiConfiguration() {
		try {
			log(logger, "Retrieving api configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			ApiConfig apiConfig = new ApiConfig();
			apiConfig.setApiUmaClientId(oxTrustappConfiguration.getApiUmaClientId());
			apiConfig.setApiUmaClientKeyId(oxTrustappConfiguration.getApiUmaClientKeyId());
			apiConfig.setApiUmaClientKeyStoreFile(oxTrustappConfiguration.getApiUmaClientKeyStoreFile());
			apiConfig.setApiUmaClientKeyStorePassword(
					encryptionService.decrypt(oxTrustappConfiguration.getApiUmaClientKeyStorePassword()));
			apiConfig.setApiUmaResourceId(oxTrustappConfiguration.getApiUmaResourceId());
			apiConfig.setApiUmaScope(oxTrustappConfiguration.getApiUmaScope());
			apiConfig.setOxTrustApiTestMode(oxTrustappConfiguration.isOxTrustApiTestMode());
			return Response.ok(apiConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update api configuration", description = "Update api configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ApiConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_APICONFIG_WRITE })
	public Response updateApiConfiguration(ApiConfig apiConfig) {
		try {
			log(logger, "Processing api configuration update");
			Preconditions.checkNotNull(apiConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(apiConfig.getApiUmaClientId())) {
				appConfiguration.setApiUmaClientId(apiConfig.getApiUmaClientId());
			}
			if (!Strings.isNullOrEmpty(apiConfig.getApiUmaClientKeyId())) {
				appConfiguration.setApiUmaClientKeyId(apiConfig.getApiUmaClientKeyId());
			}
			if (!Strings.isNullOrEmpty(apiConfig.getApiUmaClientKeyStoreFile())) {
				appConfiguration.setApiUmaClientKeyStoreFile(apiConfig.getApiUmaClientKeyStoreFile());
			}
			if (!Strings.isNullOrEmpty(apiConfig.getApiUmaClientKeyStorePassword())) {
				appConfiguration.setApiUmaClientKeyStorePassword(
						encryptionService.encrypt(apiConfig.getApiUmaClientKeyStorePassword()));
			}
			if (!Strings.isNullOrEmpty(apiConfig.getApiUmaResourceId())) {
				appConfiguration.setApiUmaResourceId(apiConfig.getApiUmaResourceId());
			}
			if (apiConfig.getApiUmaScope() != null) {
				appConfiguration.setApiUmaScope(apiConfig.getApiUmaScope());
			}
			appConfiguration.setOxTrustApiTestMode(apiConfig.getOxTrustApiTestMode());
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
