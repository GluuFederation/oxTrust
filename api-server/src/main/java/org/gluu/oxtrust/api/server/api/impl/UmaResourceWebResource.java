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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.uma.ResourceSetService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxauth.model.uma.persistence.UmaScopeDescription;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.UMA + OxTrustApiConstants.RESOURCES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UmaResourceWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ResourceSetService umaResourcesService;

	@Inject
	private ScopeDescriptionService scopeDescriptionService;

	@Inject
	private ClientService clientService;

	@GET
	@ApiOperation(value = "Get uma resources")
	public Response listUmaResources() {
		try {
			log(logger, "Get uma resources");
			List<UmaResource> umaResources = umaResourcesService.getAllResources(100);
			return Response.ok(umaResources).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search uma resources")
	public Response searchUmaResources(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@QueryParam(OxTrustApiConstants.SIZE) @NotNull int size) {
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
	@Path(OxTrustApiConstants.ID_PARAM_PATH)
	@ApiOperation(value = "Get a uma resource by id")
	public Response getUmaResourceById(@PathParam(OxTrustApiConstants.ID) @NotNull String id) {
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
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.CLIENTS)
	@ApiOperation(value = "Get clients of uma resource")
	public Response getUmaResourceClients(@PathParam(OxTrustApiConstants.ID) @NotNull String id) {
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
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.SCOPES)
	@ApiOperation(value = "Get scopes of uma resource")
	public Response getUmaResourceScopes(@PathParam(OxTrustApiConstants.ID) @NotNull String id) {
		try {
			log(logger, "Get scopes of uma resource having id " + id);
			Objects.requireNonNull(id, "id should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			if (resources != null && !resources.isEmpty()) {
				UmaResource resource = resources.get(0);
				List<String> scopesDn = resource.getScopes();
				List<UmaScopeDescription> scopes = new ArrayList<UmaScopeDescription>();
				if (scopesDn != null) {
					for (String scopeDn : scopesDn) {
						scopes.add(scopeDescriptionService.getScopeDescriptionByDn(scopeDn));
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
	@ApiOperation(value = "add client to uma resource")
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.CLIENTS + OxTrustApiConstants.INUM_PARAM_PATH)
	public Response addClientToUmaResource(@PathParam(OxTrustApiConstants.ID) @NotNull String id,
			@PathParam(OxTrustApiConstants.INUM) @NotNull String clientInum) {
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
	@ApiOperation(value = "Remove client from uma resource")
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.CLIENTS + OxTrustApiConstants.INUM_PARAM_PATH)
	public Response removeClientToUmaResource(@PathParam(OxTrustApiConstants.ID) @NotNull String id,
			@PathParam(OxTrustApiConstants.INUM) @NotNull String clientInum) {
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
	@ApiOperation(value = "add scope to uma resource")
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.SCOPES + OxTrustApiConstants.INUM_PARAM_PATH)
	public Response addScopeToUmaResource(@PathParam(OxTrustApiConstants.ID) @NotNull String id,
			@PathParam(OxTrustApiConstants.INUM) @NotNull String scopeInum) {
		log(logger, "Add scope " + scopeInum + " to uma resource " + id);
		try {
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(scopeInum, "scope inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			UmaScopeDescription umaScope = scopeDescriptionService.getUmaScopeByInum(scopeInum);
			if (resources != null && !resources.isEmpty() && umaScope != null) {
				UmaResource umaResource = resources.get(0);
				List<String> scopesDn = new ArrayList<String>();
				if (umaResource.getScopes() != null) {
					scopesDn.addAll(umaResource.getScopes());
				}
				scopesDn.add(scopeDescriptionService.getDnForScopeDescription(scopeInum));
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
	@ApiOperation(value = "remove a scope from uma resource")
	@Path(OxTrustApiConstants.ID_PARAM_PATH + OxTrustApiConstants.SCOPES + OxTrustApiConstants.INUM_PARAM_PATH)
	public Response removeScopeToUmaResource(@PathParam(OxTrustApiConstants.ID) @NotNull String id,
			@PathParam(OxTrustApiConstants.INUM) @NotNull String scopeInum) {
		try {
			log(logger, "Remove scope " + scopeInum + " from uma resource " + id);
			Objects.requireNonNull(id, "Uma id should not be null");
			Objects.requireNonNull(scopeInum, "scope inum should not be null");
			List<UmaResource> resources = umaResourcesService.findResourcesById(id);
			UmaScopeDescription umaScope = scopeDescriptionService.getUmaScopeByInum(scopeInum);
			if (resources != null && !resources.isEmpty() && umaScope != null) {
				UmaResource umaResource = resources.get(0);
				List<String> scopesDn = new ArrayList<String>();
				if (umaResource.getScopes() != null) {
					scopesDn.addAll(umaResource.getScopes());
				}
				scopesDn.remove(scopeDescriptionService.getDnForScopeDescription(scopeInum));
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
	@ApiOperation(value = "Add new uma resource")
	public Response createUmaResource(UmaResource umaResource) {
		try {
			log(logger, "Add new uma resource");
			Objects.requireNonNull(umaResource, "Attempt to create null resource");
			if (umaResource.getId() == null) {
				umaResource.setId(UUID.randomUUID().toString());
			}
			String inum = umaResourcesService.generateInumForNewResource();
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
	@ApiOperation(value = "Update uma resource")
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
	@Path(OxTrustApiConstants.ID_PARAM_PATH)
	@ApiOperation(value = "Delete a uma resource")
	public Response deleteUmaResource(@PathParam(OxTrustApiConstants.ID) @NotNull String id) {
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
