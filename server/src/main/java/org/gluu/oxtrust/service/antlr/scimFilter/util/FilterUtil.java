/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

/**
 * @author Val Pecaoco
 */
public class FilterUtil {

    //TODO: fix this
    public static String stripScim2Schema(String uri) {
/*
        for (SchemaType schemaType : SchemaTypeMapping.getSchemaInstances()) {

            String schema = schemaType.getId() + ":";

            if (uri.startsWith(schema)) {

                int index = uri.indexOf(schema) + schema.length();
                uri = uri.substring(index);
                break;
            }
        }

        return uri;
        */
return null;
    }
}
