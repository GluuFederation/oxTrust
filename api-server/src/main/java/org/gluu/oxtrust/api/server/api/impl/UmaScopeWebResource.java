package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.Objects;

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

import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.uma.UmaScopeService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

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
	@ApiOperation(value = "Get uma scopes")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Scope[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
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
	@ApiOperation(value = "Search uma scopes")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Scope[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
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
	@ApiOperation(value = "Get a uma scope by inum")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Scope.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
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
	@ApiOperation(value = "Add new uma scope")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Scope.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createUmaScope(Scope umaScopeDescription) {
		log(logger, "Add new uma scope");
		try {
			Objects.requireNonNull(umaScopeDescription, "Attempt to create null uma scope");
			String inum = scopeDescriptionService.generateInumForNewScope();
			umaScopeDescription.setDn(scopeDescriptionService.getDnForScope(inum));
			umaScopeDescription.setInum(inum);
			scopeDescriptionService.addUmaScope(umaScopeDescription);
			return Response.ok(scopeDescriptionService.getUmaScopeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update uma scope")
	@ApiResponses(value = { @ApiResponse(code = 200, response = Scope.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
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
	@ApiOperation(value = "Delete a uma scope")
	@ApiResponses(value = { @ApiResponse(code = 200, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
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

	@DELETE
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAllUmaScopes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
