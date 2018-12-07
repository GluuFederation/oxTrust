/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */package org.gluu.oxtrust.api.customscripts;

import org.xdi.model.ScriptLocationType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom JavaBean validator for custom script's location
 *
 * @author Shoeb Khan
 */
public class ScriptLocValidator implements ConstraintValidator<ScriptLocation, String> {

    public void initialize(ScriptLocation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String strScriptLoc, ConstraintValidatorContext context) {

        if (strScriptLoc == null) {
            return false;
        }


        final ScriptLocationType locType = ScriptLocationType.getByValue(strScriptLoc.toLowerCase());

        if (locType == null) {
            return false;
        }

        return true;

    }
}


