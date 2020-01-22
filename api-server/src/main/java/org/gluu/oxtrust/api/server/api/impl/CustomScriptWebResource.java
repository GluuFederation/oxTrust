package org.gluu.oxtrust.api.server.api.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.service.custom.CustomScriptService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
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
	@Operation(summary = "Get all custom scripts", description = "Get all custom scripts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CustomScript[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
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
	@Operation(summary = "Get person auth scripts", description = "Get person authentications scripts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CustomScript[].class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
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
	@Operation(summary = "Get scripts by inum", description = "Get scripts by inum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CustomScript.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
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
	@Operation(summary = "Add new custom script", description = "Add new custom script")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CustomScript.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createCustomScript(CustomScript customScript) {
		log(logger, "Add new custom script ");
		try {
			Objects.requireNonNull(customScript, "Attempt to create null custom script");
			String inum = customScript.getInum();
			if (StringHelper.isEmpty(inum)) {
				inum = UUID.randomUUID().toString();
			}
			customScript.setDn(customScriptService.buildDn(inum));
			customScript.setInum(inum);
			customScriptService.add(customScript);
			return Response.ok(customScriptService.getScriptByInum(inum)).build();

		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Operation(summary = "Update custom script", description = "Update custom script")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CustomScript.class)), description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
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
	@Operation(summary = "Delete custom script", description = "Delete an custom script")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Server error") })
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
