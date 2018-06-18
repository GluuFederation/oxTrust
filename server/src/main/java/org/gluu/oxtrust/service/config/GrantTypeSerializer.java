package org.gluu.oxtrust.service.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xdi.oxauth.model.common.GrantType;

import java.io.IOException;

class GrantTypeSerializer extends StdSerializer<GrantType> {

    GrantTypeSerializer() {
        super(GrantType.class);
    }

    @Override
    public void serialize(GrantType grantType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(grantType.getValue());
    }

}