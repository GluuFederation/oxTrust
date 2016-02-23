/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.meta;

public class SetAttribute<O, T> extends Attribute<O, T> {
    public SetAttribute(String name, Class<O> ownerClazz, Class<T> typeClazz) {
        super(name, ownerClazz, typeClazz);
    }
}