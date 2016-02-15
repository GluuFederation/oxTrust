/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the mapping for the attribute characteristics.
 *
 * @author Val Pecaoco
 */
public class AttributeHolder implements Serializable {

    private String name;
    private String type;

    private String description;
    private Boolean required;
    private Boolean multiValued;

    private Boolean caseExact;
    private String mutability;
    private String returned;
    private String uniqueness;

    private List<AttributeHolder> subAttributes = new ArrayList<AttributeHolder>();
    private List<String> referenceTypes = new ArrayList<String>();
    // private List<String> canonicalValues = new ArrayList<String>();  // Optional

    public AttributeHolder() {
        this.caseExact = Boolean.FALSE;
        this.mutability = "readWrite";
        this.returned = "default";
        this.uniqueness = "none";
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        if (type == null) {
            return "";
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        if (required == null) {
            return Boolean.FALSE;
        }
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getMultiValued() {
        if (multiValued == null) {
            return Boolean.FALSE;
        }
        return multiValued;
    }

    public void setMultiValued(Boolean multiValued) {
        this.multiValued = multiValued;
    }

    public Boolean getCaseExact() {
        return caseExact;
    }

    public String getMutability() {
        return mutability;
    }

    public String getReturned() {
        return returned;
    }

    public String getUniqueness() {
        return uniqueness;
    }

    public List<AttributeHolder> getSubAttributes() {
        return subAttributes;
    }

    public void setSubAttributes(List<AttributeHolder> subAttributes) {
        this.subAttributes = subAttributes;
    }

    public List<String> getReferenceTypes() {
        return referenceTypes;
    }

    public void setReferenceTypes(List<String> referenceTypes) {
        this.referenceTypes = referenceTypes;
    }
}
