package org.gluu.oxtrust.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.persist.annotation.AttributeEnum;

/**
 * @author Javier Rojas Blum
 * @version November 10, 2015
 */
public enum KeyEncryptionAlgorithm implements AttributeEnum {

    RSA1_5("RSA1_5", "RSA1_5"),
    RSA_OAEP("RSA-OAEP", "RSA-OAEP"),
    A128KW("A128KW", "A128KW"),
    A256KW("A256KW", "A256KW");
    //DIR("dir", "dir"), // Not supported
    //ECDH_ES("ECDH-ES", "ECDH-ES"), // Not supported
    //ECDH_ES_PLUS_A128KW("ECDH-ES+A128KW", "ECDH-ES+A128KW"), // Not supported
    //ECDH_ES_A256KW("ECDH-ES+A256KW", "ECDH-ES+A256KW"); // Not supported

    private String value;
    private String displayName;

    private static Map<String, KeyEncryptionAlgorithm> mapByValues = new HashMap<String, KeyEncryptionAlgorithm>();

    static {
        for (KeyEncryptionAlgorithm enumType : values()) {
            mapByValues.put(enumType.getValue(), enumType);
        }
    }

    private KeyEncryptionAlgorithm(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KeyEncryptionAlgorithm getByValue(String value) {
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
