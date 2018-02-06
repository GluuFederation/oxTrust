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
import org.gluu.oxtrust.model.scim2.Validations;
import org.gluu.oxtrust.model.scim2.annotations.StoreReference;
import org.gluu.oxtrust.model.scim2.annotations.Validator;

/**
 * Represents an e-mail address for a user. See section 4.1.2 of RFC 7643.
 */
/*
 * Created by jgomer on 2017-09-04.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Email {

    public enum Type {WORK, HOME, OTHER}

    @Attribute(description = "E-mail addresses for the user. The value SHOULD be canonicalized by the Service Provider, " +
            "e.g.  bjensen@example.com instead of bjensen@EXAMPLE.COM.",
            isRequired = true)  //specs says false but it doesn't make sense
    @Validator(value = Validations.EMAIL)
    @StoreReference(ref="mail")    //Take advantage that oxTrustEmail's value is synced with mail attribute
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

    @JsonProperty
    public void setType(String type) {
        this.type = type;
    }

    public void setType(Type type){
        setType(type.name().toLowerCase());
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

}
