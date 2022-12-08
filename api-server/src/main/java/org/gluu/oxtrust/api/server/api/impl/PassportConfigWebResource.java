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
import org.gluu.oxtrust.api.server.model.PassportConfig;
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

@Path(ApiConstants.CONFIGURATION + ApiConstants.PASSPORT)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PassportConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;
	
	@GET
	@Operation(summary = "Retrieve passport configuration", description = "Retrieve passport configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PassportConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_CONFIG_READ })
	public Response retrievePassportConfiguration() {
		try {
			log(logger, "Retrieving oxtrust configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			PassportConfig passportConfig = new PassportConfig();
			passportConfig.setPassportUmaClientId(oxTrustappConfiguration.getPassportUmaClientId());
			passportConfig.setPassportUmaClientKeyId(oxTrustappConfiguration.getPassportUmaClientKeyId());
			passportConfig.setPassportUmaClientKeyStoreFile(oxTrustappConfiguration.getPassportUmaClientKeyStoreFile());
			passportConfig.setPassportUmaClientKeyStorePassword(
					oxTrustappConfiguration.getPassportUmaClientKeyStorePassword());
			passportConfig.setPassportUmaResourceId(oxTrustappConfiguration.getPassportUmaResourceId());
			passportConfig.setPassportUmaScope(oxTrustappConfiguration.getPassportUmaScope());
			return Response.ok(passportConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update passport configuration", description = "Update passport configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PassportConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_CONFIG_WRITE })
	public Response updatePassportConfiguration(PassportConfig passportConfig) {
		try {
			log(logger, "Processing passport configuration update");
			Preconditions.checkNotNull(passportConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaClientId())) {
				appConfiguration.setPassportUmaClientId(passportConfig.getPassportUmaClientId());
			}
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaClientKeyId())) {
				appConfiguration.setPassportUmaClientKeyId(passportConfig.getPassportUmaClientKeyId());
			}
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaClientKeyStoreFile())) {
				appConfiguration.setPassportUmaClientKeyStoreFile(passportConfig.getPassportUmaClientKeyStoreFile());
			}
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaClientKeyStorePassword())) {
				appConfiguration
						.setPassportUmaClientKeyStorePassword(passportConfig.getPassportUmaClientKeyStorePassword());
			}
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaResourceId())) {
				appConfiguration.setPassportUmaResourceId(passportConfig.getPassportUmaResourceId());
			}
			if (!Strings.isNullOrEmpty(passportConfig.getPassportUmaScope())) {
				appConfiguration.setPassportUmaScope(passportConfig.getPassportUmaScope());
			}
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
