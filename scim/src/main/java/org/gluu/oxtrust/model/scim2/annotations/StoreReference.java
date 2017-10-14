package org.gluu.oxtrust.model.scim2.annotations;

import org.gluu.oxtrust.model.scim2.BaseScimResource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jgomer on 2017-10-10.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface StoreReference {
    String ref() default "";
    Class<? extends BaseScimResource>[] resourceType() default {};
    String[] refs() default {};
}
