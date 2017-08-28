/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom deserializer for the SCIM 2.0 User class.
 *
 * @author Val Pecaoco
 * @link User
 */
@Stateless
@Named
public class UserDeserializer extends JsonDeserializer<User> {

    private Logger log= LoggerFactory.getLogger(UserDeserializer.class);

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