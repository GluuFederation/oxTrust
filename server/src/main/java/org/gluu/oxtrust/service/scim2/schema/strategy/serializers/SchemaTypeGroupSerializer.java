/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema.strategy.serializers;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.schema.AttributeHolder;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.GroupCoreSchema;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
@Name("schemaTypeGroupSerializer")
public class SchemaTypeGroupSerializer extends JsonSerializer<Group> {

    @Logger
    private static Log log;

    private SchemaType schemaType;

    private List<AttributeHolder> attributeHolders = new ArrayList<AttributeHolder>();

    @Override
    public void serialize(Group group, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        log.info(" serialize() ");

        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            JsonNode rootNode = mapper.convertValue(group, JsonNode.class);

            Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

            while (iterator.hasNext()) {

                Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

                if (rootNodeEntry.getValue() instanceof ObjectNode) {
                    // Definitely maybe in the near future
                } else if (rootNodeEntry.getValue() instanceof ArrayNode) {

                    AttributeHolder arrayNodeAttributeHolder = new AttributeHolder();
                    arrayNodeAttributeHolder.setName(rootNodeEntry.getKey());

                    if (rootNodeEntry.getKey().equalsIgnoreCase("members")) {

                        arrayNodeAttributeHolder.setDescription(rootNodeEntry.getKey() + " list; using sub-attributes in a query filter is not supported (cross-querying)");
                        arrayNodeAttributeHolder.setCaseExact(Boolean.TRUE);

                        List<String> referenceTypes = new ArrayList<String>();
                        referenceTypes.add("User");
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

                    List<AttributeHolder> arrayNodeMapAttributeHolders = new ArrayList<AttributeHolder>();

                    Iterator<JsonNode> arrayNodeIterator = rootNodeEntry.getValue().getElements();

                    while (arrayNodeIterator.hasNext()) {

                        JsonNode jsonNode = arrayNodeIterator.next();

                        Iterator<Map.Entry<String, JsonNode>> arrayNodeMapIterator = jsonNode.getFields();

                        while (arrayNodeMapIterator.hasNext()) {

                            Map.Entry<String, JsonNode> arrayNodeMapRootNodeEntry = arrayNodeMapIterator.next();

                            AttributeHolder arrayNodeMapAttributeHolder = new AttributeHolder();

                            if (rootNodeEntry.getKey().equalsIgnoreCase("members") && arrayNodeMapRootNodeEntry.getKey().equalsIgnoreCase("reference")) {
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

                            arrayNodeMapAttributeHolders.add(arrayNodeMapAttributeHolder);
                        }

                        arrayNodeAttributeHolder.setSubAttributes(arrayNodeMapAttributeHolders);
                        attributeHolders.add(arrayNodeAttributeHolder);
                    }

                } else {

                    if (!rootNodeEntry.getKey().equalsIgnoreCase("externalId")) {

                        AttributeHolder attributeHolder = new AttributeHolder();
                        attributeHolder.setName(rootNodeEntry.getKey());

                        if (rootNodeEntry.getValue().isBoolean()) {
                            attributeHolder.setType("boolean");
                        } else {
                            attributeHolder.setType("string");
                        }

                        attributeHolder.setDescription(rootNodeEntry.getKey());

                        attributeHolder.setRequired(Boolean.FALSE);

                        if (rootNodeEntry.getKey().equalsIgnoreCase("id")) {
                            attributeHolder.setUniqueness("server");
                            attributeHolder.setCaseExact(Boolean.TRUE);
                            attributeHolder.setMutability("readOnly");
                            attributeHolder.setReturned("always");
                        }

                        attributeHolders.add(attributeHolder);
                    }
                }
            }

            GroupCoreSchema groupCoreSchema = (GroupCoreSchema) schemaType;
            groupCoreSchema.setAttributeHolders(attributeHolders);
            schemaType = groupCoreSchema;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the Group class structure");
        }
    }

    public SchemaType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(SchemaType schemaType) {
        this.schemaType = schemaType;
    }
}
