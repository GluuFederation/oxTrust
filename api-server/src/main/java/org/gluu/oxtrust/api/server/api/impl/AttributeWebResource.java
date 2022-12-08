package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.stream.Collectors;

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

import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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
	@Operation(summary = "Get all attributes", description = "Gets all the gluu attributes",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_READ })
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
	@Operation(summary = "Get all active attributes", description = "Gets all the active gluu attributes",
	security = @SecurityRequirement(name = "oauth2", scopes = {
					ApiScopeConstants.SCOPE_ATTRIBUTE_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute[].class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_READ })
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
	@Operation(summary = "Get all inactive attributes", description = "Gets all inative attributes",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute[].class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_READ })
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
	@Operation(summary = "Get attribute by inum", description = "Get an attribute by inum",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_READ })
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
	@Operation(summary = "Search attributes", description = "Perform an attribute search",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_READ }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_READ })
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
	@Operation(summary = "Add new attribute", description = "Adds a new Gluu attribute",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE })
	public Response createAttribute(GluuAttribute gluuAttribute) {
		log(logger, "Processing createAttribute()");
		try {
			Preconditions.checkNotNull(gluuAttribute, "Attempt to create null attribute");
			String inum = gluuAttribute.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = attributeService.generateInumForNewAttribute();
			}
			gluuAttribute.setInum(inum);
			gluuAttribute.setDn(attributeService.getDnForAttribute(inum));
			attributeService.addAttribute(gluuAttribute);
			return Response.status(Response.Status.CREATED).entity(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update new attribute", description = "Updates a gluu attribute",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE }))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE })
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
	@Operation(summary = "Delete gluu attribute", description = "Deletes a gluu attribute",
	security = @SecurityRequirement(name = "oauth2", scopes = {
			ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE }))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "404", description = "Not found"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE })
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
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_ATTRIBUTE_WRITE })
	public Response deleteAttributes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

}