package org.gluu.oxtrust.model.scim2;

/**
 * Created by jgomer on 2017-09-04.
 *
 * Represents the typical SCIM attribute characteristics: required, canonicalValues, caseExact, etc. according to
 * section 2.2 of RFC7643
 * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/types/AttributeDefinition.java
 */
public class AttributeDefinition {

    /**
     * An enumeration of the data types for values.
     */
    public enum Type {
        STRING("string"),
        /**
         * Boolean datatype.
         */
        BOOLEAN("boolean"),
        /**
         * Decimal datatype.
         */
        DECIMAL("decimal"),
        /**
         * Integer datatype.
         */
        INTEGER("integer"),
        /**
         * Datetime datatype.
         */
        DATETIME("dateTime"),
        /**
         * Binary datatype.
         */
        BINARY("binary"),
        /**
         * Reference datatype.
         */
        REFERENCE("reference"),
        /**
         * Complex datatype.
         */
        COMPLEX("complex");

        private String name;

        /**
         * Constructs an attribute type object.
         * @param name the name (used in SCIM schemas) of the object.
         */
        Type(final String name){
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

    }

    /**
     * This enum is used to describe the mutability of an attribute.
     */
    public enum Mutability{
        /**
         * The attribute can be read, but not written.
         */
        READ_ONLY("readOnly"),
        /**
         * The attribute can be read, and written.
         */
        READ_WRITE("readWrite"),
        /**
         * The attribute can be read, and cannot be set after
         * object creation.  It can be set during object creation.
         */
        IMMUTABLE("immutable"),
        /**
         * The attribute can only be written, and not read.  This
         * might be used for password hashes for example.
         */
        WRITE_ONLY("writeOnly");

        /**
         * The SCIM name for this enum.
         */
        private String name;

        /**
         * Mutability enum private constructor.
         *
         * @param name the name of the mutability constraint.
         */
        Mutability(final String name){
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

    }

    /**
     * This enum is used to describe the when an attribute is returned
     * from scim methods.
     */
    public enum Returned{
        /**
         * Indicates that the attribute is always returned.
         */
        ALWAYS("always"),
        /**
         * Indicates that the attribute is never returned.
         */
        NEVER("never"),
        /**
         * Indicates that the attribute is returned by default.
         */
        DEFAULT("default"),
        /**
         * Indicates that the attribute is only returned if requested.
         */
        REQUEST("request");

        /**
         * The SCIM name for this enum.
         */
        private String name;

        /**
         * Returned enum private constructor.
         *
         * @param name the name of the return constraint.
         */
        Returned(final String name){
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

    }

    public enum Uniqueness{
        /**
         * Indicates that this attribute's value need not be unique.
         */
        NONE("none"),
        /**
         * Indicates that this attribute's value must be unique for a given server.
         */
        SERVER("server"),
        /**
         * Indicates that this attribute's value must be globally unique.
         */
        GLOBAL("global");

        private String name;

        /**
         * Uniqueness enum private constructor.
         *
         * @param name the name of the uniqueness constraint.
         */
        Uniqueness(final String name){
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

    }

}
