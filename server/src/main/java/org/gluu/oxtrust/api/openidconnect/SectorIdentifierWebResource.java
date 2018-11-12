package org.gluu.oxtrust.api.openidconnect;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.ldap.service.SectorIdentifierService;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

	@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SECTORS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SECTORS, description = "Sector Identifiers webservice")
public class SectorIdentifierWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private SectorIdentifierService sectorIdentifierService;

	@GET
	@ApiOperation(value = "Get all sectors identifiers")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthSectorIdentifier[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getAllSectorIdentifiers() {
		try {
			return Response.ok(sectorIdentifierService.getAllSectorIdentifiers()).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a sector identifier")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthSectorIdentifier.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getSectorIdentifierById(@PathParam(OxTrustApiConstants.INUM) @NotNull String id) {
		try {
			OxAuthSectorIdentifier sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			if (sectorIdentifier != null) {
				return Response.ok(sectorIdentifier).build();
			} else {
				return Response.ok(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search sectors identifiers")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthSectorIdentifier[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response searchSectorIdentifier(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) String pattern,
			@DefaultValue("10") @QueryParam(value = "size") int size) {
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
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthSectorIdentifier.class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response createSectorIdentifier(OxAuthSectorIdentifier identifier) {
		try {
			Preconditions.checkNotNull(identifier);
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
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = OxAuthSectorIdentifier[].class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response updateSectorIdentifier(OxAuthSectorIdentifier identifier) {
		Preconditions.checkNotNull(identifier);
		String id = identifier.getId();
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
    @ApiOperation(value = "Delete a sector identifier")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, response = OxAuthSectorIdentifier[].class, message = "Success"),
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
	public Response deleteSectorIdentifier(@PathParam(OxTrustApiConstants.INUM) @NotNull String id) {
		try {
			OxAuthSectorIdentifier sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			if (sectorIdentifier == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			sectorIdentifierService.removeSectorIdentifier(sectorIdentifier);
			return Response.noContent().build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
