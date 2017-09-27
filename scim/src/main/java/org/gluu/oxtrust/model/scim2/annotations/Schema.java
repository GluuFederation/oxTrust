package org.gluu.oxtrust.model.scim2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jgomer on 2017-09-04.
 *
 * Annotation used to indicate the default schema of a SCIM resource
 * Based on https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/annotations/Schema.java
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Schema{
    /**
     * The id for the object.  This is the complete URN.
     *
     * @return The object's id as a URN.
     */
    String id();

    /**
     * The description for the object.
     *
     * @return The object's description.
     */
    String description();

    /**
     * The name for the object.  This is a human readable
     * name.
     *
     * @return The object's human-readable name.
     */
    String name();

}
