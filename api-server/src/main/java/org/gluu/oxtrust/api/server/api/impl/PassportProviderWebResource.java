package org.gluu.oxtrust.api.server.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.Provider;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.PASSPORT
		+ ApiConstants.PROVIDERS, description = "Passport provider webservice")
@ApplicationScoped
public class PassportProviderWebResource extends BaseWebResource {

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	@Inject
	private Logger logger;
	@Inject
	private PassportService passportService;

	@GET
	@ApiOperation(value = "List passport providers")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Provider[].class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response listProviders() {
		log(logger, "List passport providers");
		try {
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			List<Provider> providers = new ArrayList<>();
			providers.addAll(this.passportConfiguration.getProviders());
			return Response.ok(providers).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ID_PARAM_PATH)
	@ApiOperation(value = "Get passport provider by id")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Provider.class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getProviderById(@PathParam(ApiConstants.ID) @NotNull String id) {
		log(logger, "Get group having group" + id);
		id = id.equalsIgnoreCase("") ? null : id;
		try {
			Objects.requireNonNull(id, "inum should not be null");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			List<Provider> providers = new ArrayList<>();
			providers.addAll(this.passportConfiguration.getProviders());
			Provider existingProvider = getExistingProvider(providers, id);
			if (existingProvider != null) {
				return Response.ok(existingProvider).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add passport provider")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Provider.class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createPassportProvider(Provider provider) {
		log(logger, "Add passport provider " + provider.getDisplayName());
		try {
			Objects.requireNonNull(provider, "Attempt to create null provider");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			List<Provider> providers = new ArrayList<>();
			providers.addAll(this.passportConfiguration.getProviders());
			if (provider.getId() == null) {
				String computedId = provider.getDisplayName().toLowerCase().replaceAll("[^\\w-]", "");
				computedId = computedId.concat(UUID.randomUUID().toString().substring(0, 4));
				provider.setId(computedId);
			}
			providers.add(provider);
			this.passportConfiguration.setProviders(providers);
			this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
			this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
			return Response.ok(provider).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update passport provider")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Provider.class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updatePassportProvider(Provider provider) {
		String id = provider.getId();
		id = id.equalsIgnoreCase("") ? null : id;
		log(logger, "Update passport provider " + id);
		try {
			Objects.requireNonNull(id, "id should not be null");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			List<Provider> providers = new ArrayList<>();
			providers.addAll(this.passportConfiguration.getProviders());
			Provider existingProvider = getExistingProvider(providers, id);
			if (existingProvider != null) {
				providers.remove(existingProvider);
				providers.add(provider);
				this.passportConfiguration.setProviders(providers);
				this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
				this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
				return Response.ok(provider).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private Provider getExistingProvider(List<Provider> providers, String id) {
		Provider result = null;
		for (Provider provider : providers) {
			if (provider.getId().equalsIgnoreCase(id)) {
				result = provider;
				break;
			}
		}
		return result;
	}

	@DELETE
	@Path(ApiConstants.ID_PARAM_PATH)
	@ApiOperation(value = "Delete a passport provider")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteProvider(@PathParam(ApiConstants.ID) @NotNull String id) {
		log(logger, "Delete passport provider having id " + id);
		try {
			Objects.requireNonNull(id, "id should not be null");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			List<Provider> providers = new ArrayList<>();
			providers.addAll(this.passportConfiguration.getProviders());
			Provider found = null;
			for (Provider provider : providers) {
				if (id.equalsIgnoreCase(provider.getId())) {
					found = provider;
					break;
				}
			}
			if (found != null) {
				providers.remove(found);
				this.passportConfiguration.setProviders(providers);
				this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
				this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
				return Response.ok().build();
			} else {
				return Response.ok(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAllProviders() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
