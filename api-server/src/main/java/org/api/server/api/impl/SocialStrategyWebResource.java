package org.api.server.api.impl;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.passport.PassportConfiguration;
import org.xdi.model.passport.ProviderDetails;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.STRATEGIES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.STRATEGIES, description = "social strategies webservice")
@ApplicationScoped
public class SocialStrategyWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private PassportService passportService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;

	public SocialStrategyWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all strategies")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = PassportConfiguration[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	public Response getAllStrategies() {
		try {
			log(logger, "Processing get all strategies request");
			ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			List<PassportConfiguration> ldapPassportConfigurations = ldapOxPassportConfiguration
					.getPassportConfigurations();
			for (PassportConfiguration configuration : ldapPassportConfigurations) {
				if (configuration.getProviders() == null) {
					configuration.setProviders(new ArrayList<ProviderDetails>());
				}
			}
			return Response.ok(ldapPassportConfigurations).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add new strategy")
	@ApiResponses(value = { @ApiResponse(code = 200, response = PassportConfiguration.class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	public Response createSocialStrategy(PassportConfiguration configuration) {
		try {
			log(logger, "Processing add new strategy request");
			Preconditions.checkNotNull(configuration, "Attempt to create empty strategy");
			ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			List<PassportConfiguration> ldapPassportConfigurations = ldapOxPassportConfiguration
					.getPassportConfigurations();
			ldapPassportConfigurations.add(configuration);
			ldapOxPassportConfiguration.setPassportConfigurations(ldapPassportConfigurations);
			passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
			return Response.ok(configuration).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update new social")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = PassportConfiguration.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	public Response updateSocialStrategy(PassportConfiguration configuration) {
		try {
			log(logger, "Processing update strategy request");
			Preconditions.checkNotNull(configuration, "Attempt to update empty strategy");
			String strategy = configuration.getProviders().get(0).getDisplayName();
			ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			List<PassportConfiguration> configurations = ldapOxPassportConfiguration.getPassportConfigurations();
			boolean found = false;
			PassportConfiguration existingValue = null;
			for (PassportConfiguration config : configurations) {
				String strategyName = config.getProviders().get(0).getDisplayName();
				if (strategyName.equalsIgnoreCase(strategy)) {
					existingValue = config;
					found = true;
					break;
				}
			}
			if (found) {
				configurations.remove(existingValue);
				configurations.add(configuration);
				ldapOxPassportConfiguration.setPassportConfigurations(configurations);
				passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
				return Response.ok(configuration).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete a strategy")
	@ApiResponses(value = { @ApiResponse(code = 200, response = String.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	public Response deleteStrategy(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			log(logger, "Processing dlete strategy request");
			Preconditions.checkNotNull(inum);
			ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			List<PassportConfiguration> configurations = ldapOxPassportConfiguration.getPassportConfigurations();
			boolean found = false;
			PassportConfiguration existingValue = null;
			for (PassportConfiguration config : configurations) {
				if (config.getProviders() != null) {
					existingValue = config;
					found = true;
					break;
				}
			}
			if (found) {
				configurations.remove(existingValue);
				ldapOxPassportConfiguration.setPassportConfigurations(configurations);
				passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	public Response deleteStrategies() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

}
