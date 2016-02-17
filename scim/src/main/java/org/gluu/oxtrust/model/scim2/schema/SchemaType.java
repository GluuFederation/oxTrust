/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of all schema type representations.
 *
 * @author Val Pecaoco
 */
public abstract class SchemaType implements Serializable {

    private String id;
    private String name;
    private String description;

    private MetaHolder meta;
    private List<AttributeHolder> attributes = new ArrayList<AttributeHolder>();

    protected SchemaType() {}

    protected SchemaType(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.meta = new MetaHolder(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public MetaHolder getMeta() {
        return meta;
    }

    public List<AttributeHolder> getAttributes() {
        return attributes;
    }

    public void setAttributeHolders(List<AttributeHolder> attributes) {
        this.attributes = attributes;
    }
}
