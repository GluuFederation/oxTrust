/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema.strategy.serializers;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Named;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Val Pecaoco
 */
@Named("listResponseSerializer")
public class SchemaTypeAbstractSerializer extends JsonSerializer<SchemaType> {

    private Logger log= LoggerFactory.getLogger(getClass());;

    @Override
    public void serialize(SchemaType schemaType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        log.info(" serialize() ");

        try {
            jsonGenerator.writeStartObject();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            JsonNode rootNode = mapper.convertValue(schemaType, JsonNode.class);

            Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

            while (iterator.hasNext()) {

                Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

                if (!rootNodeEntry.getKey().equalsIgnoreCase("schemas")) {
                    jsonGenerator.writeFieldName(rootNodeEntry.getKey());
                    jsonGenerator.writeObject(rootNodeEntry.getValue());
                }
            }

            jsonGenerator.writeEndObject();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }
}
