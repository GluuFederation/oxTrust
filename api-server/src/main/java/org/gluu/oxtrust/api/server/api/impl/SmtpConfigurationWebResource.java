package org.gluu.oxtrust.api.server.api.impl;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.model.SmtpConfiguration;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.service.MailService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SMTP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SmtpConfigurationWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private MailService mailService;

	@Inject
	private EncryptionService encryptionService;
	
	@GET
	@Operation(summary="Get smtp configuration" ,description = "Get smtp configuration")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SmtpConfiguration.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SMTP_CONFIGURATION_READ })
	public Response getSmtpServerConfiguration() {
		try {
			SmtpConfiguration smtpConfiguration = configurationService.getConfiguration().getSmtpConfiguration();
			if (smtpConfiguration != null) {
				return Response.ok(smtpConfiguration).build();
			} else {
				return Response.ok(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary="Update smtp configuration", description = "Update smtp configuration")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SmtpConfiguration.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SMTP_CONFIGURATION_WRITE })
	public Response updateSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
		try {
			Preconditions.checkNotNull(smtpConfiguration, "Attempt to update null smtpConfiguration");
			configurationService.encryptSmtpPassword(smtpConfiguration);
			configurationService.encryptKeyStorePassword(smtpConfiguration);
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			configurationUpdate.setSmtpConfiguration(smtpConfiguration);
			configurationService.updateConfiguration(configurationUpdate);
			return Response.ok(configurationService.getConfiguration().getSmtpConfiguration()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.TEST)
	@Operation(summary="Test smtp configuration", description = "Test smtp configuration")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SmtpConfiguration.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SMTP_CONFIGURATION_READ })
	public Response testSmtpConfiguration() {
		try {
			SmtpConfiguration smtpConfiguration = configurationService.getConfiguration().getSmtpConfiguration();
			String password = encryptionService.decrypt(smtpConfiguration.getPassword());
			smtpConfiguration.setPasswordDecrypted(password);
			boolean result = mailService.sendMail(smtpConfiguration, smtpConfiguration.getFromEmailAddress(),
					smtpConfiguration.getFromName(), smtpConfiguration.getFromEmailAddress(), null,
					"SMTP Configuration verification", "Mail to test smtp configuration",
					"Mail to test smtp configuration");
			return Response.ok(result ? true : false).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
