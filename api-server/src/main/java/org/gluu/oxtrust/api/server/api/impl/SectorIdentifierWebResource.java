package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.service.SectorIdentifierService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
	@Operation(summary="Get all sector identifiers",description = "Get all sectors identifiers")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthSectorIdentifier[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_READ })
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
	@Path(ApiConstants.SECTOR + ApiConstants.ID_PARAM_PATH)
	@Operation(summary="Get sector identifier", description = "Get a sector identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthSectorIdentifier.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_READ })
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
	@Operation(summary="Search sector identifiers",description = "Search sectors identifiers")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthSectorIdentifier[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_READ })
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
	@Operation(summary="Add sector identifier" ,description = "Add a sector identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthSectorIdentifier.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_WRITE })
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
	@Operation(summary="Update sector identifier",description = "Update sector identifier")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OxAuthSectorIdentifier.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_WRITE })
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
	@Operation(summary="Delete sector identifier",description = "Delete a sector identifier")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SECTOR_IDENTIFIER_WRITE })
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
