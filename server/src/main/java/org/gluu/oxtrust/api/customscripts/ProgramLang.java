/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.customscripts;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * JavaBean annotation for programming language validation
 *
 * @author Shoeb
 */
@Target(value = {METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { ProgramLangValidator.class})
public @interface ProgramLang {

    String message() default "Value for programming language is invalid. Supported languages are: PYTHON, JAVASCRIPT";

    Class<? extends Payload>[] payload() default { };

    Class<?>[] groups() default {};

    String value() default "" ;

}

