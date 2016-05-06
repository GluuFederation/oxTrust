/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.GroupCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.core.UserCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.scim2.schema.strategy.GroupCoreLoadingStrategy;
import org.gluu.oxtrust.service.scim2.schema.strategy.LoadingStrategy;
import org.gluu.oxtrust.service.scim2.schema.strategy.UserCoreLoadingStrategy;
import org.gluu.oxtrust.service.scim2.schema.strategy.UserExtensionLoadingStrategy;

/**
 * Factory for loading a SchemaType.
 *
 * @author Val Pecaoco
 * @link SchemaType
 */
public class SchemaTypeLoadingFactory {

    public SchemaType load(String id) throws Exception {

        SchemaType schemaType = SchemaTypeMapping.getSchemaTypeInstance(id);

        if (schemaType == null) {
            return null;
        }

        return load(schemaType);
    }

    public SchemaType load(SchemaType schemaType) throws Exception {

        LoadingStrategy loadingStrategy = null;

        if (schemaType instanceof UserCoreSchema) {
            loadingStrategy = new UserCoreLoadingStrategy();
        } else if (schemaType instanceof UserExtensionSchema) {
            loadingStrategy = new UserExtensionLoadingStrategy();
        } else if (schemaType instanceof GroupCoreSchema) {
            loadingStrategy = new GroupCoreLoadingStrategy();
        }

        if (loadingStrategy == null) {
            return null;
        }

        return loadingStrategy.load(schemaType);
    }
}
