package org.gluu.oxtrust.service.config.oxauth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.gluu.oxtrust.api.configuration.oxauth.ResponseTypeApi;

import java.io.IOException;

class ResponseTypeSerializer extends StdSerializer<ResponseTypeApi> {

    ResponseTypeSerializer() {
        super(ResponseTypeApi.class);
    }

    @Override
    public void serialize(ResponseTypeApi responseType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(responseType.name().toLowerCase());
    }

}