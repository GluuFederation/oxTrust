package org.gluu.oxtrust.api.openidconnect;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CLIENTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CLIENTS, description = "Clients webservice")
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
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthClient[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response listClients() {
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
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope[].class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getClientScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Preconditions.checkNotNull(inum);
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response getClientByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Preconditions.checkNotNull(inum);
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient[].class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response searchGroups(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size) {
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response createClient(OxAuthClient client) {
	    try {
			Preconditions.checkNotNull(client, "Attempt to create null client");
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthClient.class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response updateClient(OxAuthClient client) {
		try {
			Preconditions.checkNotNull(client, "Attempt to update null client");
			String inum = client.getInum();

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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = String[].class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response addScopeToClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@PathParam(OxTrustApiConstants.SCOPE_INUM) @NotNull String sinum) {
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			OxAuthScope scope = scopeService.getScopeByInum(sinum);
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = String[].class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response removeScopeToClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@PathParam(OxTrustApiConstants.SCOPE_INUM) @NotNull String sinum) {
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			OxAuthScope scope = scopeService.getScopeByInum(sinum);
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response deleteClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Preconditions.checkNotNull(inum);
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				clientService.removeClient(client);
				return Response.noContent().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
