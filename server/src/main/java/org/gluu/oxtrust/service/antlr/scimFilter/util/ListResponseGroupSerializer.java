/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gluu.oxtrust.model.scim2.Group;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

/**
 * @author Val Pecaoco
 */
@Named("listResponseGroupSerializer")
public class ListResponseGroupSerializer extends JsonSerializer<Group> {

    @Inject
    private Logger log;

    private String attributesArray;
    private Set<String> attributes;

    @Override
    public void serialize(Group group, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        log.info(" serialize() ");

        try {
            jsonGenerator.writeStartObject();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            attributes = (attributesArray != null && !attributesArray.isEmpty()) ? new LinkedHashSet<String>(Arrays.asList(attributesArray.split("\\,"))) : null;
            // attributes = (attributesArray != null && !attributesArray.isEmpty()) ? new LinkedHashSet<String>(Arrays.asList(mapper.readValue(attributesArray, String[].class))) : null;
            if (attributes != null && attributes.size() > 0) {
                attributes.add("schemas");
                attributes.add("id");
                attributes.add("displayName");
                attributes.add("meta.created");
                attributes.add("meta.lastModified");
                attributes.add("meta.location");
                attributes.add("meta.version");
                attributes.add("meta.resourceType");
            }

            JsonNode rootNode = mapper.convertValue(group, JsonNode.class);

            processNodes(null, rootNode, jsonGenerator);

            jsonGenerator.writeEndObject();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    /*
     * This is a recursive method to completely process all the nodes
     */
    private void processNodes(String parent, JsonNode rootNode, JsonGenerator jsonGenerator) throws Exception {

        // log.info(" ##### PARENT: " + parent);

        if (parent != null) {
            parent = FilterUtil.stripScim2Schema(parent);
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

        while (iterator.hasNext()) {

            Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

            if (attributes != null && attributes.size() > 0) {

                for (String attribute : attributes) {

                    attribute = FilterUtil.stripScim2Schema(attribute);
                    String[] split = attribute.split("\\.");

                    if (split.length == 2 && split[1] != null) {
                        if (split[1].equalsIgnoreCase("$ref")) {
                            split[1] = "reference";
                        }
                    }

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

            if (parent != null && parent.equalsIgnoreCase("meta")) {

                if (rootNodeEntry.getValue() instanceof LongNode && (rootNodeEntry.getKey().equalsIgnoreCase("created") || rootNodeEntry.getKey().equalsIgnoreCase("lastModified"))) {

                    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format

                    // In millis convert to string date
                    jsonGenerator.writeObject(dateTimeFormatter.print(Long.valueOf(rootNodeEntry.getValue().asText()).longValue()));

                } else {
                    jsonGenerator.writeObject(rootNodeEntry.getValue());
                }

            } else {
                jsonGenerator.writeObject(rootNodeEntry.getValue());
            }
        }
    }

    public void setAttributesArray(String attributesArray) {
        this.attributesArray = attributesArray;
    }
}
