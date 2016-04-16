/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema.core;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;

/**
 * Represents the User Core schema.
 *
 * @author Val Pecaoco
 */
public class UserCoreSchema extends SchemaType {

    public UserCoreSchema() {
        super();
    }

    public UserCoreSchema(String id, String name, String description) {
        super(id, name, description);
    }
}
