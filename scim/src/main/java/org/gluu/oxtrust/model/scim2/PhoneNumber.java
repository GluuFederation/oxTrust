/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a phoneNumber attribute.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class PhoneNumber extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public PhoneNumber() {
    }


    @Override
    public String getOperation() {
        return super.getOperation();
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public String getDisplay() {
        return super.getDisplay();
    }

    @Override
    public boolean isPrimary() {
        return super.isPrimary();
    }

    /**
     * Gets the type of the attribute.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return
     * 
     * @return the actual type
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
		this.type = type;
	}
    
    
    public void setOperation(String operation) {
        super.setOperation(operation);
    }

    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }

    
    public void setValue(String value) {
        super.setValue(value);
    }
    
    public void setPrimary(boolean primary) {
        super.setPrimary(primary);
    }


	@Override
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
        PhoneNumber other = (PhoneNumber) obj;
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
        return "PhoneNumber [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + "]";
    }

    

    /**
     * Represents a phone number type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type WORK = new Type("work");
        public static final Type HOME = new Type("home");
        public static final Type MOBILE = new Type("mobile");
        public static final Type FAX = new Type("fax");
        public static final Type PAGER = new Type("pager");
        public static final Type OTHER = new Type("other");

        public Type(String value) {
            super(value);
        }
    }
}
