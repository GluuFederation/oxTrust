package org.gluu.oxtrust.model.scim2.group;

import org.codehaus.jackson.annotate.JsonProperty;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;

/**
 * Created by jgomer on 2017-10-11.
 *
 * A member of a GroupResource.
 * From section 4.2 of RFC 7643: "Service providers MAY require clients to provide a non-empty value by setting the
 * "required" attribute characteristic of a sub-attribute of the "members" attribute in the "Group" resource schema."
 */
public class Member {

    @Attribute(description = "The value of an 'id' attribute of a SCIM resource",
            isRequired = true,  //specs says false but it doesn't make sense
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    private String value;

    @Attribute(description = "The URI of a SCIM resource such as a 'User', or a 'Group'",
            referenceTypes = { "User" },    //No support for "Group", hence no nested groups
            mutability = AttributeDefinition.Mutability.IMMUTABLE,
            type = AttributeDefinition.Type.REFERENCE)
    @JsonProperty("$ref")
    private String ref;

    @Attribute(description = "A label indicating the type of resource, e.g., 'User' or 'Group'.",
            canonicalValues = { "User" },    //No support for "Group", hence no nested groups
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    private String type;

    @Attribute(description = "A human readable name, primarily used for display purposes.",
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    private String display;

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
