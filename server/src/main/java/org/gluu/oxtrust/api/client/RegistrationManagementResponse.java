/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.client;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;

import java.io.IOException;
import java.util.List;

/**
 * Wrapper for registration management service's response.
 *
 * @author Shoeb Khan
 * @version July 06, 2018
 */
public class RegistrationManagementResponse {


    private List<GluuAttribute> selectedAttributes;

    private List<GluuAttribute> attributes;

    private boolean captchaDisabled;

    private AppConfiguration oxTrustappConfiguration;

    public RegistrationManagementResponse() {
    }

    public RegistrationManagementResponse(List<GluuAttribute> selectedAttributes, List<GluuAttribute> attributes, boolean captchaDisabled, AppConfiguration oxTrustappConfiguration) {
        this.selectedAttributes = selectedAttributes;
        this.attributes = attributes;
        this.captchaDisabled = captchaDisabled;
        this.oxTrustappConfiguration = oxTrustappConfiguration;
    }

    public List<GluuAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<GluuAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<GluuAttribute> getSelectedAttributes() {
        return selectedAttributes;
    }

    public void setSelectedAttributes(List<GluuAttribute> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

    public boolean getCaptchaDisabled() {
        return captchaDisabled;
    }

    public void setCaptchaDisabled(boolean captchaDisabled) {
        this.captchaDisabled = captchaDisabled;
    }

    public AppConfiguration getOxTrustappConfiguration() {
        return oxTrustappConfiguration;
    }

    public void setOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
        this.oxTrustappConfiguration = oxTrustappConfiguration;
    }

    /**
     * Builds registration management response object from provided JSON string
     * @param responseJson
     * @return RegistrationManagementResponse
     */
    public static RegistrationManagementResponse fromJson(String responseJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        try {
           return mapper.readValue(responseJson, RegistrationManagementResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw  e;
        }
    }
}