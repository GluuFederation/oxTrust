package org.gluu.oxtrust.api.client;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gluu.oxtrust.model.registration.ManagementRegReqParam;
import org.xdi.model.GluuAttribute;
import org.xdi.oxauth.client.BaseRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.gluu.oxtrust.model.registration.ManagementRegReqParam.CAPTCHA_DISABLED;
import static org.gluu.oxtrust.model.registration.ManagementRegReqParam.SELECTED_ATTRIBUTES;
import static org.xdi.oxauth.model.util.StringUtils.toJSONArray;

/**
 * Wrapper for registration management service's request.
 *
 * @author Shoeb Khan
 * @version July 06, 2018
 */
public class RegistrationManagementRequest extends BaseRequest {


    private List<GluuAttribute> selectedAttributes;

    private String captchaDisabled;

    private RegistrationManagementRequest() {

        selectedAttributes = new ArrayList<GluuAttribute>();
    }

    @Override
    public String getQueryString() {
        String jsonQueryString = null;
        try {
            jsonQueryString = getJSONParameters().toString(4).replace("\\/", "/");
        } catch (JSONException e) {
            e.printStackTrace();
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
    public static RegistrationManagementRequest fromJson(String requestJson) throws JSONException {
        final JSONObject requestObject = new JSONObject(requestJson);
        final List<GluuAttribute> selectedAttributes = new ArrayList<GluuAttribute>();
        final RegistrationManagementRequest request = new RegistrationManagementRequest();
        if (requestObject.has(SELECTED_ATTRIBUTES.toString())) {
            JSONArray attributesJsonArray = requestObject.getJSONArray(SELECTED_ATTRIBUTES.toString());
            for (int i = 0; i < attributesJsonArray.length(); i++) {
                JSONObject attObj = attributesJsonArray.getJSONObject(i);
                GluuAttribute attribute = null;
                try {
                    ObjectMapper mapper = new ObjectMapper()
                            .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    attribute = mapper.readValue(attObj.toString(), GluuAttribute.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                selectedAttributes.add(attribute);
            }
        }
        //
        request.selectedAttributes = selectedAttributes;
        //
        if (requestObject.has(CAPTCHA_DISABLED.toString())) {
            request.captchaDisabled = requestObject.getString(ManagementRegReqParam.CAPTCHA_DISABLED.toString());
        }
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


    public String getCaptchaDisabled() {
        return captchaDisabled;
    }



}