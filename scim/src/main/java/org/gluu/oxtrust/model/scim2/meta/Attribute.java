/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.meta;

import java.lang.reflect.Field;

import org.gluu.oxtrust.model.exception.SCIMException;

public class Attribute<O, T> {

    private final String name;
    private final Field field;
    private final Class<O> ownerClazz;  // NOSONAR : not finished yet
    private final Class<T> typeClazz;  // NOSONAR : not finished yet

    public Attribute(String name, Class<O> ownerClazz, Class<T> typeClazz) {
        this.name = name;
        this.ownerClazz = ownerClazz;// NOSONAR : not finished yet
        this.typeClazz = typeClazz;// NOSONAR : not finished yet

        try {
            this.field = ownerClazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new SCIMException("Cannot initialize meta model attribute " + name + " for class "
                    + ownerClazz.getSimpleName(), e);
        }
    }

    public String getName() {
        return name;
    }

    public Field getField() {
        return field;
    }
}