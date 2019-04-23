package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

import org.gluu.oxtrust.ldap.service.SectorIdentifierService;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(ApiConstants.BASE_API_URL + ApiConstants.SECTORS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SectorIdentifierWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private SectorIdentifierService sectorIdentifierService;

	public SectorIdentifierWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all sectors identifiers")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllSectorIdentifiers() {
		log("Get all sectors identifiers ");
		try {
			return Response.ok(sectorIdentifierService.getAllSectorIdentifiers()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ID_PARAM_PATH)
	@ApiOperation(value = "Get a sector identifier")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getSectorIdentifierById(@PathParam(ApiConstants.ID) @NotNull String id) {
		log("Get sector identifier having id: " + id);
		try {
			Objects.requireNonNull(id);
			OxAuthSectorIdentifier sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			if (sectorIdentifier != null) {
				return Response.ok(sectorIdentifier).build();
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
	@ApiOperation(value = "Search sectors identifiers")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response searchSectorIdentifier(@QueryParam(ApiConstants.SEARCH_PATTERN) String pattern,
			@DefaultValue("10") @QueryParam(value = "size") int size) {
		log("Search sector with pattern= " + pattern + "and size: " + size);
		try {
			List<OxAuthSectorIdentifier> sectorIdentifiers = sectorIdentifierService.searchSectorIdentifiers(pattern,
					size);
			return Response.ok(sectorIdentifiers).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add a sector identifier")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createSectorIdentifier(OxAuthSectorIdentifier identifier) {
		log("Create a sector identifier");
		try {
			Objects.requireNonNull(identifier);
			String oxId = identifier.getId();
			if (oxId == null) {
				oxId = UUID.randomUUID().toString();
			}
			identifier.setBaseDn(sectorIdentifierService.getDnForSectorIdentifier(oxId));
			sectorIdentifierService.addSectorIdentifier(identifier);
			return Response.ok(sectorIdentifierService.getSectorIdentifierById(oxId)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update sector identifier")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateSectorIdentifier(OxAuthSectorIdentifier identifier) {
		Objects.requireNonNull(identifier);
		String id = identifier.getId();
		log(" Update sector identifier " + id);
		try {
			if (id == null) {
				id = UUID.randomUUID().toString();
			}
			OxAuthSectorIdentifier existingIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			if (existingIdentifier != null) {
				identifier.setId(existingIdentifier.getId());
				sectorIdentifierService.updateSectorIdentifier(identifier);
				return Response.ok(sectorIdentifierService.getSectorIdentifierById(id)).build();
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
	@ApiOperation(value = "Delete a sector identifier")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteSectorIdentifier(@PathParam(ApiConstants.INUM) @NotNull String id) {
		log("Delete sector identifier with id: " + id);
		try {
			OxAuthSectorIdentifier sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			if (sectorIdentifier != null) {
				sectorIdentifierService.removeSectorIdentifier(sectorIdentifier);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

}
