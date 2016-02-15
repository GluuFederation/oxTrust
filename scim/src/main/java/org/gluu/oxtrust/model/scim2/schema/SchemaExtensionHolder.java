/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema;

import java.io.Serializable;

/**
 * Holds the mapping of the schema extension characteristics for the resource type representation.
 *
 * @author Val Pecaoco
 */
public class SchemaExtensionHolder implements Serializable {

    private String schema;
    private Boolean required;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
