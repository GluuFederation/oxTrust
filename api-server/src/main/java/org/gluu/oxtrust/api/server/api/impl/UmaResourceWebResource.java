package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.uma.ResourceSetService;
import org.gluu.oxtrust.service.uma.UmaScopeService;
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
import java.util.UUID;

@Path(ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UmaResourceWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ResourceSetService umaResourcesService;

	@Inject
	private UmaScopeService scopeDescriptionService;

	@Inject
	private ClientService clientService;
	
	@GET
	@Operation(summary = "Get UMA resources", description = "Get uma resources")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_READ })
	public Response listUmaResources() {
		try {
			log(logger, "Get uma resources");
			return Response.ok(umaResourcesService.getAllResources(1000)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search UMA resources", description = "Search uma resources")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_READ })
	public Response searchUmaResources(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@QueryParam(ApiConstants.SIZE) @NotNull int size) {
		try {
			log(logger, "Search uma resources with pattern = " + pattern + " and size = " + size);
			List<UmaResource> ressources = umaResourcesService.findResources(pattern, size);
			return Response.ok(ressources).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ID_PARAM_PATH)
	@Operation(summary = "Get UMA resource by id", description = "Get a uma resource by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_READ })
	public Response getUmaResourceById(@PathParam(ApiConstants.ID) @NotNull String id) {
		try {
			log(logger, "Get uma resource by id " + id);
			Objects.requireNonNull(id, "id should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			if (resources != null && !resources.isEmpty()) {
				return Response.ok(resources.get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.CLIENTS)
	@Operation(summary = "Get clients of UMA resources", description = "Get clients of uma resource")
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_READ })
	public Response getUmaResourceClients(@PathParam(ApiConstants.ID) @NotNull String id) {
		try {
			log(logger, "Get clients of uma resource having id " + id);
			Objects.requireNonNull(id, "id should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			if (resources != null && !resources.isEmpty()) {
				UmaResource resource = resources.get(0);
				List<String> clientsDn = resource.getClients();
				List<OxAuthClient> clients = new ArrayList<OxAuthClient>();
				if (clientsDn != null) {
					for (String clientDn : clientsDn) {
						clients.add(clientService.getClientByDn(clientDn));
					}
				}
				return Response.ok(clients).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.SCOPES)
	@Operation(summary = "Get UMA resource scopes", description = "Get scopes of uma resource")
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_READ })
	public Response getUmaResourceScopes(@PathParam(ApiConstants.ID) @NotNull String id) {
		try {
			log(logger, "Get scopes of uma resource having id " + id);
			Objects.requireNonNull(id, "id should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			if (resources != null && !resources.isEmpty()) {
				UmaResource resource = resources.get(0);
				List<String> scopesDn = resource.getScopes();
				List<Scope> scopes = new ArrayList<Scope>();
				if (scopesDn != null) {
					for (String scopeDn : scopesDn) {
						scopes.add(scopeDescriptionService.getUmaScopeByDn(scopeDn));
					}
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

	@POST
	@Operation(summary = "Add UMA resource client", description = "add client to uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.CLIENTS + ApiConstants.INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response addClientToUmaResource(@PathParam(ApiConstants.ID) @NotNull String id,
			@PathParam(ApiConstants.INUM) @NotNull String clientInum) {
		try {
			log(logger, "Add client " + clientInum + " to uma resource " + id);
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(clientInum, "Client inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			OxAuthClient client = clientService.getClientByInum(clientInum);
			if (resources != null && !resources.isEmpty() && client != null) {
				UmaResource umaResource = resources.get(0);
				List<String> clientsDn = new ArrayList<String>();
				if (umaResource.getClients() != null) {
					clientsDn.addAll(umaResource.getClients());
				}
				clientsDn.add(clientService.getDnForClient(clientInum));
				umaResource.setClients(clientsDn);
				umaResourcesService.updateResource(umaResource);
				return Response.status(Response.Status.CREATED).entity(umaResourcesService.findResourcesById(id).get(0))
						.build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Operation(summary = "Remove UMA resource client", description = "Remove client from uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.CLIENTS + ApiConstants.INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response removeClientToUmaResource(@PathParam(ApiConstants.ID) @NotNull String id,
			@PathParam(ApiConstants.INUM) @NotNull String clientInum) {
		try {
			log(logger, "Remove client " + clientInum + " from uma resource " + id);
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(clientInum, "Client inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			OxAuthClient client = clientService.getClientByInum(clientInum);
			if (resources != null && !resources.isEmpty() && client != null) {
				UmaResource umaResource = resources.get(0);
				List<String> clientsDn = new ArrayList<String>();
				if (umaResource.getClients() != null) {
					clientsDn.addAll(umaResource.getClients());
				}
				clientsDn.remove(clientService.getDnForClient(clientInum));
				umaResource.setClients(clientsDn);
				umaResourcesService.updateResource(umaResource);
				return Response.ok(umaResourcesService.findResourcesById(id).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add UMA resource scope", description = "add scope to uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.SCOPES + ApiConstants.INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response addScopeToUmaResource(@PathParam(ApiConstants.ID) @NotNull String id,
			@PathParam(ApiConstants.INUM) @NotNull String scopeInum) {
		log(logger, "Add scope " + scopeInum + " to uma resource " + id);
		try {
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(scopeInum, "scope inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			Scope umaScope = scopeDescriptionService.getUmaScopeByInum(scopeInum);
			if (resources != null && !resources.isEmpty() && umaScope != null) {
				UmaResource umaResource = resources.get(0);
				List<String> scopesDn = new ArrayList<String>();
				if (umaResource.getScopes() != null) {
					scopesDn.addAll(umaResource.getScopes());
				}
				scopesDn.add(scopeDescriptionService.getDnForScope(scopeInum));
				umaResource.setScopes(scopesDn);
				umaResourcesService.updateResource(umaResource);
				return Response.ok(umaResourcesService.findResourcesById(id).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Operation(summary = "Remove UMA resource scope", description = "remove a scope from uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@Path(ApiConstants.ID_PARAM_PATH + ApiConstants.SCOPES + ApiConstants.INUM_PARAM_PATH)
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response removeScopeToUmaResource(@PathParam(ApiConstants.ID) @NotNull String id,
			@PathParam(ApiConstants.INUM) @NotNull String scopeInum) {
		try {
			log(logger, "Remove scope " + scopeInum + " from uma resource " + id);
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(scopeInum, "scope inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			Scope umaScope = scopeDescriptionService.getUmaScopeByInum(scopeInum);
			if (resources != null && !resources.isEmpty() && umaScope != null) {
				UmaResource umaResource = resources.get(0);
				List<String> scopesDn = new ArrayList<String>();
				if (umaResource.getScopes() != null) {
					scopesDn.addAll(umaResource.getScopes());
				}
				scopesDn.remove(scopeDescriptionService.getDnForScope(scopeInum));
				umaResource.setScopes(scopesDn);
				umaResourcesService.updateResource(umaResource);
				return Response.ok(umaResourcesService.findResourcesById(id).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add new UMA resource", description = "Add new uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response createUmaResource(UmaResource umaResource) {
		try {
			log(logger, "Add new uma resource");
			Objects.requireNonNull(umaResource, "Attempt to create null resource");
			if (umaResource.getId() == null) {
				umaResource.setId(UUID.randomUUID().toString());
			}
			String inum = umaResource.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = umaResourcesService.generateInumForNewResource();
			}
			umaResource.setDn(umaResourcesService.getDnForResource(umaResource.getId()));
			umaResource.setInum(inum);
			umaResourcesService.addResource(umaResource);
			List<UmaResource> umaResources = umaResourcesService.findResourcesById(umaResource.getId());
			return Response.ok(umaResources.get(0)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update UMA resource", description = "Update uma resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UmaResource.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response updateUmaResource(UmaResource umaResource) {
		try {
			String id = umaResource.getId();
			log(logger, "Update uma resource having id " + id);
			Objects.requireNonNull(id, " id should not be null");
			Objects.requireNonNull(umaResource, "Attempt to update null uma resource");
			List<UmaResource> existingResources = umaResourcesService.findResourcesById(id);
			if (existingResources != null && !existingResources.isEmpty()) {
				umaResource.setDn(umaResourcesService.getDnForResource(id));
				umaResourcesService.updateResource(umaResource);
				return Response.ok(umaResourcesService.findResourcesById(id).get(0)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(ApiConstants.ID_PARAM_PATH)
	@Operation(summary = "Delete UMA resource", description = "Delete a uma resource")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_RESOURCE_WRITE })
	public Response deleteUmaResource(@PathParam(ApiConstants.ID) @NotNull String id) {
		try {
			log(logger, "Delete uma resource with id = " + id);
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			if (resources != null && !resources.isEmpty()) {
				umaResourcesService.removeResource(resources.get(0));
				log(logger, "Delete a uma resource having id " + id + " done");
				return Response.ok().build();
			} else {
				log(logger, "No uma scope found with " + id);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
