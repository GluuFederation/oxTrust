/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.net.URI;
import java.net.URISyntaxException;

import org.gluu.oxtrust.model.data.ImageDataURI;
import org.gluu.oxtrust.model.data.PhotoValueType;
import org.gluu.oxtrust.model.exception.SCIMDataValidationException;

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
public class Photo extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    private Photo() {
    }

    private Photo(Builder builder) {
        super(builder);
        this.type = builder.type;
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
     * @return
     * 
     * @return the actual type
     */
    public Type getType() {
        return type;
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
     * Builder class that is used to build {@link Photo} instances
     */
    public static class Builder extends MultiValuedAttribute.Builder {

        private Type type;

        public Builder() {
        }

        /**
         * builds an Builder based of the given Attribute
         * 
         * @param photo
         *        existing Attribute
         */
        public Builder(Photo photo) {
            super(photo);
            type = photo.type;
        }

        @Override
        public Builder setOperation(String operation) {
            super.setOperation(operation);
            return this;
        }

        @Override
        public Builder setDisplay(String display) {
            super.setDisplay(display);
            return this;

        }

        /**
         * an URI pointing to an image
         * 
         * @param uri
         *        a image URI
         * @return the Builder itself
         */
        public Builder setValue(URI uri) {
            super.setValue(uri.toString());
            return this;
        }

        /**
         * an imageDataURI which contains a small in data image. For performance issues it is recommend to to store big
         * pictures as ImageDataURI
         * 
         * @param image
         *        a image
         * @return the Builder itself
         */
        public Builder setValue(ImageDataURI image) {
            super.setValue(image.toString());
            return this;
        }

        /**
         * Sets the label indicating the attribute's function (See {@link MultiValuedAttribute#getType()}).
         * 
         * @param type
         *        the type of the attribute
         * @return the builder itself
         */
        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public Builder setPrimary(boolean primary) {
            super.setPrimary(primary);
            return this;
        }

        @Override
        public Photo build() {
            return new Photo(this);
        }
    }

    /**
     * Represents a photo type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type PHOTO = new Type("photo");
        public static final Type THUMBNAIL = new Type("thumbnail");

        public Type(String value) {
            super(value);
        }
    }

}
