/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.math.BigDecimal;

import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class represents a schema extension.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-4">SCIM core schema 2.0, section 4</a>
 * </p>
 */
public class Extension {

    @JsonIgnore
    private String urn;

    private Map<String, Field> fields = new HashMap<String, Field>();

    /**
     * Default constructor for Jackson
     */
    protected Extension() {
    }

    private Extension(Builder builder) {
        this.urn = builder.urn;
        this.fields = builder.fields;
    }

    /**
     * Returns the URN of this extension.
     * 
     * @return The URN
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Return the value for the field with a given name and type.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @param extensionFieldType
     *        The type of the field.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    @JsonAnyGetter
    public <T> T getField(String field, ExtensionFieldType<T> extensionFieldType) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Invalid field name");
        }
        if (extensionFieldType == null) {
            throw new IllegalArgumentException("Invalid field type");
        }

        if (!isFieldPresent(field)) {
            throw new NoSuchElementException("Field " + field + " not valid in this extension");
        }

        return extensionFieldType.fromString(fields.get(field).value);
    }

    /**
     * Return the value for the field with a given name as String.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    public String getFieldAsString(String field) {
        return getField(field, ExtensionFieldType.STRING);
    }

    /*
     * Return the value for the field with a given name as boolean.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    /*
    public boolean getFieldAsBoolean(String field) {
        return getField(field, ExtensionFieldType.BOOLEAN);
    }
    */

    /*
     * Return the value for the field with a given name as ByteBuffer.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    /*
    public ByteBuffer getFieldAsByteBuffer(String field) {
        return getField(field, ExtensionFieldType.BINARY);
    }
    */

    /**
     * Return the value for the field with a given name as Date.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    public Date getFieldAsDate(String field) {
        return getField(field, ExtensionFieldType.DATE_TIME);
    }

    /**
     * Return the value for the field with a given name as BigDecimal.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    public BigDecimal getFieldAsDecimal(String field) {
        return getField(field, ExtensionFieldType.DECIMAL);
    }

    /*
     * Return the value for the field with a given name as BigInteger.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    /*
    public BigInteger getFieldAsInteger(String field) {
        return getField(field, ExtensionFieldType.INTEGER);
    }
    */

    /*
     * Return the value for the field with a given name as URI.
     * 
     * @param field
     *        The name of the field to retrieve the value of.
     * @return The value for the field with the given name.
     * @throws NoSuchElementException
     *         if this schema does not contain a field of the given name.
     * @throws IllegalArgumentException
     *         if the given field is null or an empty string or if the extensionFieldType is null.
     */
    /*
    public URI getFieldAsReference(String field) {
        return getField(field, ExtensionFieldType.REFERENCE);
    }
    */

    /**
     * Provides a {@link Map} containing the entries of the extension. Note that the returned {@link Map} is immutable.
     * 
     * @return The Entries of this schema as an map.
     */
    // @JsonIgnore
    public Map<String, Field> getFields() {
        return ImmutableMap.copyOf(fields);
    }

