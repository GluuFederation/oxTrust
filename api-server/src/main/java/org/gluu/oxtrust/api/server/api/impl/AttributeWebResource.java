package org.gluu.oxtrust.api.server.api.impl;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path(ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AttributeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private AttributeService attributeService;

	public AttributeWebResource() {
	}

	@GET
	@Operation(summary = "Get all attributes", description = "Gets all the gluu attributes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute[].class)
            ), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllAttributes() {
		log(logger, "Processing getAllAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes();
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ACTIVE)
	@Operation(summary = "Get all active attributes", description = "Gets all the active gluu attributes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute[].class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllActiveAttributes() {
		log(logger, "Processing getAllActivesAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes().stream()
					.filter(e -> e.getStatus() == GluuStatus.ACTIVE).collect(Collectors.toList());
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INACTIVE)
	@Operation(summary = "Get all inactive attributes", description = "Gets all inative attributes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute[].class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllInactiveAttributes() {
		log(logger, "Processing getAllInActivesAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes().stream()
					.filter(e -> e.getStatus() == GluuStatus.INACTIVE).collect(Collectors.toList());
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ATTRIBUTE + ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get attribute by inum", description = "Get an attribute by inum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute.class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAttributeByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Processing getAttributeByInum()");
		try {
			Preconditions.checkNotNull(inum);
			return Response.ok(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search attributes", description = "Perform an attribute search")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute.class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response searchAttributes(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(ApiConstants.SIZE) int size) {
		log(logger, "Processing searchAttributes()");
		try {
			List<GluuAttribute> attributes = attributeService.searchAttributes(pattern, size);
			return Response.ok(attributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add new attribute", description = "Adds a new Gluu attribute")
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute.class)
            ), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createAttribute(GluuAttribute gluuAttribute) {
		log(logger, "Processing createAttribute()");
		try {
			Preconditions.checkNotNull(gluuAttribute, "Attempt to create null attribute");
			String inum = attributeService.generateInumForNewAttribute();
			gluuAttribute.setInum(inum);
			gluuAttribute.setDn(attributeService.getDnForAttribute(inum));
			attributeService.addAttribute(gluuAttribute);
			return Response.ok(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary="Update new attribute", description = "Updates a gluu attribute")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(
                    schema = @Schema(implementation = GluuAttribute.class)
            ), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateAttribute(GluuAttribute gluuAttribute) {
		log(logger, "Processing updateAttribute()");
		try {
			Preconditions.checkNotNull(gluuAttribute, "Attempt to update null attribute");
			String inum = gluuAttribute.getInum();
			GluuAttribute existingAttribute = attributeService.getAttributeByInum(inum);
			if (existingAttribute != null) {
				gluuAttribute.setInum(existingAttribute.getInum());
				attributeService.updateAttribute(gluuAttribute);
				return Response.ok(attributeService.getAttributeByInum(existingAttribute.getInum())).build();
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
	@Operation(summary = "Delete gluu attribute", description = "Deletes a gluu attribute")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAttribute(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Processing deleteAttribute()");
		try {
			Preconditions.checkNotNull(inum);
			GluuAttribute gluuAttribute = attributeService.getAttributeByInum(inum);
			if (gluuAttribute != null) {
				attributeService.removeAttribute(gluuAttribute);
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
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAttributes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

}