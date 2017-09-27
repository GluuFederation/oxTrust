package org.gluu.oxtrust.model.scim2.user;

import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.Validations;
import org.gluu.oxtrust.model.scim2.annotations.Validator;

/**
 * Created by jgomer on 2017-09-04.
 *
 * Phone number for the user.
 */
public class PhoneNumber {

    @Attribute(description = "Phone number of the User",
            isRequired = true)  //specs says false but it doesn't make sense
    @Validator(value = Validations.PHONE)
    private String value;

    @Attribute(description = "A human readable name, primarily used for  display purposes.")
    private String display;

    @Attribute(description = "A label indicating the attribute's  function; e.g., 'work' or 'home' or 'mobile' etc.",
            canonicalValues = { "work", "home", "mobile", "fax", "pager", "other" })
    private String type;

    @Attribute(description = "A Boolean value indicating the 'primary'  or preferred attribute value for this attribute, " +
            "e.g., the  preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no " +
            "more than once.")
    private boolean primary;

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

    public boolean getPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
