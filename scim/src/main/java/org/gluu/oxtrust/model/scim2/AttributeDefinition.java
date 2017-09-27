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
    }
/*
    @Attribute(description = "The attribute's name.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final String name;

    @Attribute(description = "The attribute's data type.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final Type type;

    @Attribute(description = "When an attribute is of type \"complex\", " +
            "\"subAttributes\" defines set of sub-attributes.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE,
            multiValueClass = AttributeDefinition.class)
    private final Collection<AttributeDefinition> subAttributes;

    @Attribute(description = "Boolean value indicating the attribute's " +
            "plurality.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final boolean multiValued;

    @Attribute(description = "The attribute's human readable description.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final String description;

    @Attribute(description = "A Boolean value that specifies if the " +
            "attribute is required.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final boolean required;

    @Attribute(description = "A collection of suggested canonical values " +
            "that MAY be used.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE,
            multiValueClass = String.class)
    private final Collection<String> canonicalValues;

    @Attribute(description = "A Boolean value that specifies if the " +
            "String attribute is case sensitive.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final boolean caseExact;

    @Attribute(description = "A single keyword indicating the " +
            "circumstances under which the value of the attribute can be " +
            "(re)defined.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final Mutability mutability;

    @Attribute(description = "A single keyword that indicates when an " +
            "attribute and associated values are returned in response to a GET " +
            "request or in response to a PUT, POST, or PATCH request.",
            isRequired = true,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final Returned returned;

    @Attribute(description = "A single keyword value that specifies how " +
            "the service provider enforces uniqueness of attribute values.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE)
    private final Uniqueness uniqueness;

    @Attribute(description = "A multi-valued array of JSON strings that " +
            "indicate the SCIM resource types that may be referenced.",
            isRequired = false,
            isCaseExact = false,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.DEFAULT,
            uniqueness = AttributeDefinition.Uniqueness.NONE,
            multiValueClass = String.class)
    private final Collection<String> referenceTypes;

    AttributeDefinition(final String name,
                        final Type type,
                        final Collection<AttributeDefinition> subAttributes,
                        final boolean multiValued,
                        final String description,
                        final boolean required,
                        final Collection<String> canonicalValues,
                        final boolean caseExact,
                        final Mutability mutability,
                        final Returned returned,
                        final Uniqueness uniqueness,
                        final Collection<String> referenceTypes){
        this.name = name;
        this.type = type;
        this.subAttributes = subAttributes == null ?
                null : Collections.unmodifiableList(
                new ArrayList<AttributeDefinition>(subAttributes));
        this.multiValued = multiValued;
        this.description = description;
        this.required = required;
        this.canonicalValues = canonicalValues == null ?
                null : Collections.unmodifiableList(
                new ArrayList<String>(canonicalValues));
        this.caseExact = caseExact;
        this.mutability = mutability;
        this.returned = returned;
        this.uniqueness = uniqueness;
        this.referenceTypes = referenceTypes == null ?
                null : Collections.unmodifiableList(
                new ArrayList<String>(referenceTypes));
    }
*/
}
