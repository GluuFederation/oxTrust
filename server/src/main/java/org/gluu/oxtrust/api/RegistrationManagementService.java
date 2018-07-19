/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.codehaus.jettison.json.JSONException;
import org.gluu.oxtrust.api.client.RegistrationManagementRequest;
import org.gluu.oxtrust.api.client.RegistrationManagementResponse;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.util.StringHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for register REST web services.
 *
 * @author Shoeb Khan
 * @version July 06, 2018
 */
@Path(OxTrustApiConstants.BASE_API_URL + "/configurations/registration")
@ProtectedApi
public class RegistrationManagementService {

    public static final String SERVER_DENIED_THE_REQUEST = "Server denied the request.";
    public static final String MALFORMED_REQUEST = "The request is malformed.";
    @Inject
    private Logger log;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private AttributeService attributeService;

    @Inject
    private JsonConfigurationService jsonConfigurationService;


    /**
     * This operation retrieves the configuration info for the specified findAttributesForPattern criteria,
     * or all the configurations if no findAttributesForPattern criteria specified.
     *
     * @param searchPattern   Search pattern.
     * @param securityContext An injectable interface that provides access to security related information.
     * @return response
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "Reads registration configuration info.",
            notes = "Reads registration configuration info."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "access_denied\n" +
                    SERVER_DENIED_THE_REQUEST),
            @ApiResponse(code = 200, response = RegistrationManagementResponse.class, message = "Success")
    })
    public Response getConfiguration(@QueryParam(value = "searchPattern") String searchPattern, @HeaderParam("Authorization") String authorization, @Context HttpServletRequest httpRequest, @Context SecurityContext securityContext) {
        log.debug("Attempting to read configurations: searchPattern = {}", searchPattern);
        Response.ResponseBuilder builder = search(searchPattern);
        Response response = builder.build();
        return response;
    }


    /**
     * @param requestJson     request parameters
     * @param httpRequest     http request object
     * @param securityContext An injectable interface that provides access to security related information.
     * @return response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Saves configuration.",
            notes = "Saves configuration.",
            response = Response.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "invalid_request\n" +
                    MALFORMED_REQUEST),
            @ApiResponse(code = 403, message = "access_denied\n" +
                    SERVER_DENIED_THE_REQUEST)

    })
    public Response saveConfiguration(String requestJson, @HeaderParam("Authorization") String authorization, @Context HttpServletRequest httpRequest, @Context SecurityContext securityContext) {
        try {
            RegistrationManagementRequest request = RegistrationManagementRequest.fromJson(requestJson);
            final String captchaDisabled = request.getCaptchaDisabled();
            return save(Boolean.valueOf(captchaDisabled), request.getSelectedAttributes());
        } catch (JSONException jsonEx) {
            log.error("Error while parsing request", jsonEx);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception otherEx) {
            log.error(otherEx.getMessage(), otherEx);
            return Response.serverError().build();
        }
    }

    private Response save(boolean captchaDisabled, List<? extends GluuAttribute> selectedAttributes) {
        Response.ResponseBuilder builder = Response.ok();
        GluuOrganization org = organizationService.getOrganization();
        RegistrationConfiguration config = org.getOxRegistrationConfiguration();
        if (config == null) {
            config = new RegistrationConfiguration();
        }
        config.setCaptchaDisabled(captchaDisabled);
        List<String> attributeList = new ArrayList<String>();
        for (GluuAttribute attribute : selectedAttributes) {
            attributeList.add(attribute.getInum());
        }
        config.setAdditionalAttributes(attributeList);
        org.setOxRegistrationConfiguration(config);
        organizationService.updateOrganization(org);
        jsonConfigurationService.saveOxTrustappConfiguration(getOxTrustAppConfiguration());
        return builder.build();
    }


    private Response.ResponseBuilder search(String searchPattern) {
        final AppConfiguration oxTrustAppConfiguration = getOxTrustAppConfiguration();
        final List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
        final List<GluuAttribute> selectedAttributes = new ArrayList<GluuAttribute>();
        final GluuOrganization org = organizationService.getOrganization();
        final RegistrationConfiguration config = org.getOxRegistrationConfiguration();
        final RegistrationManagementResponse regResponse = new RegistrationManagementResponse();
        if (config != null) {
            regResponse.setCaptchaDisabled(config.isCaptchaDisabled());
            List<String> attributeList = config.getAdditionalAttributes();
            if (attributeList != null && !attributeList.isEmpty()) {
               // configureRegistrationForm = true; // Is this needed in REST service ?
                for (String attributeInum : attributeList) {
                    GluuAttribute attribute = attributeService.getAttributeByInum(attributeInum);
                    selectedAttributes.add(attribute);
                    attributes.add(attribute);
                }
            }
        }
        regResponse.setSelectedAttributes(selectedAttributes);
        regResponse.setOxTrustappConfiguration(oxTrustAppConfiguration);
        try {
            regResponse.setAttributes(findAttributesForPattern(selectedAttributes, searchPattern));

        } catch (Exception ex) {
            log.error("Failed to find attributes", ex);
            return Response.serverError();
        }
        return Response.ok(regResponse);
    }

    private AppConfiguration getOxTrustAppConfiguration() {
        return jsonConfigurationService.getOxTrustappConfiguration();
    }

    /*
    Finds attribute list for specified search pattern.
     */
    private List<GluuAttribute> findAttributesForPattern(final List<GluuAttribute> selectedAttributes, final String searchPattern) throws Exception {
        // We may not perform this comparison as rest is stateless.
        // After confirming with business analyst implement a work around
        // or delete the following block.
       /* if (StringHelper.isNotEmpty(this.oldSearchPattern) && Util.equals(this.oldSearchPattern, this.searchPattern)) {
            return OxTrustConstants.RESULT_SUCCESS;
        }*/
        final List<GluuAttribute> attributes;
        if (StringHelper.isEmpty(searchPattern)) {
            attributes = attributeService.getAllAttributes();
        } else {
            attributes = attributeService.searchAttributes(searchPattern, OxTrustConstants.searchPersonsSizeLimit);
        }
        for (GluuAttribute selectedAttribute : selectedAttributes) {
            if (!attributes.contains(selectedAttribute)) {
                attributes.add(selectedAttribute);
            }
        }
        return attributes;
    }


}