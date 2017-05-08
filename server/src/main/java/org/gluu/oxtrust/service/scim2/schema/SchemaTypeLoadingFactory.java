/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.GroupCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.core.UserCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.core.fido.FidoDeviceCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.scim2.schema.strategy.*;
import org.xdi.config.oxtrust.AppConfiguration;

/**
 * Factory for loading a SchemaType.
 *
 * @author Val Pecaoco
 * @link SchemaType
 */
public class SchemaTypeLoadingFactory {

    public SchemaType load(AppConfiguration appConfiguration, String id) throws Exception {

        SchemaType schemaType = SchemaTypeMapping.getSchemaTypeInstance(id);

        if (schemaType == null) {
            return null;
        }

        return load(appConfiguration, schemaType);
    }

    public SchemaType load(AppConfiguration appConfiguration, SchemaType schemaType) throws Exception {

        LoadingStrategy loadingStrategy = null;

        if (schemaType instanceof UserCoreSchema) {
            loadingStrategy = new UserCoreLoadingStrategy();
        } else if (schemaType instanceof UserExtensionSchema) {
            loadingStrategy = new UserExtensionLoadingStrategy();
        } else if (schemaType instanceof GroupCoreSchema) {
            loadingStrategy = new GroupCoreLoadingStrategy();
        } else if (schemaType instanceof FidoDeviceCoreSchema) {
            loadingStrategy = new FidoDeviceCoreLoadingStrategy();
        }

        if (loadingStrategy == null) {
            return null;
        }

        return loadingStrategy.load(appConfiguration, schemaType);
    }
}
