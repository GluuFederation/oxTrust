/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.schema.core;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;

/**
 * Represents the Group Core schema.
 *
 * @author Val Pecaoco
 */
public class GroupCoreSchema extends SchemaType {

    public GroupCoreSchema() {
        super();
    }

    public GroupCoreSchema(String id, String name, String description) {
        super(id, name, description);
    }
}
