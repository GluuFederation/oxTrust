/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.helper;

import java.io.IOException;

import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.Constants;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class UserDeserializer extends StdDeserializer<User> {

	private static final long serialVersionUID = 1L;

	public UserDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = jp.readValueAsTree();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ExtensionDeserializer deserializer = new ExtensionDeserializer(Extension.class);
        SimpleModule testModule = new SimpleModule("ExtensionDeserializerModule", Version.unknownVersion())
                .addDeserializer(Extension.class, deserializer);
        mapper.registerModule(testModule);

        User user = mapper.readValue(rootNode.toString(), User.class);
        if (user.getSchemas() == null) {
            throw new JsonMappingException("Required field Schema is missing");
        }
        if (user.getSchemas().size() == 1) {
            return user;
        }

        User userObj = new User();

        for (String urn : user.getSchemas()) {
            if (urn.equals(Constants.USER_CORE_SCHEMA)) {
                continue;
            }

            JsonNode extensionNode = rootNode.get(urn);
            if (extensionNode == null) {
                throw new JsonParseException("Registered extension not present.", JsonLocation.NA);
            }

            deserializer.setUrn(urn);
            Extension extension = mapper.readValue(extensionNode.toString(), Extension.class);
            userObj.addExtension(extension);

        }
        return userObj;
    }

}
