package org.gluu.oxtrust.api.server.api.impl;

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

import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.config.Configuration;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path(ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.CONFIG)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.PASSPORT
		+ ApiConstants.PROVIDERS, description = "Passport basic configuration webservice")
@ApplicationScoped
public class PassportBasciConfigWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private PassportService passportService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;

	@GET
	@ApiOperation(value = "Get passport basic configuration")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getPassportBasicConfig() {
		log(logger, "Get passport basic configuration");
		try {
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			return Response.ok(this.passportConfiguration.getConf()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update passport basic configuration")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updatePassportBasicConfig(Configuration configuration) {
		log(logger, "Update passport basic configuration");
		try {
			Objects.requireNonNull(configuration, "config should not be null");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.passportConfiguration.setConf(configuration);
			this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
			this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
			return Response.ok(passportService.loadConfigurationFromLdap().getPassportConfiguration().getConf())
					.build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
