package org.gluu.oxtrust.api.openidconnect;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SCOPES, description = "Scopes webservice")
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
	@ApiOperation(value = "Get all scopes")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getAllScopes() {
		try {
			return Response.ok(scopeService.searchScopes(null, 100)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a specific openid connect scope")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getScopeByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
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
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search openid connect scopes")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope[].class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response searchScope(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) String pattern,
			@DefaultValue("10") @QueryParam("size") int size) {
		try {
			List<OxAuthScope> scopes = scopeService.searchScopes(pattern, size);
			return Response.ok(scopes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add an openidconnect scope")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope.class, message = "Success"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response createScope(OxAuthScope scope) {
		try {
			Preconditions.checkNotNull(scope, "Attempt to create null scope");
			String inum = scopeService.generateInumForNewScope();
			scope.setInum(inum);
			scope.setDn(scopeService.getDnForScope(inum));
			scopeService.addScope(scope);
			return Response.ok(scopeService.getScopeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update openidconect scope")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response updateScope(OxAuthScope scope) {
		String inum = scope.getInum();
		try {
			Preconditions.checkNotNull(scope, "Attempt to update scope null value");
			Preconditions.checkNotNull(inum);
			OxAuthScope existingScope = scopeService.getScopeByInum(inum);
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.CLAIMS)
	@ApiOperation(value = "List all claims of a scope")
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, response = OxAuthScope.class, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response getScopeClaims(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Preconditions.checkNotNull(inum, "inum should not be null");
			OxAuthScope oxAuthScope = scopeService.getScopeByInum(inum);
			List<String> claimsDn = new ArrayList<String>();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			if (oxAuthScope != null) {
				claimsDn = oxAuthScope.getOxAuthClaims();
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an openidconnect scope")
	@ApiResponses(
			value = {
					@ApiResponse(code = 204, message = "Success"),
					@ApiResponse(code = 404, message = "Not found"),
					@ApiResponse(code = 500, message = "Server error")
			}
	)
	public Response deleteScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				scopeService.removeScope(scope);
				return Response.noContent().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
