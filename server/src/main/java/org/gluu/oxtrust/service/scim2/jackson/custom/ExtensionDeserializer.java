/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import javax.ejb.Stateless;
import javax.inject.Named;

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
import org.gluu.oxtrust.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.OxMultivalued;
import org.xdi.model.ScimCustomAtribute;
import org.xdi.service.cdi.util.CdiUtil;

/**
 * Custom deserializer for the SCIM 2.0 User Extension class.
 */
@Stateless
@Named
public class ExtensionDeserializer extends JsonDeserializer<Extension> {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AttributeService attributeService;

    private String id;	

    @Override
    public Extension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        log.info(" deserialize() ");

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            attributeService= CdiUtil.bean(AttributeService.class);
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("The URN cannot be null or empty");
            }

            JsonNode rootNode = jsonParser.readValueAsTree();
            if (!rootNode.isObject()) {
                throw new IllegalArgumentException("Extension is of wrong JSON type");
            }

            boolean deepNode=rootNode.get("fields")!=null && rootNode.size()==1;
            if (deepNode)
                rootNode=rootNode.get("fields");

            Extension.Builder extensionBuilder = new Extension.Builder(id);

            Iterator<Map.Entry<String, JsonNode>> fieldIterator = rootNode.getFields();

            while (fieldIterator.hasNext()) {

                Map.Entry<String, JsonNode> entry = fieldIterator.next();

                GluuAttribute gluuAttribute = attributeService.getAttributeByName(entry.getKey());

                if (gluuAttribute != null) {

                    if (!(gluuAttribute.getOxSCIMCustomAttribute() != null && gluuAttribute.getOxSCIMCustomAttribute().equals(ScimCustomAtribute.TRUE))) {
                        log.info(" NOT A CUSTOM ATTRIBUTE: " + gluuAttribute.getName());
                        throw new IllegalArgumentException("NOT A CUSTOM ATTRIBUTE: " + gluuAttribute.getName());
                    }

                    GluuAttributeDataType attributeDataType = gluuAttribute.getDataType();

                    if ((gluuAttribute.getOxMultivaluedAttribute() != null) && gluuAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {
                        JsonNode node=entry.getValue();
                        if (deepNode)
                            node=mapper.readTree(node.get("value").asText());

                        if (node instanceof ArrayNode) {

                            ArrayNode arrayNode = (ArrayNode)node;

                            if (attributeDataType.equals(GluuAttributeDataType.STRING) || attributeDataType.equals(GluuAttributeDataType.PHOTO)) {
                                List<String> stringList = Arrays.asList(mapper.readValue(arrayNode, String[].class));
                                extensionBuilder.setFieldAsList(entry.getKey(), stringList);
                            } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                                //Convert dates to suitable format for storing in LDAP
                                List<String> strDates=Arrays.asList(mapper.readValue(arrayNode, String[].class));
                                List<String> formattedStrDates=new ArrayList<String>();
                                for (String strDate : strDates){
                                    String formatted= DateUtil.ISOToGeneralizedStringDate(strDate);
                                    if (formatted==null){
                                        throw new IllegalArgumentException("Date supplied " + strDate + " not in ISO 8601 format");
                                    }
                                    else{
                                        formattedStrDates.add(formatted);
                                        log.debug("Added generalized date: {}", formatted);
                                    }
                                }
                                extensionBuilder.setFieldAsList(entry.getKey(), formattedStrDates);
                                /*
                                List<Date> dateList = Arrays.asList(mapper.readValue(arrayNode, Date[].class));  // For validation
                                extensionBuilder.setFieldAsList(entry.getKey(), Arrays.asList(mapper.readValue(arrayNode, String[].class)));
                                */
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
                                handleString(extensionBuilder, entry, deepNode);
                            } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                                handleDateTime(extensionBuilder, entry, deepNode);
                            } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {
                                handleNumber(extensionBuilder, entry, deepNode);
                            } else {
                                log.info(" NO MATCH: attributeDataType.getDisplayName() = " + attributeDataType.getDisplayName());
                                throw new IllegalArgumentException("JSON type not supported: " + entry.getValue().toString());
                            }
                        }
                    }

                } else {
                    //throw new IllegalArgumentException("NOT FOUND: custom attribute = " + entry.getKey());
                    log.error("NOT FOUND: custom attribute = " + entry.getKey());
                }
            }

            return extensionBuilder.build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    private void handleNumber(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry, boolean inner) {
        JsonNode node=inner ? entry.getValue().get("value") : entry.getValue();
        BigDecimal value = ExtensionFieldType.DECIMAL.fromString(node.asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    private void handleString(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry, boolean inner) {
        JsonNode node=inner ? entry.getValue().get("value") : entry.getValue();
        String value = ExtensionFieldType.STRING.fromString(node.asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    private void handleDateTime(Extension.Builder extensionBuilder, Map.Entry<String, JsonNode> entry, boolean inner) {
        JsonNode node=inner ? entry.getValue().get("value") : entry.getValue();
        Date value = ExtensionFieldType.DATE_TIME.fromString(node.asText());
        extensionBuilder.setField(entry.getKey(), value);
    }

    void setId(String id) {
        this.id = id;
    }
}