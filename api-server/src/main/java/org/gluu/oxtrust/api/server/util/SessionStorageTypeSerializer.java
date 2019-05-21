package org.gluu.oxtrust.api.server.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SessionStorageTypeSerializer extends JsonSerializer<SessionStorageType> {

    @Override
    public void serialize(SessionStorageType sessionStorageType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(sessionStorageType.getName());
    }

}
