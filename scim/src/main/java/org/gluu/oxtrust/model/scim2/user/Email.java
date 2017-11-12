package org.gluu.oxtrust.model.scim2.user;

import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.Validations;
import org.gluu.oxtrust.model.scim2.annotations.Validator;

/**
 * Created by jgomer on 2017-09-04.
 *
 * email address for the user.
 */
public class Email {

    @Attribute(description = "E-mail addresses for the user. The value SHOULD be canonicalized by the Service Provider, " +
            "e.g.  bjensen@example.com instead of bjensen@EXAMPLE.COM.",
            isRequired = true)  //specs says false but it doesn't make sense
    @Validator(value = Validations.EMAIL)
    private String value;

    @Attribute(description = "A human readable name, primarily used for  display purposes.")
    private String display;

    @Attribute(description = "A label indicating the attribute's  function; e.g., 'work' or 'home'.",
            canonicalValues = { "work", "home", "other" })
    private String type;

    @Attribute(description = "A Boolean value indicating the 'primary'  or preferred attribute value for this attribute," +
                "e.g., the  preferred mailing address or primary e-mail address. The primary  attribute value 'true' MUST " +
                "appear no more than once.",
            type = AttributeDefinition.Type.BOOLEAN)
    private Boolean primary;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }
}
