package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.service.custom.CustomScriptService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
		+ ApiConstants.SCRIPTS, description = "Custom script web service")
@ApplicationScoped
public class CustomScriptWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private CustomScriptService customScriptService;

	@Inject
	private ConfigurationService configurationService;

	public CustomScriptWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all custom scripts")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response listCustomScripts() {
		log(logger, "Get all custom scripts");
		try {
			List<CustomScript> customScripts = customScriptService.findAllCustomScripts(null);
			return Response.ok(customScripts).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.TYPE_PATH + ApiConstants.TYPE_PARAM_PATH)
	@ApiOperation(value = "Get person authentications scripts")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response listCustomScriptsByType(@PathParam(ApiConstants.TYPE) @NotNull String type) {
		log(logger, "Get custom scripts of type: " + type);
		try {
			List<String> allowedCustomScriptTypes = Stream.of(this.configurationService.getCustomScriptTypes())
					.map(e -> e.getValue()).collect(Collectors.toList());
			if (allowedCustomScriptTypes.contains(type)) {
				List<CustomScript> customScripts = customScriptService.findAllCustomScripts(null).stream()
						.filter(e -> e.getScriptType().getValue().equalsIgnoreCase(type)).collect(Collectors.toList());
				return Response.ok(customScripts).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get scripts by inum")
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getCustomScriptsByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Get scripts by inum");
		try {
			CustomScript script = customScriptService.getScriptByInum(inum);
			if (script != null) {
				return Response.ok(script).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add new custom script")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createCustomScript(CustomScript customScript) {
		log(logger, "Add new custom script ");
		try {
			Objects.requireNonNull(customScript, "Attempt to create null custom script");
			if (StringHelper.isEmpty(customScript.getDn())) {
				String inum = UUID.randomUUID().toString();
				String dn = customScriptService.buildDn(inum);
				customScript.setDn(dn);
				customScript.setInum(inum);
			}
			customScriptService.add(customScript);
			return Response.ok().build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update custom script")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateCustomScript(CustomScript customScript) {
		try {
			Objects.requireNonNull(customScript, "Attempt to update null custom script");
			String inum = customScript.getInum();
			log(logger, "Update custom script " + inum);
			CustomScript existingScript = customScriptService.getScriptByInum(customScript.getInum());
			if (existingScript != null) {
				customScript.setInum(existingScript.getInum());
				customScriptService.update(customScript);
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
	@Path(ApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an custom script")
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteCustomScript(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Delete custom script" + inum);
		try {
			Objects.requireNonNull(inum);
			CustomScript existingScript = customScriptService.getScriptByInum(inum);
			if (existingScript != null) {
				customScriptService.remove(existingScript);
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
