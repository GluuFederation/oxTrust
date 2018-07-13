package org.gluu.oxtrust.api.authorization.casprotocol;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.gluu.oxtrust.api.authorization.casprotocol.SessionStorageType;

import java.io.IOException;

public class SessionStorageTypeSerializer extends JsonSerializer<SessionStorageType> {

    @Override
    public void serialize(SessionStorageType sessionStorageType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(sessionStorageType.getName());
    }

}