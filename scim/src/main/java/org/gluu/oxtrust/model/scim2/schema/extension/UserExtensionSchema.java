/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema.extension;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;

/**
 * Represents the User Extension schema.
 *
 * @author Val Pecaoco
 */
public class UserExtensionSchema extends SchemaType {

    public UserExtensionSchema() {
        super();
    }

    public UserExtensionSchema(String id, String name, String description) {
        super(id, name, description);
    }
}
