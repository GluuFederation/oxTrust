package org.gluu.oxtrust.api.server.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class SessionStorageTypeDeserializer extends JsonDeserializer<SessionStorageType> {

	@Override
	public SessionStorageType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		JsonNode node = jsonParser.getCodec().readTree(jsonParser);
		return SessionStorageType.from(node.asText());
	}

}