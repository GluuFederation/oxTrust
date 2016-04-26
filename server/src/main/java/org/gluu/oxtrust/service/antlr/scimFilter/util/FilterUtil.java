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

    public static String stripScimSchema(String schema, String uri) {

        String path = schema + ":";
        if (uri.startsWith(path)) {
            int index = uri.indexOf(path) + path.length();
            uri = uri.substring(index);
        }

        return uri;
    }
}
