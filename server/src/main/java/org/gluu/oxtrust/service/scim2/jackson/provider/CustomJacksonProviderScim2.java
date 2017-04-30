/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.provider;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserDeserializer;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserSerializer;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * RESTEasy custom Jackson provider-interceptor for registration of custom serializers and deserializers
 * specifically for SCIM 2.0.
 *
 * @author Val Pecaoco
 */
@Provider
@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
@Produces({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
@Named("customJacksonProviderScim2")
public class CustomJacksonProviderScim2 extends JacksonJaxbJsonProvider implements ContextResolver<ObjectMapper> {

    @Logger
    private static Log log;

    public CustomJacksonProviderScim2() {

        super();

        log.info(" CustomJacksonProviderScim2() ");

        ObjectMapper mapper = _mapperConfig.getDefaultMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule customJacksonModule = new SimpleModule("CustomJacksonModuleScim2", new Version(1, 0, 0, ""));

        customJacksonModule.addSerializer(User.class, new UserSerializer());
        customJacksonModule.addDeserializer(User.class, new UserDeserializer());

        mapper.registerModule(customJacksonModule);
    }

    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        return _mapperConfig.getDefaultMapper();
    }
}
