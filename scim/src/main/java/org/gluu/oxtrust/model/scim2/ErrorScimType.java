package org.gluu.oxtrust.model.scim2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
public enum ErrorScimType {

    INVALID_FILTER ("invalidFilter"),
    TOO_MANY ("tooMany"),
    UNIQUENESS ("uniqueness"),
    MUTABILITY ("mutability"),
    INVALID_SYNTAX ("invalidSyntax"),
    INVALID_PATH ("invalidPath"),
    NO_TARGET ("noTarget"),
    INVALID_VALUE ("invalidValue"),
    INVALID_VERSION ("invalidVers"),
    SENSITIVE ("sensitive");

    private String value;

    private static Map<String, ErrorScimType> mapByValues = new HashMap<String, ErrorScimType>();

    static {
        for (ErrorScimType enumType : values()) {
            mapByValues.put(enumType.getValue(), enumType);
        }
    }

    ErrorScimType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ErrorScimType getByValue(String value) {
        return mapByValues.get(value);
    }

    public ErrorScimType resolveByValue(String value) {
        return getByValue(value);
    }

    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum type should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */
    @Override
    public String toString() {
        return getValue();
    }
}
