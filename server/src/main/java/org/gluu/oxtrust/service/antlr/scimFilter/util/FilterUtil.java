/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;

/**
 * @author Val Pecaoco
 */
public class FilterUtil {

    public static String stripScimSchema(String uri) {

        for (SchemaType schemaType : SchemaTypeMapping.getSchemaInstances()) {

            String schema = schemaType.getId() + ":";

            if (uri.startsWith(schema)) {

                int index = uri.indexOf(schema) + schema.length();
                uri = uri.substring(index);
                break;
            }
        }

        return uri;
    }
}
