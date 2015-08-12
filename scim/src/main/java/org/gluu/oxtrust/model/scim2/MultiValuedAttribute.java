/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.gluu.oxtrust.model.exception.SCIMDataValidationException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

/**
 * This class represents a multi valued attribute.
 * 
 * <p>
 * For more detailed information please look at the <a href=
 * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
@JsonInclude(Include.NON_EMPTY)
public abstract class MultiValuedAttribute {

    private String operation;
    private String value;
    private String display;
    private boolean primary;
    @JsonProperty("$ref")
    private String reference;

    /**
     * Default constructor for Jackson
     */
    protected MultiValuedAttribute() {
    }


    /**
     * Gets the attribute's significant value; e.g., the e-mail address, phone number etc.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return the value of the actual multi value attribute
     */
    protected String getValue() {
        return value;
    }

    /**
     * Gets the human readable name, primarily used for display purposes.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return the display attribute
     */
    protected String getDisplay() {
        return display;
    }

    /**
     * Gets a Boolean value indicating the 'primary' or preferred attribute value for this attribute.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return the primary attribute
     */
    protected boolean isPrimary() {
        return primary;
    }

    /**
     * Gets the operation applied during a PATCH request.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return the operation
     */
    protected String getOperation() {
        return operation;
    }

    /**
     * Gets the reference to the actual SCIM Resource.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
     * </p>
     * 
     * @return the reference of the actual resource
     */
    protected String getReference() {
        return reference;
    }

    public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setValue(String value) {
		if(Strings.isNullOrEmpty(value)){
            throw new SCIMDataValidationException("The given value can't be null or empty.");
        }
        this.value = value;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + (primary ? 1231 : 1237);
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        MultiValuedAttribute other = (MultiValuedAttribute) obj;
        if (display == null) {
            if (other.display != null) {
                return false;
            }
        } else if (!display.equals(other.display)) {
            return false;
        }
        if (operation == null) {
            if (other.operation != null) {
                return false;
            }
        } else if (!operation.equals(other.operation)) {
            return false;
        }
        if (primary != other.primary) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
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
        return "MultiValuedAttribute [operation=" + operation + ", value=" + value + ", display=" + display
                + ", primary=" + primary + ", reference=" + reference + "]";
    }
}
