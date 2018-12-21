/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.customscripts;

import com.google.common.base.Optional;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.custom.script.model.ScriptError;
import org.xdi.service.custom.script.AbstractCustomScriptService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.gluu.oxtrust.api.customscripts.CustomScriptDTO.toDTO;


/**
 * Error resource class for custom script
 *
 * @author Shoeb Khan
 * @version November 20, 2018
 */

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)

public class ScriptErrorResource {

    @Inject
    private AbstractCustomScriptService customScriptService;

    /**
     * {@link CustomScriptManagementService#getAllScriptWithErrors()} }
     */
    @GET
    public Response getAllScriptWithErrors() {

        final List<CustomScript> allCustomScripts = customScriptService.findAllCustomScripts(
                new String[] {"displayName","oxScriptError", "description"});

        final List<CustomScriptDTO> scriptsWithError = new ArrayList<CustomScriptDTO>();

        for (final Iterator<CustomScript> iterator = allCustomScripts.iterator(); iterator.hasNext(); ) {

            final CustomScript customScript = iterator.next();

            if (customScript.getScriptError() != null) {

                final CustomScriptDTO scriptDTO = toDTO(customScript);
                scriptsWithError.add(scriptDTO);

            }
        }

        return Response.ok(scriptsWithError).build();
    }


    /**
     * {@link CustomScriptManagementService#GetScriptErrorByINum(String)} }
     */
    @GET
    @Path("/{inum}")
    public Response GetScriptErrorByINum(@PathParam(value = "inum") final String inum) {

        final String baseDn = customScriptService.baseDn();

        final Optional<CustomScript> scriptOpt =
                customScriptService.getCustomScriptByINum(baseDn,inum, new String[]{"oxScriptError"});

        if (! scriptOpt.isPresent()) {
            throw new NotFoundException(String.format("Script not found for inum:%s", inum));
        }

        final CustomScript customScript =  scriptOpt.get();
        final ScriptError scriptError = customScript.getScriptError();

        if (scriptError == null) {
            return Response.ok().build();
        }

        return Response.ok(scriptError).build();
    }
}
