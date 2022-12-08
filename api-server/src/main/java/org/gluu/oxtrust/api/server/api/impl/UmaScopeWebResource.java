package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.uma.UmaScopeService;
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
import java.util.List;
import java.util.Objects;

@Path(ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UmaScopeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private UmaScopeService scopeDescriptionService;

	@GET
	@Operation(summary = "Get UMA scopes", description = "Get uma scopes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_READ })
	public Response listUmaScopes() {
		log(logger, "Get uma scopes");
		try {
			List<Scope> umaScopeDescriptions = scopeDescriptionService.getAllUmaScopes(100);
			return Response.ok(umaScopeDescriptions).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search UMA scopes", description = "Search uma scopes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_READ })
	public Response searchUmaScopes(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern) {
		log(logger, "Search uma scope with pattern = " + pattern);
		try {
			List<Scope> scopes = scopeDescriptionService.findUmaScopes(pattern, 100);
			return Response.ok(scopes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get UMA scope by inum", description = "Get a uma scope by inum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_READ })
	public Response getUmaScopeByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get uma scope " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Scope scope = scopeDescriptionService.getUmaScopeByInum(inum);
			if (scope != null) {
				return Response.ok(scope).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add new UMA scope", description = "Add new uma scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Scope.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_WRITE })
	public Response createUmaScope(Scope umaScopeDescription) {
		log(logger, "Add new uma scope");
		try {
			Objects.requireNonNull(umaScopeDescription, "Attempt to create null uma scope");
			String inum = umaScopeDescription.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = scopeDescriptionService.generateInumForNewScope();
			}
			umaScopeDescription.setDn(scopeDescriptionService.getDnForScope(inum));
			umaScopeDescription.setInum(inum);
			scopeDescriptionService.addUmaScope(umaScopeDescription);
			return Response.status(Response.Status.CREATED).entity(scopeDescriptionService.getUmaScopeByInum(inum))
					.build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update UMA scope", description = "Update uma scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_WRITE })
	public Response updateUmaScope(Scope umaScopeDescription) {
		String inum = umaScopeDescription.getInum();
		log(logger, "Update uma scope " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Objects.requireNonNull(umaScopeDescription, "Attempt to update null uma scope");
			Scope existingScope = scopeDescriptionService.getUmaScopeByInum(inum);
			if (existingScope != null) {
				umaScopeDescription.setDn(scopeDescriptionService.getDnForScope(inum));
				scopeDescriptionService.updateUmaScope(umaScopeDescription);
				return Response.ok(scopeDescriptionService.getUmaScopeByInum(inum)).build();
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
	@Operation(summary = "Delete UMA scope", description = "Delete a uma scope")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_UMA_SCOPE_WRITE })
	public Response deleteUmaScope(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Delete a uma scope having inum " + inum);
		try {
			Scope existingScope = scopeDescriptionService.getUmaScopeByInum(inum);
			if (existingScope != null) {
				scopeDescriptionService.removeUmaScope(existingScope);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	//
	// It is unclear what this method does. So we comment it until we find out
	// what it is supposed to do.
	// @DELETE
	// @ProtectedApi(scopes = { SCOPE_UMA_SCOPE_WRITE })
	// public Response deleteAllUmaScopes() {
	// return Response.status(Response.Status.UNAUTHORIZED).build();
	// }
}
