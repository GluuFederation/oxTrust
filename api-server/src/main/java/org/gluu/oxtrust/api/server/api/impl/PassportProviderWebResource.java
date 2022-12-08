package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.Provider;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Path(ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PassportProviderWebResource extends BaseWebResource {

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	@Inject
	private Logger logger;
	@Inject
	private PassportService passportService;
	
	@GET
	@Operation(summary="List passport providers",description = "List passport providers")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Provider[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_READ })
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
	@Operation(summary="Get passport provider by id",description = "Get passport provider by id")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Provider.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_READ })
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
	@Operation(summary="Add passport provider",description = "Add passport provider")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Provider.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_WRITE })
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
	@Operation(summary="Update passport provider",description = "Update passport provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Provider.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_WRITE })
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
	@Operation(summary="Delete passport provider",description = "Delete a passport provider")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_WRITE })
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
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_PASSPORT_PROVIDER_WRITE })
	public Response deleteAllProviders() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
