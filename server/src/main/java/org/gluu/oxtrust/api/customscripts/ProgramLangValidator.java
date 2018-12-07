/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.customscripts;

import org.xdi.model.ProgrammingLanguage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Custom JavaBean validator for script's programming language
 *
 * @author Shoeb
 */
public class ProgramLangValidator implements ConstraintValidator<ProgramLang, String> {

    public void initialize(ProgramLang constraintAnnotation) {
    }

    @Override
    public boolean isValid(String strProgramLang, ConstraintValidatorContext context) {

        if (strProgramLang == null) {
            return false;
        }

        final ProgrammingLanguage programLang = ProgrammingLanguage.getByValue(strProgramLang.toLowerCase());

        if (programLang == null) {
            return false;
        }

        return true;

    }
}


