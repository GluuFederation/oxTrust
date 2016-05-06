/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gluu.oxtrust.model.scim2.Group;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
@Name("listResponseGroupSerializer")
public class ListResponseGroupSerializer extends JsonSerializer<Group> {

    @Logger
    private static Log log;

    private String attributesArray;
    private String[] attributes;

    @Override
    public void serialize(Group group, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        log.info(" serialize() ");

        try {

            jsonGenerator.writeStartObject();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            attributes = (attributesArray != null && !attributesArray.isEmpty()) ? mapper.readValue(attributesArray, String[].class) : null;

            JsonNode rootNode = mapper.convertValue(group, JsonNode.class);

            processNodes(null, rootNode, jsonGenerator);

            jsonGenerator.writeEndObject();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the input parameters.");
        }
    }

    /*
     * This is a recursive method to completely process all the nodes
     */
    private void processNodes(String parent, JsonNode rootNode, JsonGenerator jsonGenerator) throws Exception {

        // log.info(" ##### PARENT: " + parent);

        Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

        while (iterator.hasNext()) {

            Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

            if (attributes != null && attributes.length > 0) {

                for (String attribute : attributes) {

                    String[] split = attribute.split("\\.");

                    if ((((parent != null && !parent.isEmpty()) && parent.equalsIgnoreCase(split[0])) && rootNodeEntry.getKey().equalsIgnoreCase(split[1])) ||
                        rootNodeEntry.getKey().equalsIgnoreCase(split[0])) {

                        // log.info(" ##### MATCH: " + attribute);

                        writeStructure(parent, rootNodeEntry, jsonGenerator);

                        break;
                    }
                }

            } else {
                writeStructure(parent, rootNodeEntry, jsonGenerator);
            }
        }
    }

    private void writeStructure(String parent, Map.Entry<String, JsonNode> rootNodeEntry, JsonGenerator jsonGenerator) throws Exception {

        // No Group Extension Schema yet

        if ((parent != null && !parent.isEmpty()) && parent.equalsIgnoreCase("members") && rootNodeEntry.getKey().equalsIgnoreCase("reference")) {
            jsonGenerator.writeFieldName("$ref");
        } else {
            jsonGenerator.writeFieldName(rootNodeEntry.getKey());
        }

        if (rootNodeEntry.getValue() instanceof ObjectNode) {

            jsonGenerator.writeStartObject();
            processNodes(rootNodeEntry.getKey(), rootNodeEntry.getValue(), jsonGenerator);  // Recursion
            jsonGenerator.writeEndObject();

        } else if (rootNodeEntry.getValue() instanceof ArrayNode) {

            ArrayNode arrayNode = (ArrayNode) rootNodeEntry.getValue();

            jsonGenerator.writeStartArray();

            if (arrayNode.size() > 0) {

                for (int i = 0; i < arrayNode.size(); i++) {

                    JsonNode arrayNodeElement = arrayNode.get(i);

                    if (arrayNodeElement.isObject()) {

                        jsonGenerator.writeStartObject();
                        processNodes(rootNodeEntry.getKey(), arrayNodeElement, jsonGenerator);  // Recursion
                        jsonGenerator.writeEndObject();

                    } else {
                        jsonGenerator.writeObject(arrayNodeElement);
                    }
                }
            }

            jsonGenerator.writeEndArray();

        } else {
            jsonGenerator.writeObject(rootNodeEntry.getValue());
        }
    }

    public void setAttributesArray(String attributesArray) {
        this.attributesArray = attributesArray;
    }
}
