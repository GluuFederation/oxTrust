package org.gluu.oxtrust.model.scim2.user;

import org.codehaus.jackson.annotate.JsonProperty;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;

/**
 * Created by jgomer on 2017-09-12.
 *
 * Group membership for the user.
 */
public class Group {

    @Attribute(description = "The identifier of the User's group.",
            isRequired = true,  //Specs says the converse, but doesn't make sense
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String value;

    @Attribute(description = "The URI of the corresponding Group resource to which the user belongs",
            referenceTypes = { "User", "Group" },
            mutability = AttributeDefinition.Mutability.READ_ONLY)
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

    public void setType(String type) {
        this.type = type;
    }
}
