package org.gluu.oxtrust.api.openidconnect;

import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
public class ScopeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ScopeService scopeService;

	public ScopeWebResource() {
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a specific openidconnect scope")
	public String getScopeByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return mapper.writeValueAsString(scope);
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when retrieving scope " + inum, response);
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Add an openidconnect scope")
	public String createScope(OxAuthScope scope, @Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(scope, "Attempt to create null scope");
			String inum = scopeService.generateInumForNewScope();
			scope.setInum(inum);
			scopeService.addScope(scope);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return inum;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during scope registration", response);
		}
	}

	@PUT
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Update openidconect scope")
	public String updateScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, OxAuthScope scope,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(scope, "Attempt to update scope null value");
			OxAuthScope existingScope = scopeService.getScopeByInum(inum);
			if (existingScope != null) {
				scope.setInum(existingScope.getInum());
				scopeService.updateScope(scope);
				response.setStatus(HttpServletResponse.SC_OK);
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return OxTrustConstants.RESULT_FAILURE;
			}
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during scope update", response);
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Delete an openidconnect scope")
	public String deleteScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum,
			@Context HttpServletResponse response) {
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				scopeService.removeScope(scope);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when deleting scope " + inum, response);
		}
	}

}
