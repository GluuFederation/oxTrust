/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.gluu.oxtrust.model.exception.SCIMDataValidationException;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a email attribute.
 *
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class Email extends MultiValuedAttribute {

	public static final Pattern VALIDATION_PATTERN = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
            "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
	
    @JsonProperty
    private Type type;

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

    @Override
    public String getReference() {
        return super.getReference();
    }

    /**
     * Gets the reference to the actual SCIM Resource.
     * <p>
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
     * </p>
     *
     * @return the reference of the actual resource
     */

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
    
    public void setOperation(String operation) {
        super.setOperation(operation);
    }
    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }

    /**
     * Sets the email value.
     *
     * @param value
     *        the email attribute
     * @throws SCIMDataValidationException in case the value is not a well-formed email
     */

    public void setValue(String value) {
        Matcher matcher = VALIDATION_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new SCIMDataValidationException("The value '" + value + "' is not a well-formed email.");
        }
        super.setValue(value);
    }

    /**
     * Sets the label indicating the attribute's function
     *
     * @param type
     *        the type of the attribute
     */
    public void setType(Type type) {
        this.type = type;
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
        Email other = (Email) obj;
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
        return "Email [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary()
                + ", operation=" + getOperation() + "]";
    }

    

    /**
     * Represents an email type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type WORK = new Type("work");
        public static final Type HOME = new Type("home");
        public static final Type OTHER = new Type("other");

        private static Map<String, Type> namesMap = new HashMap<String, Type>(3);

        static {
            namesMap.put(WORK.getValue(), WORK);
            namesMap.put(HOME.getValue(), HOME);
            namesMap.put(OTHER.getValue(), OTHER);
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
