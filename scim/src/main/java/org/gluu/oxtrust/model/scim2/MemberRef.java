/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * This class represents a {@link User} or a {@link Group} which are members of an actual {@link Group}
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
 * </p>
 */
public class MemberRef extends MultiValuedAttribute { // NOSONAR - will be constructed by the builder or jackson

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public MemberRef() {
    }

    

    @Override
    public String getReference() {
        return super.getReference();
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
    
    public void setReference(String reference) {
        super.setReference(reference);
    }

    public void setValue(String value) {
        super.setValue(value);
    }
    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }
   
    public void setOperation(String operation) {
        super.setOperation(operation);
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
        MemberRef other = (MemberRef) obj;
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
        return "MemberRef [display=" + getDisplay() +", value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + ", ref=" + getReference() + "]";
    }

    

    /**
     * Represents an Member reference type.
     */
    public static class Type extends MultiValuedAttributeType {
        /**
         * indicates that the member is a {@link User}
         */
        public static final Type USER = new Type("User");
        /**
         * indicates that the member is a {@link Group}
         */
        public static final Type GROUP = new Type("Group");

        private Type(String value) {
            super(value);
        }
    }
}
