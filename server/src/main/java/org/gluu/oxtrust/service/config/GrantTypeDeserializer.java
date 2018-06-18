package org.gluu.oxtrust.service.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xdi.oxauth.model.common.GrantType;

import java.io.IOException;

class GrantTypeDeserializer extends StdDeserializer<GrantType> {

    GrantTypeDeserializer() {
        super(GrantType.class);
    }

    @Override
    public GrantType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String grantTypeName = node.asText();
        return GrantType.fromString(grantTypeName);
    }
}