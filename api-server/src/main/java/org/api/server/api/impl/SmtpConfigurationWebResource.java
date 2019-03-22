package org.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.model.SmtpConfiguration;
import org.xdi.service.MailService;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.SMTP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.SMTP, description = "Smtp server configuration web service")
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
	@ApiOperation(value = "Get smtp configuration")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = SmtpConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getSmtpServerConfiguration() {
		try {
			SmtpConfiguration smtpConfiguration = configurationService.getConfiguration().getSmtpConfiguration();
			return Response.ok(smtpConfiguration).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update smtp configuration")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = SmtpConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	public Response updateSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
		try {
			Preconditions.checkNotNull(smtpConfiguration, "Attempt to update null smtpConfiguration");
			configurationService.encryptedSmtpPassword(smtpConfiguration);
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			configurationUpdate.setSmtpConfiguration(smtpConfiguration);
			configurationService.updateConfiguration(configurationUpdate);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.TEST)
	@ApiOperation(value = "Test smtp configuration")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = SmtpConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response testSmtpConfiguration() {
		try {
			SmtpConfiguration smtpConfiguration = configurationService.getConfiguration().getSmtpConfiguration();
			String password = encryptionService.decrypt(smtpConfiguration.getPassword());
			smtpConfiguration.setPasswordDecrypted(password);
			boolean result = mailService.sendMail(smtpConfiguration, smtpConfiguration.getFromEmailAddress(),
					smtpConfiguration.getFromName(), smtpConfiguration.getFromEmailAddress(), null,
					"SMTP Configuration verification", "Mail to test smtp configuration",
					"Mail to test smtp configuration");
			return Response.ok(result ? Constants.RESULT_SUCCESS : Constants.RESULT_FAILURE).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
