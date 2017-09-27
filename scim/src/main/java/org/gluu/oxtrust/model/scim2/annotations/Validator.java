package org.gluu.oxtrust.model.scim2.annotations;

import org.gluu.oxtrust.model.scim2.Validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jgomer on 2017-09-15.
 *
 * Annotation employed to associate a property of a SCIM resource with a concrete validation type that should be applied
 * on it. See enumeration {@link org.gluu.oxtrust.model.scim2.Validations}
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Validator {
    Validations value();
}
