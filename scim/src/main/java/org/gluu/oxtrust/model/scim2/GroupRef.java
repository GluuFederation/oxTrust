/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.gluu.oxtrust.model.scim2.Photo.Type;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a Reference of a Group
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class GroupRef extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public GroupRef() {
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public String getDisplay() {
        return super.getDisplay();
    }

    /**
     * Gets the type of the attribute.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return the actual type
     */
    public Type getType() {
        return type;
    }

    @Override
    public String getReference() {
        return super.getReference();
    }

    @Override
    public String getOperation() {
        return super.getOperation();
    }

    public void setType(Type type) {
		this.type = type;
	}
    
    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }

    
    public void setValue(String value) {
        super.setValue(value);
    }
    
    public void setReference(String reference) {
        super.setReference(reference);
    }

	
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GroupRef other = (GroupRef) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GroupRef [display=" + getDisplay() +", value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + ", ref=" + getReference() + "]";
    }

    
    /**
     * Represents an Group reference type.
     */
    public static class Type extends MultiValuedAttributeType {
        /**
         * The User is direct in the actual Group
         */
        public static final Type DIRECT = new Type("direct");
        /**
         * The User is not direct in the actual Group but in a Group that is in the actual Group
         */
        public static final Type INDIRECT = new Type("indirect");

        private static Map<String, Type> namesMap = new HashMap<String, Type>(3);

        static {
            namesMap.put(DIRECT.getValue(), DIRECT);
            namesMap.put(INDIRECT.getValue(), INDIRECT);
        }

        public Type(String value) {
            super(value);
        }

        @JsonCreator
        public static MultiValuedAttributeType forValue(String value) {
            return namesMap.get(StringUtils.lowerCase(value));
        }

        @JsonValue
        public String getValue() {
            return super.getValue();
        }
    }
}