/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api;

import com.google.common.base.Optional;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.exception.InvalidScriptDataException;
import org.gluu.oxtrust.exception.ScriptNotFoundException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.CustomScriptDTO;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.custom.script.model.ScriptError;
import org.xdi.service.custom.script.AbstractCustomScriptService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.gluu.oxtrust.action.ManageCustomScriptAction.*;
import static org.gluu.oxtrust.model.CustomScriptDTO.toDTO;

/**
 * Service for custom script management
 *
 * @author Shoeb Khan
 * @version October 24, 2018
 */


@Path(OxTrustApiConstants.BASE_API_URL + "/configurations/scripts")
@ProtectedApi
public class CustomScriptManagementService {

    @Inject
    private Logger log;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private ApplianceService applianceService;

    @Inject
    private AbstractCustomScriptService customScriptService;

    private static final String INVALID_SCRIPT_TYPE_ERROR = buildInvalidScriptTypeError();

    private static final String INVALID_LOCATION_TYPE_ERROR = buildInvalidScriptLocationError();


    private static final String SERVER_DENIED_THE_REQUEST = "access_denied\n" + "Server denied the request.";

    /**
     * Returns all custom scripts by their types
     *
     * @returns HTTP Response
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Saves the script")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Script saved."),
            @ApiResponse(code = 400, message = "One or more of the input parameters are invalid."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Problem saving custom script")
    })
    public Response save(final CustomScriptDTO customScriptDTO) {

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
    @Path("/inum/{inum}")
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

        throw new ScriptNotFoundException(String.format("Failed to find script for inum:%s", inum));

    }

    /**
     * Returns the list of Custom Scripts that had errors
     *
     * @returns HTTP response
     */

    @GET
    @Path("/errors/all")
    @Produces({MediaType.APPLICATION_JSON})

    @ApiOperation(value = "Returns all the scripts having errors. ")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Found custom scripts with errors details."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Failed to load custom scripts")
    })
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
     * Returns the error details of script,if any, for given INUM
     *
     * @param inum
     * @return
     */
    @GET
    @Path("/errors/inum/{inum}")
    @Produces({MediaType.APPLICATION_JSON})

    @ApiOperation(value = "Returns the error details of script,if any, for given INUM ")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Found custom scripts with errors."),
            @ApiResponse(code = 403, message =  SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 500, message = "Script not found for inum.")
    })

    public Response GetScriptErrorByINum(@PathParam(value = "inum") final String inum) {

        final String baseDn = customScriptService.baseDn();

        final Optional<CustomScript> scriptOpt =
                customScriptService.getCustomScriptByINum(baseDn,inum, new String[]{"oxScriptError"});

        if (! scriptOpt.isPresent()) {
            throw new ScriptNotFoundException(String.format("Script not found for inum:%s", inum));
        }

        final CustomScript customScript =  scriptOpt.get();
        final ScriptError scriptError = customScript.getScriptError();

        if (scriptError == null) {
            return Response.ok().build();
        }

        return Response.ok(scriptError).build();
    }

    private void validate(final CustomScriptDTO currentScript) {

        try {

            validateScriptName(currentScript.getName());

            validateUsageType(currentScript);

            validatePL(currentScript);

            validateScriptType(currentScript);

            validateLocationType(currentScript);

        } catch (IllegalArgumentException ex) {

            log.error(ex.getMessage(), ex);
            throw new InvalidScriptDataException(ex.getMessage(), ex);

        }
    }

    private static void validateLocationType(CustomScriptDTO currentScript) {

        final String strLocationType = currentScript.getLocationType();

        if (strLocationType == null ) {
            throw new IllegalArgumentException("Location type is required.");
        }

        final ScriptLocationType locationType = ScriptLocationType.getByValue(currentScript.getLocationType());

        if (locationType == null) {
            throw new IllegalArgumentException(INVALID_LOCATION_TYPE_ERROR);
        }
    }

    private void validateUsageType(CustomScriptDTO currentScript) {
        final CustomScriptType csType = CustomScriptType.getByValue(currentScript.getScriptType());

        if (csType == CustomScriptType.PERSON_AUTHENTICATION
                && currentScript.getUsageType() == null) throw new InvalidScriptDataException("Usage type is not specified");
    }

    private void validatePL(final CustomScriptDTO scriptDTO) {

        if (scriptDTO.getProgrammingLanguage() == null) {
            throw new IllegalArgumentException("Script's programming language is required.");
        }

    }

    private void validateScriptType(final CustomScriptDTO scriptDTO) {

        final String strScriptType = scriptDTO.getScriptType();

        if (strScriptType == null ) {
            throw new IllegalArgumentException("Script type is required.");
        }

        final CustomScriptType csType = CustomScriptType.getByValue(strScriptType);

        if (csType == null) {

            throw new IllegalArgumentException(INVALID_SCRIPT_TYPE_ERROR);
        }

        if (isNotEmpty(scriptDTO.getDn())) {

            final CustomScript existingScript = customScriptService.getCustomScriptByDn(scriptDTO.getDn(), "oxScriptType");

            if (existingScript.getScriptType() != csType) {
                throw new IllegalArgumentException("Script's type should not be changed.");
            }

        }

    }

    private static String buildInvalidScriptTypeError() {

        final StringBuilder sb = new StringBuilder("Value for script type is invalid. Supported script types are: ");

        for (final CustomScriptType scriptType : CustomScriptType.values()) {
            sb.append(scriptType.getDisplayName()).append(", ");
        }

        removeLastComma(sb);

        return sb.toString();
    }

    private static String buildInvalidScriptLocationError() {

        final StringBuilder sb = new StringBuilder("Value for script location is invalid. Supported locations are: ");

        for (final ScriptLocationType locationType : ScriptLocationType.values()) {
            sb.append(locationType.getDisplayName()).append(", ");
        }

        removeLastComma(sb);

        return sb.toString();
    }


    private static void removeLastComma(StringBuilder sb) {

        final int commaIdx = sb.lastIndexOf(",");

        if (commaIdx > 0) {
            sb.deleteCharAt(commaIdx);
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
