package org.gluu.oxtrust.api.openidconnect;

import java.util.List;
import java.util.UUID;

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

import com.google.common.base.Preconditions;
import org.gluu.oxtrust.ldap.service.SectorIdentifierService;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SECTORS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SectorIdentifierWebResource extends BaseWebResource {
	@Inject
	private Logger logger;

	@Inject
	private SectorIdentifierService sectorIdentifierService;

	public SectorIdentifierWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all sectors identifiers")
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
	public Response deleteSectorIdentifier(@PathParam(OxTrustApiConstants.INUM) @NotNull String id) {
		try {
			OxAuthSectorIdentifier sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
			sectorIdentifierService.removeSectorIdentifier(sectorIdentifier);
			return Response.ok().build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
