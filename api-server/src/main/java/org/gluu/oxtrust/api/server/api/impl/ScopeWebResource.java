package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.model.GluuAttribute;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.ScopeService;
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

@Path(ApiConstants.BASE_API_URL + ApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScopeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ScopeService scopeService;
	@Inject
	private AttributeService attributeService;

	public ScopeWebResource() {
	}
	
	@GET
	@Operation(summary = "Get all scopes", description = "Get all scopes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_READ })
	public Response getAllScopes() {
		log(logger, "List openid connect scopes ");
		try {
			return Response.ok(scopeService.searchScopes(null, 100)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@Operation(summary = "Get openid scope", description = "Get a specific openid connect scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_READ })
	public Response getScopeByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get openid connect scope by " + inum);
		try {
			Scope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				return Response.ok(scope).build();
			} else {
				return Response.ok(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@Operation(summary = "Search openid connect scopes", description = "Search openid connect scopes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_READ })
	public Response searchScope(@QueryParam(ApiConstants.SEARCH_PATTERN) String pattern,
			@DefaultValue("10") @QueryParam("size") int size) {
		log(logger, "Search openid connect scopes with pattern= " + pattern);
		try {
			List<Scope> scopes = scopeService.searchScopes(pattern, size);
			return Response.ok(scopes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Add openid connect scope", description = "Add an openidconnect scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Scope.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_WRITE })
	public Response createScope(Scope scope) {
		log(logger, "Create scope");
		try {
			Objects.requireNonNull(scope, "Attempt to create null scope");
			String inum = scope.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = scopeService.generateInumForNewScope();
			}
			scope.setInum(inum);
			scope.setDn(scopeService.getDnForScope(inum));
			scopeService.addScope(scope);
			return Response.status(Response.Status.CREATED).entity(scopeService.getScopeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update openid connect scope", description = "Update openidconect scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Scope.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_WRITE })
	public Response updateScope(Scope scope) {
		String inum = scope.getInum();
		log(logger, "Update scope " + inum);
		try {
			Objects.requireNonNull(scope, "Attempt to update scope null value");
			Objects.requireNonNull(inum);
			Scope existingScope = scopeService.getScopeByInum(inum);
			if (existingScope != null) {
				scope.setInum(existingScope.getInum());
				scopeService.updateScope(scope);
				return Response.ok(scopeService.getScopeByInum(inum)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH + ApiConstants.CLAIMS)
	@Operation(summary = "Get scope claims", description = "List all claims of a scope")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuAttribute[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_WRITE })
	public Response getScopeClaims(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "List all claims of scope ==> " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			Scope oxAuthScope = scopeService.getScopeByInum(inum);
			List<String> claimsDn = new ArrayList<String>();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			if (oxAuthScope != null) {
				claimsDn.addAll(oxAuthScope.getOxAuthClaims());
				for (String claimDn : claimsDn) {
					attributes.add(attributeService.getAttributeByDn(claimDn));
				}
				return Response.ok(attributes).build();
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
	@Operation(summary = "Delete openid connect scope", description = "Delete an openidconnect scope")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_OXTRUST_SCOPE_WRITE })
	public Response deleteScope(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Delete openidconnect scope " + inum);
		try {
			Scope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				scopeService.removeScope(scope);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
