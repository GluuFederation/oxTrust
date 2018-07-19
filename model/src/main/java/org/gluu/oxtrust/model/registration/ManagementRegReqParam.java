/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.registration;

import org.apache.commons.lang.StringUtils;

/**
 * List of parameters required in {@link org.gluu.oxtrust.api.RegistrationManagementService} class.
 *
 * @author Shoeb Khan
 * @version 07/08/2018
 */

@SuppressWarnings("JavadocReference")
public enum ManagementRegReqParam {

    /**
     Whether captcha is disabled
     */
    CAPTCHA_DISABLED("captchaDisabled"),

    /**
     * Selected Attributes
     */
     SELECTED_ATTRIBUTES("selectedAttributes") ;

    /**
     * Parameter name
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name parameter name
     */
    ManagementRegReqParam(String name) {
        this.name = name;
    }

    /**
     * Gets parameter name.
     *
     * @return parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether parameter is standard
     *
     * @param p_parameterName parameter name
     * @return whether parameter is standard
     */
    public static boolean isStandard(String p_parameterName) {
        if (StringUtils.isNotBlank(p_parameterName)) {
            for (ManagementRegReqParam t : values()) {
                if (t.getName().equalsIgnoreCase(p_parameterName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether custom parameter is valid.
     *
     * @param p_parameterName parameter name
     * @return whether custom parameter is valid
     */
    public static boolean isCustomParameterValid(String p_parameterName) {
        return StringUtils.isNotBlank(p_parameterName) && !isStandard(p_parameterName);
    }


    @Override
    public String toString() {
        return name;
    }
}