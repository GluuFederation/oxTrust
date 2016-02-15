/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema;

import java.io.Serializable;

/**
 * Holds the mapping for the meta characteristics.
 *
 * @author Val Pecaoco
 */
public class MetaHolder implements Serializable {

    private static final String URI_PREFIX = "/v2/Schemas/";

    private String resourceType;
    private String location;

    public MetaHolder() {
        this.resourceType = "Schemas";
    }

    public MetaHolder(String id) {
        this();
        this.location = URI_PREFIX + id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getLocation() {
        return location;
    }
}
