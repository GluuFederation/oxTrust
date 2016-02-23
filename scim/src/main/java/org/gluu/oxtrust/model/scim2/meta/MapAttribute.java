/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.meta;

public class MapAttribute<O, K, V> extends Attribute<O, V> {
    public MapAttribute(String name, Class<O> ownerClazz, Class<V> typeClazz) {
        super(name, ownerClazz, typeClazz);
    }
}