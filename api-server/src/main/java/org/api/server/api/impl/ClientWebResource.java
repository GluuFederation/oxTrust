package org.api.server.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.api.server.util.OxTrustApiConstants;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthScope;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CLIENTS)
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

	@Inject
	public ClientWebResource() {
	}

	@GET
	@ApiOperation(value = "Get openid connect clients")
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.SCOPES)
	@ApiOperation(value = "Get client scopes")
	public Response getClientScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get client scopes");
		try {
			Objects.requireNonNull(inum);
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				List<String> scopesDn = client.getOxAuthScopes();
				List<OxAuthScope> scopes = new ArrayList<OxAuthScope>();
				for (String scopeDn : scopesDn) {
					scopes.add(scopeService.getScopeByDn(scopeDn));
				}
				return Response.ok(scopes).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a specific openidconnect client")
	public Response getClientByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
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
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search clients")
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size) {
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
	@ApiOperation(value = "Add an openidconnect client")
	public Response createClient(OxAuthClient client) {
		log(logger, "Add new client ");
		try {
			Objects.requireNonNull(client, "Attempt to create null client");
			String inum = clientService.generateInumForNewClient();
			client.setInum(inum);
			client.setDn(clientService.getDnForClient(inum));
			clientService.addClient(client);
			return Response.ok(clientService.getClientByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update openidconnect client")
	public Response updateClient(OxAuthClient client) {
		try {
			Objects.requireNonNull(client, "Attempt to update null client");
			String inum = client.getInum();
			log(logger, "Update client " + inum);
			OxAuthClient existingClient = clientService.getClientByInum(inum);
			if (existingClient != null) {
				client.setInum(existingClient.getInum());
				client.setBaseDn(clientService.getDnForClient(inum));
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.SCOPES + OxTrustApiConstants.SCOPE_INUM_PARAM_PATH)
	@ApiOperation(value = "Get client scopes")
	public Response addScopeToClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@PathParam(OxTrustApiConstants.SCOPE_INUM) @NotNull String sinum) {
		log(logger, "add new scope to client");
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			OxAuthScope scope = scopeService.getScopeByInum(sinum);
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.SCOPES + OxTrustApiConstants.SCOPE_INUM_PARAM_PATH)
	@ApiOperation(value = "Remove a client scope")
	public Response removeScopeToClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@PathParam(OxTrustApiConstants.SCOPE_INUM) @NotNull String sinum) {
		log(logger, "add new scope to client");
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			OxAuthScope scope = scopeService.getScopeByInum(sinum);
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an openidconnect client")
	public Response deleteClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
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
	public Response deleteClients() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.SCOPES)
	public Response deleteClientScopes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
