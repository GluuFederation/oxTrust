package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.persist.annotation.AttributeEnum;

/**
 * @author Javier Rojas Blum
 * @version November 10, 2015
 */
public enum BlockEncryptionAlgorithm implements AttributeEnum {

    A128CBC_PLUS_HS256("A128CBC+HS256", "A128CBC+HS256"),
    A256CBC_PLUS_HS512("A256CBC+HS512", "A256CBC+HS512"),
    A128GCM("A128GCM", "A128GCM"),
    A256GCM("A256GCM", "A256GCM");

    private String value;
    private String displayName;

    private static Map<String, BlockEncryptionAlgorithm> mapByValues = new HashMap<String, BlockEncryptionAlgorithm>();

    static {
        for (BlockEncryptionAlgorithm enumType : values()) {
            mapByValues.put(enumType.getValue(), enumType);
        }
    }

    private BlockEncryptionAlgorithm(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BlockEncryptionAlgorithm getByValue(String value) {
        return mapByValues.get(value);
    }

    public Enum<? extends AttributeEnum> resolveByValue(String value) {
        return getByValue(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
