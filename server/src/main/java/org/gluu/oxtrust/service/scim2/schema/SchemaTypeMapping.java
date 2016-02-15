/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping definitions
 *
 * @author Val Pecaoco
 */
public class SchemaTypeMapping {

    public static final String CORE_ID = "urn:ietf:params:scim:schemas:core:2.0:User";
    public static final String CORE_NAME = "User";
    public static final String CORE_DESCRIPTION = "User Account";

    public static final String EXT_ID = "urn:ietf:params:scim:schemas:extension:gluu:2.0:User";
    public static final String EXT_NAME = "GluuCustomExtension";
    public static final String EXT_DESCRIPTION = "Gluu Custom Extension";

    private static Map<String, SchemaType> schemaInstances = new HashMap<String, SchemaType>();

    static {
        // schemaInstances.put(CORE_ID, new UserCoreSchema(CORE_ID, CORE_NAME, CORE_DESCRIPTION));
        schemaInstances.put(EXT_ID, new UserExtensionSchema(EXT_ID, EXT_NAME, EXT_DESCRIPTION));
    }

    public static SchemaType getSchemaTypeInstance(String id) throws Exception {
        return schemaInstances.get(id);
    }

    public static List<SchemaType> getSchemaInstances() {
        List<SchemaType> schemaInstancesAsList = new ArrayList<SchemaType>();
        for (Map.Entry<String, SchemaType> entry : schemaInstances.entrySet()) {
            schemaInstancesAsList.add(entry.getValue());
        }
        return schemaInstancesAsList;
    }
}
