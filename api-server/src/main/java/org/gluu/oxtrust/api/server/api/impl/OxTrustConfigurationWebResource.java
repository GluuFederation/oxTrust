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
import org.gluu.oxtrust.api.server.model.OxTrustBasicConfig;
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

@Path(ApiConstants.CONFIGURATION + ApiConstants.OXTRUST)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OxTrustConfigurationWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;
	
	@GET
	@Operation(summary = "Retrieve oxtrust configuration", description = "Retrieve oxtrust configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxTrustBasicConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_CONFIGURATION_READ })
	public Response retrieveOxtrustConfiguration() {
		try {
			log(logger, "Retrieving oxtrust configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			OxTrustBasicConfig oxTrustBasicConfig = new OxTrustBasicConfig();
			oxTrustBasicConfig.setApplicationUrl(oxTrustappConfiguration.getApplicationUrl());
			oxTrustBasicConfig.setBaseDN(oxTrustappConfiguration.getBaseDN());
			oxTrustBasicConfig.setOrgSupportEmail(oxTrustappConfiguration.getOrgSupportEmail());
			oxTrustBasicConfig.setOrganizationName(oxTrustappConfiguration.getOrganizationName());
			oxTrustBasicConfig.setBaseEndpoint(oxTrustappConfiguration.getBaseEndpoint());
			oxTrustBasicConfig.setLdifStore(oxTrustappConfiguration.getLdifStore());
			oxTrustBasicConfig.setUpdateStatus(oxTrustappConfiguration.isUpdateStatus());
			oxTrustBasicConfig.setKeystorePath(oxTrustappConfiguration.getKeystorePath());
			oxTrustBasicConfig.setAllowPersonModification(oxTrustappConfiguration.isAllowPersonModification());
			oxTrustBasicConfig.setEnableUpdateNotification(oxTrustappConfiguration.isEnableUpdateNotification());
			oxTrustBasicConfig.setUseLocalCache(oxTrustappConfiguration.getUseLocalCache());
			oxTrustBasicConfig.setEnforceEmailUniqueness(oxTrustappConfiguration.getEnforceEmailUniqueness());
			oxTrustBasicConfig.setConfigGeneration(oxTrustappConfiguration.isConfigGeneration());
			oxTrustBasicConfig.setCertDir(oxTrustappConfiguration.getCertDir());
			oxTrustBasicConfig.setCleanServiceInterval(oxTrustappConfiguration.getCleanServiceInterval());
			oxTrustBasicConfig.setLoggingLevel(oxTrustappConfiguration.getLoggingLevel());
			oxTrustBasicConfig.setPasswordResetRequestExpirationTime(
					oxTrustappConfiguration.getPasswordResetRequestExpirationTime());
			return Response.ok(oxTrustBasicConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update oxtrust configuration", description = "Update oxtrust configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AppConfiguration.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_CONFIGURATION_WRITE })
	public Response updateOxtrustConfiguration(OxTrustBasicConfig oxTrustBasicConfig) {
		try {
			log(logger, "Processing oxtrust json update request");
			Preconditions.checkNotNull(oxTrustBasicConfig, "Attempt to update null oxtrust json settings");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getApplicationUrl())) {
				oxTrustappConfiguration.setApplicationUrl(oxTrustBasicConfig.getApplicationUrl());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getBaseDN())) {
				oxTrustappConfiguration.setBaseDN(oxTrustBasicConfig.getBaseDN());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getOrgSupportEmail())) {
				oxTrustappConfiguration.setOrgSupportEmail(oxTrustBasicConfig.getOrgSupportEmail());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getOrganizationName())) {
				oxTrustappConfiguration.setOrganizationName(oxTrustBasicConfig.getOrganizationName());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getBaseEndpoint())) {
				oxTrustappConfiguration.setBaseEndpoint(oxTrustBasicConfig.getBaseEndpoint());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getLdifStore())) {
				oxTrustappConfiguration.setLdifStore(oxTrustBasicConfig.getLdifStore());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getKeystorePath())) {
				oxTrustappConfiguration.setKeystorePath(oxTrustBasicConfig.getKeystorePath());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getCertDir())) {
				oxTrustappConfiguration.setCertDir(oxTrustBasicConfig.getCertDir());
			}
			if (!Strings.isNullOrEmpty(oxTrustBasicConfig.getLoggingLevel())) {
				oxTrustappConfiguration.setLoggingLevel(oxTrustBasicConfig.getLoggingLevel());
			}
			oxTrustappConfiguration.setUpdateStatus(oxTrustBasicConfig.getUpdateStatus());
			oxTrustappConfiguration.setEnableUpdateNotification(oxTrustBasicConfig.getEnableUpdateNotification());
			oxTrustappConfiguration.setAllowPersonModification(oxTrustBasicConfig.getAllowPersonModification());
			oxTrustappConfiguration
					.setPasswordResetRequestExpirationTime(oxTrustBasicConfig.getPasswordResetRequestExpirationTime());
			oxTrustappConfiguration.setCleanServiceInterval(oxTrustBasicConfig.getCleanServiceInterval());
			oxTrustappConfiguration.setEnforceEmailUniqueness(oxTrustBasicConfig.getEnforceEmailUniqueness());
			oxTrustappConfiguration.setUseLocalCache(oxTrustBasicConfig.getUseLocalCache());
			oxTrustappConfiguration.setConfigGeneration(oxTrustBasicConfig.getConfigGeneration());
			oxTrustappConfiguration.setAllowPersonModification(oxTrustBasicConfig.getAllowPersonModification());
			jsonConfigurationService.saveOxTrustappConfiguration(oxTrustappConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
