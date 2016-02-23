/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */


package org.gluu.oxtrust.model.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * This class is package-private by intention. If you need to use it, something went wrong.
 */
class ExtensionDeserializer extends StdDeserializer<Extension> {

    private static final long serialVersionUID = 2581146730706177962L;

    private String urn;

    protected ExtensionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Extension deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (urn == null || urn.isEmpty()) {
            throw new IllegalStateException("The URN cannot be null or empty");
        }
        JsonNode rootNode = jp.readValueAsTree();
        if (rootNode.getNodeType() != JsonNodeType.OBJECT) {
            throw new JsonMappingException("Extension is of wrong JSON type");
        }
        Extension.Builder extensionBuilder = new Extension.Builder(urn);
        Iterator<Map.Entry<String, JsonNode>> fieldIterator = rootNode.fields();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldIterator.next();
            switch (entry.getValue().getNodeType()) {
                case BOOLEAN:
                    handleBoolean(extensionBuilder, entry);
                    break;
                case STRING:
                    handleString(extensionBuilder, entry);
                    break;
                case NUMBER:
                    handleNumber(extensionBuilder, entry);
                    break;
                default:
                    throw new IllegalArgumentException("JSON type not supported: " + entry.getValue().getNodeType());
            }
        }

        return extensionBuilder.build();
    }

    private void handleNumber(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {

        String stringValue = entry.getValue().asText();
        if (stringValue.contains(".")) {
            BigDecimal value = ExtensionFieldType.DECIMAL.fromString(stringValue);
            extensionBuilder.setField(entry.getKey(), value);
        } else {
            BigInteger value = ExtensionFieldType.INTEGER.fromString(stringValue);
            extensionBuilder.setField(entry.getKey(), value);
        }
    }

    private void handleString(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {
        String value = ExtensionFieldType.STRING.fromString(entry.getValue().asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    private void handleBoolean(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {
        Boolean value = ExtensionFieldType.BOOLEAN.fromString(entry.getValue().asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    void setUrn(String urn) {
        this.urn = urn;
    }
}