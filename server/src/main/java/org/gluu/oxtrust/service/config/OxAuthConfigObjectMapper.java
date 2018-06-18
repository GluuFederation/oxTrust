package org.gluu.oxtrust.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.gluu.oxtrust.api.configuration.OxAuthConfig;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;
import org.xdi.oxauth.model.common.GrantType;

import java.io.IOException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;

public class OxAuthConfigObjectMapper {

    public OxAuthConfig deserialize(LdapOxAuthConfiguration ldapOxAuthConfiguration) throws IOException {
        return deserialize(ldapOxAuthConfiguration.getOxAuthConfigDynamic());
    }

    public OxAuthConfig deserialize(String oxAuthConfigDynamic) throws IOException {
        return mapper().readValue(oxAuthConfigDynamic, OxAuthConfig.class);
    }

    public String serialize(OxAuthConfig configuration) throws IOException {
        return mapper().writeValueAsString(configuration);
    }

    private ObjectMapper mapper() {
        return new ObjectMapper()
                .registerModule(new SimpleModule()
                        .addDeserializer(GrantType.class, new GrantTypeDeserializer())
                        .addSerializer(GrantType.class, new GrantTypeSerializer()))
                .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}