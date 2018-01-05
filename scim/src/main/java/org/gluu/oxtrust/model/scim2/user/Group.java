/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.model.scim2.user;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.StoreReference;

/**
 * Represents a group to which a user belongs. See section 4.1.2 of RFC 7643.
 */
/*
 * Created by jgomer on 2017-09-12.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    public enum Type {DIRECT, INDIRECT}

    @Attribute(description = "The identifier of the User's group.",
            isRequired = true,  //Specs says the converse, but doesn't make sense
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    @StoreReference(ref = "memberOf")
    private String value;

    @Attribute(description = "The URI of the corresponding Group resource to which the user belongs",
            referenceTypes = { "User", "Group" },
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.REFERENCE)
    @JsonProperty("$ref")
    private String ref;

    @Attribute(description = "A human readable name, primarily used for display purposes.",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String display;

    @Attribute(description = "A label indicating the attribute's function; e.g., 'direct' or 'indirect'.",
            canonicalValues = { "direct", "indirect" },
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getType() {
        return type;
    }

    @JsonProperty
    public void setType(String type) {
        this.type = type;
    }

    public void setType(Type type){
        setType(type.name().toLowerCase());
    }

}
