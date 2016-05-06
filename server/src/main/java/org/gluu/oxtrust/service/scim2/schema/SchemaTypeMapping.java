/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema;

import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.GroupCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.core.UserCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;

import java.util.*;

/**
 * Mapping definitions
 *
 * @author Val Pecaoco
 */
public class SchemaTypeMapping {

    private static Map<String, SchemaType> schemaInstances = new LinkedHashMap<String, SchemaType>();

    static {
        schemaInstances.put(Constants.GROUP_CORE_SCHEMA_ID, new GroupCoreSchema(Constants.GROUP_CORE_SCHEMA_ID, Constants.GROUP_CORE_SCHEMA_NAME, Constants.GROUP_CORE_SCHEMA_DESCRIPTION));
        schemaInstances.put(Constants.USER_CORE_SCHEMA_ID, new UserCoreSchema(Constants.USER_CORE_SCHEMA_ID, Constants.USER_CORE_SCHEMA_NAME, Constants.USER_CORE_SCHEMA_DESCRIPTION));
        schemaInstances.put(Constants.USER_EXT_SCHEMA_ID, new UserExtensionSchema(Constants.USER_EXT_SCHEMA_ID, Constants.USER_EXT_SCHEMA_NAME, Constants.USER_EXT_SCHEMA_DESCRIPTION));
    }

    public static SchemaType getSchemaTypeInstance(String id) throws Exception {
        return schemaInstances.get(id);
    }

    public static List<SchemaType> getSchemaInstances() {
        List<SchemaType> schemaInstancesAsList = new LinkedList<SchemaType>();
        for (Map.Entry<String, SchemaType> entry : schemaInstances.entrySet()) {
            schemaInstancesAsList.add(entry.getValue());
        }
        return schemaInstancesAsList;
    }
}