    /**
     * Checks if the given field is present in this extension because not every field is mandatory (according to scim2
     * 2.0 spec).
     * 
     * @param field
     *        Name of the field to check
     * @return true if the given field is present, else false
     */
    public boolean isFieldPresent(String field) {
        return fields.containsKey(field);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((urn == null) ? 0 : urn.hashCode());
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
        Extension other = (Extension) obj;
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        if (urn == null) {
            if (other.urn != null) {
                return false;
            }
        } else if (!urn.equals(other.urn)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringPresentation = new StringBuilder();
        stringPresentation.append("Extension [urn=").append(urn).append("{");
        boolean firstEntry = true;
        for (String key : fields.keySet()) {
            Field field = fields.get(key);
            if (!firstEntry) {
                stringPresentation.append(", ");
            }
            firstEntry = false;
            stringPresentation.append("field=").append(key).append("(")
                    .append("value=").append(field.getValue()).append(", type=")
                    .append(field.getType()).append(")");
        }
        stringPresentation.append("}]");
        return stringPresentation.toString();
    }

    /**
     * Builder class that is used to build {@link Extension} instances
     */
    public static class Builder {

        private String urn;

        private Map<String, Field> fields = new HashMap<String, Field>();

        /**
         * Constructs an extension with the given urn.
         * 
         * @param urn
         *        the urn of the extension
         */
        public Builder(String urn) {
            this.urn = urn;
        }

        /**
         * Constructs an extension based on the given extension.
         * 
         * @param extension
         *        existing extension
         */
        public Builder(Extension extension) {
            this.urn = extension.urn;
            this.fields = extension.fields;
        }

        /**
         * Sets the field specified by the given field name with the given value. <br>
         * Can only be set and saved if extension field is registered in the database
         * 
         * @param fieldName
         *        the field name
         * @param value
         *        the new value
         * @return the builder itself
         */
        @JsonAnySetter
        public Builder setField(String fieldName, String value) {
            setField(fieldName, value, ExtensionFieldType.STRING, false);
            return this;
        }

        /*
        @JsonAnySetter
        public Builder setField(String fieldName, Boolean value) {
            setField(fieldName, value, ExtensionFieldType.BOOLEAN, false);
            return this;
        }
        */

        /*
        @JsonAnySetter
        public Builder setField(String fieldName, ByteBuffer value) {
            setField(fieldName, value, ExtensionFieldType.BINARY, false);
            return this;
        }
        */

        /*
        @JsonAnySetter
        public Builder setField(String fieldName, BigInteger value) {
            setField(fieldName, value, ExtensionFieldType.INTEGER, false);
            return this;
        }
        */

        /**
         * Sets the field specified by the given field name with the given value. <br>
         * Can only be set and saved if extension field is registered in the database
         * 
         * @param fieldName
         *        the field name
         * @param value
         *        the new value
         * @return the builder itself
         */
        @JsonAnySetter
        public Builder setField(String fieldName, BigDecimal value) {
            setField(fieldName, value, ExtensionFieldType.DECIMAL, false);
            return this;
        }

        /**
         * Sets the field specified by the given field name with the given value. <br>
         * Can only be set and saved if extension field is registered in the database
         * 
         * @param fieldName
         *        the field name
         * @param value
         *        the new value
         * @return the builder itself
         */
        @JsonAnySetter
        public Builder setField(String fieldName, Date value) {
            setField(fieldName, value, ExtensionFieldType.DATE_TIME, false);
            return this;
        }

        /*
        @JsonAnySetter
        public Builder setField(String fieldName, URI value) {
            setField(fieldName, value, ExtensionFieldType.REFERENCE, false);
            return this;
        }
        */

        @JsonAnySetter
        public Builder setFieldAsList(String fieldName, List<?> values) throws Exception {

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

            Date[] dateArray = null;
            boolean isDateArray = false;
            try {
                dateArray = (Date[])values.toArray();
                isDateArray = true;
            } catch (Exception e) {
            }

            if (isDateArray) {

                DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
                List<String> stringDateList = new ArrayList<String>();
                for (Date date : dateArray) {
                    stringDateList.add(dateTimeFormatter.print(date.getTime()));
                }

                setField(fieldName, mapper.writeValueAsString(stringDateList), ExtensionFieldType.STRING, true);
                return this;

            } else {

                setField(fieldName, mapper.writeValueAsString(values), ExtensionFieldType.STRING, true);
                return this;
            }
        }

        /**
         * Sets the field specified by the given field name with the given value of the given type. <br>
         * Can only be set and saved if extension field is registered in the database
         * 
         * @param fieldName
         *        the field name
         * @param value
         *        the new value
         * @param type
         *        the scim2 type of the field
         * @return the builder itself
         */
        @JsonAnySetter
        public <T> Builder setField(String fieldName, T value, ExtensionFieldType<T> type, boolean isMultiValued) {
            if (fieldName == null || fieldName.isEmpty()) {
                throw new IllegalArgumentException("The field name can't be null or empty.");
            }
            if (value == null) {
                throw new IllegalArgumentException("The value can't be null.");
            }
            if (type == null) {
                throw new IllegalArgumentException("The type can't be null.");
            }
            fields.put(fieldName, new Field(type, type.toString(value), isMultiValued));
            return this;
        }

        /**
         * removes one field and its value
         * 
         * @param fieldName
         *        the field to be removed
         * @return the builder itself
         */
        public Builder removeField(String fieldName) {
            fields.remove(fieldName);
            return this;
        }

        public Extension build() {
            return new Extension(this);
        }
    }

    /**
     * This class represents a field of an extension with its type and value. Instances of this class are immutable.
     */
    public static final class Field {

        private final ExtensionFieldType<?> type;
        private final String value;
        private final boolean isMultiValued;

        /**
         * Constructs a new {@link Field} with the given type and value.
         * 
         * @param type
         *        the type of the field
         * @param value
         *        the value of the field
         */
        public Field(ExtensionFieldType<?> type, String value, boolean isMultiValued) {
            this.type = type;
            this.value = value;
            this.isMultiValued = isMultiValued;
        }

        /**
         * Returns the type of the {@link Field}
         * 
         * @return the type of the {@link Field}
         */
        public ExtensionFieldType<?> getType() {
            return type;
        }

        /**
         * Returns the value of the {@link Field}
         * 
         * @return the value of the {@link Field}
         */
        public String getValue() {
            return value;
        }

        public boolean isMultiValued() {
            return isMultiValued;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
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
            Field other = (Field) obj;
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
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
            return "Field [type=" + type + ", value=" + value + "]";
        }

    }
}
