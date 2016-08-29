/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.schema.core.fido;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;

/**
 * @author Val Pecaoco
 */
public class FidoDeviceCoreSchema extends SchemaType {

    public FidoDeviceCoreSchema() {
        super();
    }

    public FidoDeviceCoreSchema(String id, String name, String description) {
        super(id, name, description);
    }
}
