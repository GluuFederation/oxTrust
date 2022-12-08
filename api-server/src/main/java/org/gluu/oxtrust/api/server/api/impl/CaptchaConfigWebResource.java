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
import org.gluu.oxtrust.api.server.model.CaptchaConfig;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Path(ApiConstants.CONFIGURATION + ApiConstants.CAPTCHA)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CaptchaConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;    

	@GET
	@Operation(summary = "Retrieve captcha configuration", description = "Retrieve captcha configuration",
    security = @SecurityRequirement(name = "oauth2", scopes = {
    		ApiScopeConstants.SCOPE_CAPTCHA_CONFiG_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CaptchaConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CAPTCHA_CONFiG_READ })
	public Response retrieveCaptchaConfiguration() {
		try {
			log(logger, "Retrieving captcha configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			CaptchaConfig captchaConfig = new CaptchaConfig();
			captchaConfig.setAuthenticationRecaptchaEnabled(oxTrustappConfiguration.isAuthenticationRecaptchaEnabled());
			captchaConfig.setRecaptchaSiteKey(oxTrustappConfiguration.getRecaptchaSiteKey());
			captchaConfig.setRecaptchaSecretKey(oxTrustappConfiguration.getRecaptchaSecretKey());
			return Response.ok(captchaConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update captcha configuration", description = "Update captcha configuration",
	security = @SecurityRequirement(name = "oauth2", scopes = {
	   		ApiScopeConstants.SCOPE_CAPTCHA_CONFiG_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CaptchaConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CAPTCHA_CONFiG_WRITE })
	public Response updateCaptchaConfiguration(CaptchaConfig captchaConfig) {
		try {
			log(logger, "Processing captcha configuration update");
			Preconditions.checkNotNull(captchaConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(captchaConfig.getRecaptchaSiteKey())) {
				appConfiguration.setRecaptchaSiteKey(captchaConfig.getRecaptchaSiteKey());
			}
			if (!Strings.isNullOrEmpty(captchaConfig.getRecaptchaSecretKey())) {
				appConfiguration.setRecaptchaSecretKey(captchaConfig.getRecaptchaSecretKey());
			}
			appConfiguration.setAuthenticationRecaptchaEnabled(captchaConfig.getAuthenticationRecaptchaEnabled());
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
