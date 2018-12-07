/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.customscripts;

import org.xdi.model.custom.script.CustomScriptType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom JavaBean validator for custom script's type
 *
 * @author Shoeb
 **/
public class ScriptTypeValidator implements ConstraintValidator<ScriptType, String> {

    public void initialize(ScriptType constraintAnnotation) {
    }

    @Override
    public boolean isValid(String strScriptType, ConstraintValidatorContext context) {

        if (strScriptType == null) {
            return false;
        }

        final CustomScriptType csType = CustomScriptType.getByValue(strScriptType.toLowerCase());

        if (csType == null) {
            return false;
        }

        return true;

    }
}


