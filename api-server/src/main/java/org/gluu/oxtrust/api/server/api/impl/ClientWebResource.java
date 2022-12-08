package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.StringHelper;
import org.oxauth.persistence.model.Scope;
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

@Path(ApiConstants.BASE_API_URL + ApiConstants.CLIENTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ClientWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ClientService clientService;

	@Inject
	private ScopeService scopeService;

	public ClientWebResource() {
	}

	@GET
	@Operation(summary = "Get openid connect clients", description = "Get openid connect clients",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_CLIENT_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthClient[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_READ })
	public Response listClients() {
		log(logger, "Get all clients ");
		try {
			List<OxAuthClient> clientList = clientService.getAllClients();
			return Response.ok(clientList).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.SCOPES)
	@Operation(summary = "Get assigned OIDC client scopes", description = "Get OIDC scopes assign to OIDC client",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_CLIENT_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error"),
			@ApiResponse(responseCode = "404", description = "Not Found") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_READ })
	public Response getClientScope(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get client scopes");
		try {
			Objects.requireNonNull(inum);
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				List<String> scopesDn = client.getOxAuthScopes();
				List<Scope> scopes = new ArrayList<Scope>();
				if (scopesDn != null) {
					for (String scopeDn : scopesDn) {
						scopes.add(scopeService.getScopeByDn(scopeDn));
					}
					return Response.ok(scopes).build();
				} else {
					return Response.ok(scopes).build();
				}
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get OIDC client", description = "Get a specific OIDC client",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_CLIENT_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthClient.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_READ })
	public Response getClientByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get client " + inum);
		try {
			Objects.requireNonNull(inum);
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				return Response.ok(client).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search OIDC clients", description = "Search OIDC clients",
			security = @SecurityRequirement(name = "oauth2", scopes = {	ApiScopeConstants.SCOPE_CLIENT_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthClient[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_READ })
	public Response searchClients(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(ApiConstants.SIZE) int size) {
		log(logger, "Search client with pattern= " + pattern + " and size " + size);
		try {
			List<OxAuthClient> clients = clientService.searchClients(pattern, size);
			return Response.ok(clients).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add OIDC client", description = "Add an openidconnect client",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CLIENT_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = OxAuthClient.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response createClient(OxAuthClient client) {
		log(logger, "Add new client ");
		try {
			Objects.requireNonNull(client, "Attempt to create null client");
			String inum = client.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = clientService.generateInumForNewClient();
			}
			client.setInum(inum);
			client.setDn(clientService.getDnForClient(inum));
			client.setDeletable(client.getExp() != null);
			clientService.addClient(client);
			return Response.status(Response.Status.CREATED).entity(clientService.getClientByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update OIDC client", description = "Update openidconnect client",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CLIENT_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthClient.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response updateClient(OxAuthClient client) {
		try {
			Objects.requireNonNull(client, "Attempt to update null client");
			String inum = client.getInum();
			log(logger, "Update client " + inum);
			OxAuthClient existingClient = clientService.getClientByInum(inum);
			if (existingClient != null) {
				client.setInum(existingClient.getInum());
				client.setBaseDn(clientService.getDnForClient(inum));
				client.setDeletable(client.getExp() != null);
				clientService.updateClient(client);
				return Response.ok(clientService.getClientByInum(existingClient.getInum())).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.SCOPES + ApiConstants.SCOPE_INUM_PARAM_PATH)
	@Operation(summary = "Add OIDC client scopes", description = "Add scopes to OIDC client",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CLIENT_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response addScopeToClient(@PathParam(ApiConstants.INUM) @NotNull String inum,
			@PathParam(ApiConstants.SCOPE_INUM) @NotNull String sinum) {
		log(logger, "add new scope to client");
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			Scope scope = scopeService.getScopeByInum(sinum);
			Objects.requireNonNull(client);
			Objects.requireNonNull(scope);
			if (client != null && scope != null) {
				List<String> scopes = new ArrayList<String>(client.getOxAuthScopes());
				String scopeBaseDn = scopeService.getDnForScope(scope.getInum());
				scopes.remove(scopeBaseDn);
				scopes.add(scopeBaseDn);
				client.setOxAuthScopes(scopes);
				clientService.updateClient(client);
				return Response.ok(scopes).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.SCOPES + ApiConstants.SCOPE_INUM_PARAM_PATH)
	@Operation(summary = "Remove OIDC client scope", description = "Remove an existing scope from client",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CLIENT_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response removeScopeToClient(@PathParam(ApiConstants.INUM) @NotNull String inum,
			@PathParam(ApiConstants.SCOPE_INUM) @NotNull String sinum) {
		log(logger, "remove scope to client");
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			Scope scope = scopeService.getScopeByInum(sinum);
			Objects.requireNonNull(client);
			Objects.requireNonNull(scope);
			if (client != null && scope != null) {
				List<String> scopes = new ArrayList<String>(client.getOxAuthScopes());
				scopes.remove(scopeService.getDnForScope(scope.getInum()));
				client.setOxAuthScopes(scopes);
				clientService.updateClient(client);
				return Response.ok(scopes).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Delete OIDC client ", description = "Delete an openidconnect client",
			security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_CLIENT_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthClient[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response deleteClient(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Delete client " + inum);
		try {
			Objects.requireNonNull(inum);
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				clientService.removeClient(client);
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
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response deleteClients() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	@DELETE
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.SCOPES)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_CLIENT_WRITE })
	public Response deleteClientScopes(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
