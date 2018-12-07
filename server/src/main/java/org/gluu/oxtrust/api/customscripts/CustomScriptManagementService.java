/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api.customscripts;

import com.google.common.base.Optional;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.exception.InvalidScriptDataException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.service.custom.script.AbstractCustomScriptService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.gluu.oxtrust.action.ManageCustomScriptAction.*;
import static org.gluu.oxtrust.api.customscripts.CustomScriptDTO.toDTO;

/**
 * Service for custom script management
 *
 * @author Shoeb Khan
 * @version November 20, 2018
 */

@Api
@Path(OxTrustApiConstants.BASE_API_URL + "/configurations/scripts")
@ProtectedApi
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)

public class CustomScriptManagementService {

    @Inject
    private Logger log;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private ApplianceService applianceService;

    @Inject
    private ScriptErrorResource errorResource;

    @Inject
    private AbstractCustomScriptService customScriptService;

    static final String SERVER_DENIED_THE_REQUEST = "access_denied\n" + "Server denied the request.";

    /**
     * Returns all custom scripts by their types
     *
     * @returns HTTP Response
     */
    @GET
    @ApiOperation(value = "Returns all custom scripts by their types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Found custom scripts. Returning all of them"),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Failed to load custom scripts")
    })
    public Response getCustomScriptsByTypes() {

        final CustomScriptType[] allowedCustomScriptTypes = this.applianceService.getCustomScriptTypes();

        final List<CustomScript> customScripts = customScriptService.findCustomScripts(Arrays.asList(allowedCustomScriptTypes));

        final Map<CustomScriptType, List<CustomScript>> scriptsMap = loadScriptsMap(allowedCustomScriptTypes, customScripts);

        return Response.ok(convertToDtoMap(scriptsMap)).build();
    }

    /**
     * Saves the custom script
     *
     * @param customScriptDTO - the script to be saved
     *
     * @returns HTTP response
     */
    @PUT
    @ApiOperation(value = "Saves the script")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Script saved."),
            @ApiResponse(code = 400, message = "One or more of the input parameters are invalid."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Problem saving custom script")
    })
    public Response save(@Valid final CustomScriptDTO customScriptDTO) {

        try {

            validate(customScriptDTO);

            final CustomScript customScript = CustomScriptDTO.fromDTO(customScriptDTO);

            final Map<CustomScriptType, List<CustomScript>> customScriptTypeListMap = new LinkedHashMap<CustomScriptType, List<CustomScript>>();
            customScriptTypeListMap.put(customScript.getScriptType(), singletonList(customScript));

            saveCustomScriptList(customScriptTypeListMap.entrySet(), organizationService, customScriptService);

            return Response.ok(toDTO(customScript)).build();

        } catch (final BasePersistenceException ex) {

            log.error("Failed to update custom scripts", ex);
            throw ex;

        }

    }

    /**
     * Deletes the script identified by the inum
     *
     * @param inum - INUM of the script
     *
     * @returns HTTP response
     */
    @DELETE
    @Path("/{inum}")
    @ApiOperation(value = "Deletes the script identified by inum")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Deleted custom script"),
            @ApiResponse(code = 400, message = "Failed to find script for inum")
    })
    public Response removeScript(@PathParam(value = "inum") final String inum) {

        final Optional<CustomScript> scriptOpt =
                customScriptService.getCustomScriptByINum(customScriptService.baseDn(), inum);

        if (scriptOpt.isPresent()) {

            final CustomScript script = scriptOpt.get();

            customScriptService.remove(script);

            return Response.ok().build();

        }

        throw new NotFoundException(String.format("Failed to find script for inum:%s", inum));

    }

    /**
     * Returns the list of Custom Scripts that had errors
     *
     * @returns HTTP response
     */

    @Path("/errors")
    @ApiOperation(value = "Returns all the scripts having errors. ")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Found custom scripts with errors details."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Failed to load custom scripts")
    })
    public ScriptErrorResource getAllScriptWithErrors() {
        return errorResource;
    }


    /**
     * Returns the error details of script,if any, for given INUM
     *
     * @param inum
     * @return
     */
    @Path("/errors")
    @ApiOperation(value = "Returns the error details of script,if any, for given INUM ")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Found custom scripts with errors."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Script not found for inum.")
    })

    public ScriptErrorResource GetScriptErrorByINum(@PathParam(value = "inum") final String inum) {
        return errorResource;
    }

    private void validate(final CustomScriptDTO currentScript) {

        try {

            validateScriptName(currentScript.getName());

            validateUsageType(currentScript);

            validateScriptType(currentScript);

        } catch (IllegalArgumentException ex) {

            log.error(ex.getMessage(), ex);
            throw new InvalidScriptDataException(ex.getMessage(), ex);

        }
    }


    /**
     * Additional behavior for verifying that, if script's type is PERSON_AUTHENTICATION,
     * it's usage_type module property is set
     *
     */
    private void validateUsageType(CustomScriptDTO currentScript) {
        final CustomScriptType csType = CustomScriptType.getByValue(currentScript.getScriptType());

        if (csType == CustomScriptType.PERSON_AUTHENTICATION
                && currentScript.getUsageType() == null) throw new InvalidScriptDataException("Usage type is not specified");
    }

    private void validateScriptType(final CustomScriptDTO scriptDTO) {

        final String strScriptType = scriptDTO.getScriptType();

        final CustomScriptType csType = CustomScriptType.getByValue(strScriptType);

           if (isNotEmpty(scriptDTO.getDn())) {

            /* The script under action is NOT new. */

            final CustomScript existingScript = customScriptService.getCustomScriptByDn(scriptDTO.getDn(), "oxScriptType");

            if (existingScript.getScriptType() != csType) {
                throw new IllegalArgumentException("Script's type should not be changed.");
            }

        }

    }


    private static Map<CustomScriptType, List<CustomScriptDTO>> convertToDtoMap(final Map<CustomScriptType, List<CustomScript>> srcMap) {

        final Map<CustomScriptType, List<CustomScriptDTO>> dstMap = new LinkedHashMap<CustomScriptType, List<CustomScriptDTO>>();

        for (final CustomScriptType csType : srcMap.keySet()) {

            final List<CustomScriptDTO> dstList = scriptToDtoList(srcMap.get(csType));

            dstMap.put(csType, dstList);

        }

        return dstMap;
    }

    private static List<CustomScriptDTO> scriptToDtoList(final List<CustomScript> srcList) {

        final List<CustomScriptDTO> dstList = new ArrayList<CustomScriptDTO>();

        for (final CustomScript tmpScript : srcList) {

            final CustomScriptDTO dto = toDTO(tmpScript);
            dstList.add(dto);

        }

        return dstList;
    }


}
