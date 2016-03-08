/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.OxMultivalued;
import org.xdi.model.ScimCustomAtribute;

/**
 * Custom deserializer for the SCIM 2.0 User Extension class.
 *
 * This class is package-private by intention. If you need to use it, something went wrong.
 */
@Name("extensionDeserializer")
public class ExtensionDeserializer extends JsonDeserializer<Extension> {

    @Logger
    private static Log log;

    private String id;

    @Override
    public Extension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        log.info(" deserialize() ");

        try {

            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("The URN cannot be null or empty");
            }

            JsonNode rootNode = jsonParser.readValueAsTree();
            if (!rootNode.isObject()) {
                throw new IllegalArgumentException("Extension is of wrong JSON type");
            }

            AttributeService attributeService = AttributeService.instance();

            Extension.Builder extensionBuilder = new Extension.Builder(id);

            Iterator<Map.Entry<String, JsonNode>> fieldIterator = rootNode.getFields();

            while (fieldIterator.hasNext()) {

                Map.Entry<String, JsonNode> entry = fieldIterator.next();

                GluuAttribute gluuAttribute = attributeService.getAttributeByName(entry.getKey());

                if (gluuAttribute != null) {
                    
                    if (!(gluuAttribute.isCustom() && (gluuAttribute.getOxSCIMCustomAttribute() != null && gluuAttribute.getOxSCIMCustomAttribute().equals(ScimCustomAtribute.TRUE)))) {
                        log.info(" NOT A CUSTOM ATTRIBUTE: " + gluuAttribute.getName());
                        throw new IllegalArgumentException("NOT A CUSTOM ATTRIBUTE: " + gluuAttribute.getName());
                    }

                    GluuAttributeDataType attributeDataType = gluuAttribute.getDataType();

                    if ((gluuAttribute.getOxMultivaluedAttribute() != null) && gluuAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {

                        if (entry.getValue() instanceof ArrayNode) {

                            ArrayNode arrayNode = (ArrayNode)entry.getValue();

                            ObjectMapper mapper = new ObjectMapper();
                            mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

                            if (attributeDataType.equals(GluuAttributeDataType.STRING) || attributeDataType.equals(GluuAttributeDataType.PHOTO)) {
                                List<String> stringList = Arrays.asList(mapper.readValue(arrayNode, String[].class));
                                extensionBuilder.setFieldAsList(entry.getKey(), stringList);
                            } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                                List<Date> dateList = Arrays.asList(mapper.readValue(arrayNode, Date[].class));  // For validation
                                extensionBuilder.setFieldAsList(entry.getKey(), Arrays.asList(mapper.readValue(arrayNode, String[].class)));
                            } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {
                                List<BigDecimal> numberList = Arrays.asList(mapper.readValue(arrayNode, BigDecimal[].class));
                                extensionBuilder.setFieldAsList(entry.getKey(), numberList);
                            } else {
                                log.info(" NO MATCH: attributeDataType.getDisplayName() = " + attributeDataType.getDisplayName());
                                throw new IllegalArgumentException("JSON type not supported: " + entry.getValue().toString());
                            }

                        } else {
                            throw new IllegalArgumentException("Attribute \"" + entry.getKey() + "\" is multi-valued but passed value is not of array type.");
                        }

                    } else {

                        if (entry.getValue() instanceof ArrayNode) {
                            throw new IllegalArgumentException("Attribute \"" + entry.getKey() + "\" is not multi-valued but passed value is of array type.");
                        } else {
                            if (attributeDataType.equals(GluuAttributeDataType.STRING) || attributeDataType.equals(GluuAttributeDataType.PHOTO)) {
                                handleString(extensionBuilder, entry);
                            } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                                handleDateTime(extensionBuilder, entry);
                            } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {
                                handleNumber(extensionBuilder, entry);
                            } else {
                                log.info(" NO MATCH: attributeDataType.getDisplayName() = " + attributeDataType.getDisplayName());
                                throw new IllegalArgumentException("JSON type not supported: " + entry.getValue().toString());
                            }
                        }
                    }

                } else {
                    throw new IllegalArgumentException("NOT FOUND: custom attribute = " + entry.getKey());
                }
            }

            return extensionBuilder.build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the input parameters.");
        }
    }

    private void handleNumber(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {
        BigDecimal value = ExtensionFieldType.DECIMAL.fromString(entry.getValue().asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    private void handleString(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {
        String value = ExtensionFieldType.STRING.fromString(entry.getValue().asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    private void handleDateTime(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry) {
        Date value = ExtensionFieldType.DATE_TIME.fromString(entry.getValue().asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    void setId(String id) {
        this.id = id;
    }
}