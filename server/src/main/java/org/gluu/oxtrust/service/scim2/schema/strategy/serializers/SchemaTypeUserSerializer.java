/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema.strategy.serializers;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.schema.AttributeHolder;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.UserCoreSchema;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
@Named("schemaTypeUserSerializer")
public class SchemaTypeUserSerializer extends JsonSerializer<User> {

    @Logger
    private static Log log;

    private SchemaType schemaType;

    private List<AttributeHolder> attributeHolders = new ArrayList<AttributeHolder>();

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        log.info(" serialize() ");

        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            JsonNode rootNode = mapper.convertValue(user, JsonNode.class);

            Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

            while (iterator.hasNext()) {

                Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

                if (!(SchemaTypeMapping.getSchemaTypeInstance(rootNodeEntry.getKey()) instanceof UserExtensionSchema)) {

                    if (rootNodeEntry.getValue() instanceof ObjectNode) {

                        if (rootNodeEntry.getKey().equalsIgnoreCase("name")) {

                            AttributeHolder attributeHolder = new AttributeHolder();
                            attributeHolder.setName(rootNodeEntry.getKey());
                            attributeHolder.setType("string");
                            attributeHolder.setDescription("Name object");
                            attributeHolder.setRequired(Boolean.FALSE);

                            List<AttributeHolder> nameAttributeHolders = new ArrayList<AttributeHolder>();
                            Iterator<Map.Entry<String, JsonNode>> nameIterator = rootNodeEntry.getValue().getFields();

                            while (nameIterator.hasNext()) {

                                Map.Entry<String, JsonNode> nameRootNodeEntry = nameIterator.next();

                                AttributeHolder nameAttributeHolder = new AttributeHolder();
                                nameAttributeHolder.setName(nameRootNodeEntry.getKey());
                                nameAttributeHolder.setType("string");

                                if (nameRootNodeEntry.getKey().equalsIgnoreCase("formatted")) {
                                    nameAttributeHolder.setDescription("Formatted name on-the-fly for display. Using this in a query filter is not supported.");
                                    nameAttributeHolder.setMutability("readOnly");
                                } else {
                                    nameAttributeHolder.setDescription(nameRootNodeEntry.getKey());
                                }

                                if (nameRootNodeEntry.getKey().equalsIgnoreCase("givenName") || nameRootNodeEntry.getKey().equalsIgnoreCase("familyName")) {
                                    nameAttributeHolder.setRequired(true);
                                } else {
                                    nameAttributeHolder.setRequired(false);
                                }

                                nameAttributeHolders.add(nameAttributeHolder);
                            }

                            attributeHolder.setSubAttributes(nameAttributeHolders);
                            attributeHolders.add(attributeHolder);
                        }

                    } else if (rootNodeEntry.getValue() instanceof ArrayNode) {

                        AttributeHolder arrayNodeAttributeHolder = new AttributeHolder();
                        arrayNodeAttributeHolder.setName(rootNodeEntry.getKey());

                        if (rootNodeEntry.getKey().equalsIgnoreCase("groups")) {

                            arrayNodeAttributeHolder.setDescription(rootNodeEntry.getKey() + " list; using sub-attributes in a query filter is not supported (cross-querying)");
                            arrayNodeAttributeHolder.setCaseExact(Boolean.TRUE);

                            List<String> referenceTypes = new ArrayList<String>();
                            referenceTypes.add("Group");
                            arrayNodeAttributeHolder.setReferenceTypes(referenceTypes);

                        } else {
                            arrayNodeAttributeHolder.setDescription(rootNodeEntry.getKey() + " list");
                            arrayNodeAttributeHolder.setCaseExact(Boolean.FALSE);
                        }

                        arrayNodeAttributeHolder.setRequired(Boolean.FALSE);
                        arrayNodeAttributeHolder.setMultiValued(Boolean.TRUE);

                        if (rootNodeEntry.getKey().equalsIgnoreCase("schemas")) {
                            arrayNodeAttributeHolder.setUniqueness("server");
                            arrayNodeAttributeHolder.setType("string");
                            arrayNodeAttributeHolder.setCaseExact(Boolean.TRUE);
                            arrayNodeAttributeHolder.setReturned("always");
                        } else {
                            arrayNodeAttributeHolder.setType("complex");
                        }

                        if (rootNodeEntry.getKey().equalsIgnoreCase("photos")) {

                            arrayNodeAttributeHolder.setType("reference");

                            List<String> referenceTypes = new ArrayList<String>();
                            referenceTypes.add("uri");
                            arrayNodeAttributeHolder.setReferenceTypes(referenceTypes);
                        }

                        List<AttributeHolder> arrayNodeMapAttributeHolders = new ArrayList<AttributeHolder>();

                        Iterator<JsonNode> arrayNodeIterator = rootNodeEntry.getValue().getElements();

                        while (arrayNodeIterator.hasNext()) {

                            JsonNode jsonNode = arrayNodeIterator.next();

                            Iterator<Map.Entry<String, JsonNode>> arrayNodeMapIterator = jsonNode.getFields();

                            while (arrayNodeMapIterator.hasNext()) {

                                Map.Entry<String, JsonNode> arrayNodeMapRootNodeEntry = arrayNodeMapIterator.next();

                                AttributeHolder arrayNodeMapAttributeHolder = new AttributeHolder();

                                if (rootNodeEntry.getKey().equalsIgnoreCase("groups") && arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("reference")) {
                                    arrayNodeMapAttributeHolder.setName("$ref");
                                } else {
                                    arrayNodeMapAttributeHolder.setName(arrayNodeMapRootNodeEntry.getKey());
                                }

                                arrayNodeMapAttributeHolder.setType("string");
                                arrayNodeMapAttributeHolder.setDescription(arrayNodeMapRootNodeEntry.getKey());

                                if (arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("value") || arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("type")) {
                                    arrayNodeMapAttributeHolder.setRequired(Boolean.TRUE);
                                } else {
                                    arrayNodeMapAttributeHolder.setRequired(Boolean.FALSE);
                                }

                                if (arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("valueAsImageDataURI") || arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("valueAsURI")) {

                                    arrayNodeMapAttributeHolder.setMutability("readOnly");
                                    arrayNodeMapAttributeHolder.setType("reference");

                                    List<String> referenceTypes = new ArrayList<String>();
                                    referenceTypes.add("uri");
                                    arrayNodeMapAttributeHolder.setReferenceTypes(referenceTypes);
                                }

                                arrayNodeMapAttributeHolders.add(arrayNodeMapAttributeHolder);
                            }

                            arrayNodeAttributeHolder.setSubAttributes(arrayNodeMapAttributeHolders);
                            attributeHolders.add(arrayNodeAttributeHolder);
                        }

                    } else {

                        AttributeHolder attributeHolder = new AttributeHolder();
                        attributeHolder.setName(rootNodeEntry.getKey());

                        if (rootNodeEntry.getValue().isBoolean()) {
                            attributeHolder.setType("boolean");
                        } else {
                            attributeHolder.setType("string");
                        }

                        attributeHolder.setDescription(rootNodeEntry.getKey());

                        if (rootNodeEntry.getKey().equalsIgnoreCase("userName") || rootNodeEntry.getKey().equalsIgnoreCase("displayName")) {
                            attributeHolder.setRequired(Boolean.TRUE);
                        } else {
                            attributeHolder.setRequired(Boolean.FALSE);
                        }

                        if (rootNodeEntry.getKey().equalsIgnoreCase("id") || rootNodeEntry.getKey().equalsIgnoreCase("userName")) {
                            attributeHolder.setUniqueness("server");
                            attributeHolder.setReturned("always");
                        }

                        if (rootNodeEntry.getKey().equalsIgnoreCase("id") || rootNodeEntry.getKey().equalsIgnoreCase("externalId") || rootNodeEntry.getKey().equalsIgnoreCase("password")) {
                            attributeHolder.setCaseExact(Boolean.TRUE);
                        }

                        if (rootNodeEntry.getKey().equalsIgnoreCase("id")) {
                            attributeHolder.setMutability("readOnly");
                        }

                        attributeHolders.add(attributeHolder);
                    }
                }
            }

            UserCoreSchema userCoreSchema = (UserCoreSchema) schemaType;
            userCoreSchema.setAttributeHolders(attributeHolders);
            schemaType = userCoreSchema;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the User class structure.");
        }
    }

    public SchemaType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(SchemaType schemaType) {
        this.schemaType = schemaType;
    }
}
