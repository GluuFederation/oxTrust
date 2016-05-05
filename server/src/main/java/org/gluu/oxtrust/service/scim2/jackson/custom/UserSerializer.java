/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.*;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.OxMultivalued;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Custom serializer for the SCIM 2.0 User class.
 *
 * @author Val Pecaoco
 * @link User
 */
@Name("userSerializer")
public class UserSerializer extends JsonSerializer<User> {

    @Logger
    private static Log log;

    @Override
    public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        log.info(" serialize() ");

        try {

            jsonGenerator.writeStartObject();

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

            JsonNode rootNode = mapper.convertValue(user, JsonNode.class);

            Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();
            while (iterator.hasNext()) {

                Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

                jsonGenerator.writeFieldName(rootNodeEntry.getKey());

                if (SchemaTypeMapping.getSchemaTypeInstance(rootNodeEntry.getKey()) instanceof UserExtensionSchema) {

                    serializeUserExtension(rootNodeEntry, mapper, user, jsonGenerator);

                } else {

                    jsonGenerator.writeObject(rootNodeEntry.getValue());
                }
            }

            jsonGenerator.writeEndObject();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unexpected processing error; please check the input parameters.");
        }
    }

    protected void serializeUserExtension(Map.Entry<String, JsonNode> rootNodeEntry, ObjectMapper mapper, User user, JsonGenerator jsonGenerator) throws Exception {

        AttributeService attributeService = AttributeService.instance();
        Extension extension = user.getExtension(rootNodeEntry.getKey());

        Map<String, Object> list = new HashMap<String, Object>();
        for (Map.Entry<String, Extension.Field> extEntry : extension.getFields().entrySet()) {

            GluuAttribute gluuAttribute = attributeService.getAttributeByName(extEntry.getKey());
            GluuAttributeDataType attributeDataType = gluuAttribute.getDataType();

            if ((gluuAttribute.getOxMultivaluedAttribute() != null) && gluuAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {

                if (attributeDataType.equals(GluuAttributeDataType.STRING) || attributeDataType.equals(GluuAttributeDataType.PHOTO)) {

                    List<String> stringList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), String[].class));
                    list.put(extEntry.getKey(), stringList);

                } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {

                    List<Date> dateList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), Date[].class));
                    List<String> stringList = new ArrayList<String>();
                    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
                    for (Date date : dateList) {
                        String dateString = dateTimeFormatter.print(date.getTime());
                        stringList.add(dateString);
                    }
                    list.put(extEntry.getKey(), stringList);

                } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {

                    List<BigDecimal> numberList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), BigDecimal[].class));
                    list.put(extEntry.getKey(), numberList);
                }

            } else {

                if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
                    list.put(extEntry.getKey(), dateTimeFormatter.print(new Long(extEntry.getValue().getValue())));
                } else {
                    list.put(extEntry.getKey(), extEntry.getValue().getValue());
                }
            }
        }

        jsonGenerator.writeObject(list);
    }
}
