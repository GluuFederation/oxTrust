/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema;

import org.gluu.oxtrust.model.scim2.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of all schema type representations.
 *
 * @author Val Pecaoco
 */
public abstract class SchemaType extends Resource {

    private String name;
    private String description;

    private List<AttributeHolder> attributes = new ArrayList<AttributeHolder>();

    protected SchemaType() {
        super();
    }

    protected SchemaType(String id, String name, String description) {
        super();
        super.setId(id);
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return super.getId();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<AttributeHolder> getAttributes() {
        return attributes;
    }

    public void setAttributeHolders(List<AttributeHolder> attributes) {
        this.attributes = attributes;
    }
}
