/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserSerializer;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.*;

/**
 * @author Val Pecaoco
 */
@Name("listResponseUserSerializer")
public class ListResponseUserSerializer extends UserSerializer {

    @Logger
    private static Log log;

    private String attributesArray;
    private Set<String> attributes;

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

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
                attributes.add("userName");
                attributes.add("meta.created");
                attributes.add("meta.lastModified");
                attributes.add("meta.location");
                attributes.add("meta.version");
                attributes.add("meta.resourceType");
            }

            JsonNode rootNode = mapper.convertValue(user, JsonNode.class);

            processNodes(null, rootNode, mapper, user, jsonGenerator);

            jsonGenerator.writeEndObject();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the input parameters.");
        }
    }

    /*
     * This is a recursive method to completely process all the nodes
     */
    private void processNodes(String parent, JsonNode rootNode, ObjectMapper mapper, User user, JsonGenerator jsonGenerator) throws Exception {

        // log.info(" ##### PARENT: " + parent);

        Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

        while (iterator.hasNext()) {

            Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

            if (attributes != null && attributes.size() > 0) {

                for (String attribute : attributes) {

                    String[] split = attribute.split("\\.");

                    if ((((parent != null && !parent.isEmpty()) && parent.equalsIgnoreCase(split[0])) && rootNodeEntry.getKey().equalsIgnoreCase(split[1])) ||
                        rootNodeEntry.getKey().equalsIgnoreCase(split[0])) {

                        // log.info(" ##### MATCH: " + attribute);

                        writeStructure(parent, rootNodeEntry, mapper, user, jsonGenerator);

                        break;
                    }
                }

            } else {
                writeStructure(parent, rootNodeEntry, mapper, user, jsonGenerator);
            }
        }
    }

    private void writeStructure(String parent, Map.Entry<String, JsonNode> rootNodeEntry, ObjectMapper mapper, User user, JsonGenerator jsonGenerator) throws Exception {

        if (!(SchemaTypeMapping.getSchemaTypeInstance(rootNodeEntry.getKey()) instanceof UserExtensionSchema)) {

            if ((parent != null && !parent.isEmpty()) && parent.equalsIgnoreCase("groups") && rootNodeEntry.getKey().equalsIgnoreCase("reference")) {
                jsonGenerator.writeFieldName("$ref");
            } else {
                jsonGenerator.writeFieldName(rootNodeEntry.getKey());
            }

            if (rootNodeEntry.getValue() instanceof ObjectNode) {

                jsonGenerator.writeStartObject();
                processNodes(rootNodeEntry.getKey(), rootNodeEntry.getValue(), mapper, user, jsonGenerator);  // Recursion
                jsonGenerator.writeEndObject();

            } else if (rootNodeEntry.getValue() instanceof ArrayNode) {

                ArrayNode arrayNode = (ArrayNode) rootNodeEntry.getValue();

                jsonGenerator.writeStartArray();

                if (arrayNode.size() > 0) {

                    for (int i = 0; i < arrayNode.size(); i++) {

                        JsonNode arrayNodeElement = arrayNode.get(i);

                        if (arrayNodeElement.isObject()) {

                            jsonGenerator.writeStartObject();
                            processNodes(rootNodeEntry.getKey(), arrayNodeElement, mapper, user, jsonGenerator);  // Recursion
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

        } else {
            jsonGenerator.writeFieldName(rootNodeEntry.getKey());
            serializeUserExtension(rootNodeEntry, mapper, user, jsonGenerator);
        }
    }

    public void setAttributesArray(String attributesArray) {
        this.attributesArray = attributesArray;
    }
}
