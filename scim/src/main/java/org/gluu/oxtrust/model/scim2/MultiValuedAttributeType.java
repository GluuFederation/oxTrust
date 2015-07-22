/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;

public abstract class MultiValuedAttributeType {

    private final String value;

    protected MultiValuedAttributeType(String value) {
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(String.format("The value of %s can't be null or empty.", getClass().getName()));
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MultiValuedAttributeType other = (MultiValuedAttributeType) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value;
    }

}
