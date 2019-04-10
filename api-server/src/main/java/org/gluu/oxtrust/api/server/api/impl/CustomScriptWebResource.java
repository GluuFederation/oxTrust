package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.service.custom.CustomScriptService;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION + OxTrustApiConstants.SCRIPTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.CONFIGURATION
		+ OxTrustApiConstants.SCRIPTS, description = "Custom script web service")
@ApplicationScoped
public class CustomScriptWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private CustomScriptService customScriptService;

	public CustomScriptWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all custom scripts")
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

	@POST
	@ApiOperation(value = "Add new custom script")
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
	public Response updateCustomScript(CustomScript customScript) {
		try {
			Objects.requireNonNull(customScript, "Attempt to update null custom script");
			String inum = customScript.getInum();
			log(logger, "Update custom script " + inum);
			Optional<CustomScript> existingScript = customScriptService.getCustomScriptByINum(customScript.getBaseDn(),
					inum, new String[] {});
			if (existingScript.isPresent()) {
				customScript.setInum(existingScript.get().getInum());
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
}
