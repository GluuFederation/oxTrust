/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */


package org.gluu.oxtrust.model.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;

import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ExtensionSerializer extends JsonSerializer<Extension> {

    @Override
    public void serialize(Extension value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        Map<String, Extension.Field> fields = value.getFields();
        for (Entry<String, Extension.Field> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            ExtensionFieldType<?> fieldType = entry.getValue().getType();
            String rawFieldValue = entry.getValue().getValue();

            jgen.writeFieldName(fieldName);

            if (fieldType == ExtensionFieldType.INTEGER) {
                BigInteger valueAsBigInteger = ExtensionFieldType.INTEGER.fromString(rawFieldValue);
                jgen.writeNumber(valueAsBigInteger);
            } else if (fieldType == ExtensionFieldType.DECIMAL) {
                BigDecimal valueAsBigDecimal = ExtensionFieldType.DECIMAL.fromString(rawFieldValue);
                jgen.writeNumber(valueAsBigDecimal);
            } else if (fieldType == ExtensionFieldType.BOOLEAN) {
                Boolean valueAsBoolean = ExtensionFieldType.BOOLEAN.fromString(rawFieldValue);
                jgen.writeBoolean(valueAsBoolean);
            } else {
                jgen.writeString(rawFieldValue);
            }
        }

        jgen.writeEndObject();
    }

}
