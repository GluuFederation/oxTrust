/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonValue;
import org.gluu.oxtrust.model.data.ImageDataURI;
import org.gluu.oxtrust.model.data.PhotoValueType;
import org.gluu.oxtrust.model.exception.SCIMDataValidationException;
import org.gluu.oxtrust.model.scim2.Email.Type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a photo attribute.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
@JsonPropertyOrder({"operation", "value", "display", "primary", "type", "valueAsImageDataURI", "valueType", "valueAsURI"})
public class Photo extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public Photo() {
    }


    @Override
    public String getOperation() {
        return super.getOperation();
    }

    /**
     * the value of the photo as URI. Check first with {@link Photo#getValueType()} if the type is
     * {@link PhotoValueType#URI}
     * 
     * @return returns the value of the photo as URI
     */
    public URI getValueAsURI() {
        URI uri = null;
        try {
            uri = new URI(super.getValue());
        } catch (URISyntaxException e) {
            throw new SCIMDataValidationException(e.getMessage(), e);
        }
        return uri;
    }

    /**
     * the value of the photo as {@link ImageDataURI}. Check first with {@link Photo#getValueType()} if the type is
     * {@link PhotoValueType#IMAGE_DATA_URI}
     * 
     * @return the value of the photo as {@link ImageDataURI}
     */
    @JsonIgnore
    public ImageDataURI getValueAsImageDataURI() {
        return new ImageDataURI(super.getValue());
    }

    /**
     * 
     * @return the type of the saved photo value
     */
    @JsonIgnore
    public PhotoValueType getValueType() {
        if (super.getValue().startsWith("data:image/") && super.getValue().contains(";base64,")) {
            try {
                getValueAsImageDataURI();
                return PhotoValueType.IMAGE_DATA_URI;
            } catch (Exception e) {
            }
        }
        try {
            getValueAsURI();
            return PhotoValueType.URI;
        } catch (Exception e) {
        }
        return PhotoValueType.UNKNOWN;
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
     * @return the actual type
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
		this.type = type;
	}
    
    
    public void setOperation(String operation) {
        super.setOperation(operation);
    }

    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    public void setValue(URI uri) {
        super.setValue(uri.toString());
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
        Photo other = (Photo) obj;
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
        return "Photo [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary()
                + ", operation=" + getOperation() + "]";
    }

    

    /**
     * Represents a photo type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type PHOTO = new Type("photo");
        public static final Type THUMBNAIL = new Type("thumbnail");

        private static Map<String, Type> namesMap = new HashMap<String, Type>(3);

        static {
            namesMap.put(PHOTO.getValue(), PHOTO);
            namesMap.put(THUMBNAIL.getValue(), THUMBNAIL);
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
