/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import java.io.IOException;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.User;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

/**
 * Custom deserializer for the SCIM 2.0 User class.
 *
 * @author Val Pecaoco
 * @link User
 */
@Named("userDeserializer")
public class UserDeserializer extends JsonDeserializer<User> {

    @Logger
    private static Log log;

    @Override
    public User deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        log.info(" deserialize() ");

        try {

            JsonNode rootNode = jsonParser.readValueAsTree();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

            User user = mapper.readValue(rootNode.toString(), User.class);

            if (user.getSchemas() == null) {

                throw new IllegalArgumentException("Required field \"schemas\" is null or missing.");

            } else if (!user.getSchemas().contains(Constants.USER_CORE_SCHEMA_ID)) {

                throw new IllegalArgumentException("User Core schema is required.");

            } else if (user.getSchemas().contains(Constants.USER_EXT_SCHEMA_ID)) {

                JsonNode userExtensionNode = rootNode.get(Constants.USER_EXT_SCHEMA_ID);

                if (userExtensionNode != null) {

                    ExtensionDeserializer deserializer = new ExtensionDeserializer();
                    deserializer.setId(Constants.USER_EXT_SCHEMA_ID);

                    SimpleModule deserializerModule = new SimpleModule("ExtensionDeserializerModule", new Version(1, 0, 0, ""));
                    deserializerModule.addDeserializer(Extension.class, deserializer);
                    mapper.registerModule(deserializerModule);

                    Extension extension = mapper.readValue(userExtensionNode.toString(), Extension.class);

                    user.addExtension(extension);

                } else {
                    throw new IllegalArgumentException("User Extension schema is indicated, but value body is absent.");
                }
            }

            return user;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }
}
