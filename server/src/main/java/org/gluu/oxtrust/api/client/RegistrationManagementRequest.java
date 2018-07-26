/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.client;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.gluu.oxtrust.model.registration.ManagementRegReqParam.CAPTCHA_DISABLED;
import static org.gluu.oxtrust.model.registration.ManagementRegReqParam.SELECTED_ATTRIBUTES;
import static org.xdi.oxauth.model.util.StringUtils.toJSONArray;

/**
 * Wrapper for registration management service's request.
 *
 * @author Shoeb Khan
 * @version July 06, 2018
 */
public class RegistrationManagementRequest  {


    private List<GluuAttribute> selectedAttributes;

    private Boolean captchaDisabled;

    @Inject
    private Logger log;


    public RegistrationManagementRequest() {

        selectedAttributes = new ArrayList<GluuAttribute>();
    }


    public String getQueryString() {
        String jsonQueryString = null;
        try {
            jsonQueryString = getJSONParameters().toString(4).replace("\\/", "/");
        } catch (JSONException e) {
            log.error("Error parsing query string.", e);
        }
        return jsonQueryString;
    }

    /**
     * Builds request object from json
     *
     * @param requestJson
     * @return RegistrationManagementRequest
     * @throws JSONException
     */
    public static RegistrationManagementRequest fromJson(String requestJson) throws IOException {
        RegistrationManagementRequest request;
        ObjectMapper mapper = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        request = mapper.readValue(requestJson, RegistrationManagementRequest.class);
        return request;

    }

    public JSONObject getJSONParameters() throws JSONException {
        JSONObject parameters = new JSONObject();
        if (selectedAttributes != null && !selectedAttributes.isEmpty()) {
            parameters.put(SELECTED_ATTRIBUTES.toString(), toJSONArray(selectedAttributes).toString());
        }
        if (captchaDisabled != null) {
            parameters.put(CAPTCHA_DISABLED.toString(), captchaDisabled);
        }
        return parameters;
    }

    public List<? extends GluuAttribute> getSelectedAttributes() {
        return selectedAttributes;
    }


    public Boolean getCaptchaDisabled() {
        return captchaDisabled;
    }

    public void setSelectedAttributes(List<GluuAttribute> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

    public void setCaptchaDisabled(Boolean captchaDisabled) {
        this.captchaDisabled = captchaDisabled;
    }

    public String toJsonString() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        return mapper.writeValueAsString(this);

    }


}