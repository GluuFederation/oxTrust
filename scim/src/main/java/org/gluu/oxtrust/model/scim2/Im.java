/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a Im attribute.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class Im extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    private Im() {
    }

    private Im(Builder builder) {
        super(builder);
        this.type = builder.type;
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
        Im other = (Im) obj;
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
        return "Im [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + "]";
    }

    /**
     * Builder class that is used to build {@link Im} instances
     */
    public static class Builder extends MultiValuedAttribute.Builder {

        private Type type;

        public Builder() {
        }

        /**
         * builds an Builder based of the given Attribute
         * 
         * @param im
         *        existing Attribute
         */
        public Builder(Im im) {
            super(im);
            type = im.type;
        }

        @Override
        public Builder setOperation(String operation) {
            super.setOperation(operation);
            return this;
        }

        @Override
        public Builder setDisplay(String display) {
            super.setDisplay(display);
            return this;

        }

        @Override
        public Builder setValue(String value) {
            super.setValue(value);
            return this;
        }

        /**
         * Sets the label indicating the attribute's function (See {@link MultiValuedAttribute#getType()}).
         * 
         * @param type
         *        the type of the attribute
         * @return the builder itself
         */
        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public Builder setPrimary(boolean primary) {
            super.setPrimary(primary);
            return this;
        }

        @Override
        public Im build() {
            return new Im(this);
        }
    }

    /**
     * Represents an IM type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type AIM = new Type("aim");
        public static final Type GTALK = new Type("gtalk");
        public static final Type ICQ = new Type("icq");
        public static final Type XMPP = new Type("xmpp");
        public static final Type MSN = new Type("msn");
        public static final Type SKYPE = new Type("skype");
        public static final Type QQ = new Type("qq");
        public static final Type YAHOO = new Type("yahoo");

        public Type(String value) {
            super(value);
        }
    }

}
