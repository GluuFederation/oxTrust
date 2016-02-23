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
import org.gluu.oxtrust.model.scim2.GroupRef.Type;

/**
 * This class represents a role attribute.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class Role extends MultiValuedAttribute {

    private Type type;

    /**
     * Default constructor for Jackson
     */
    public Role() {
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
    public String toString() {
        return "Role [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + "]";
    }

    

    /**
     * Represents an Role type.
     */
    public static class Type extends MultiValuedAttributeType {

        public Type(String value) {
            super(value);
        }

        @JsonValue
        public String getValue() {
            return super.getValue();
        }
    }

}
