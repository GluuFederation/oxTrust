/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.jackson.custom;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.gluu.oxtrust.model.helper.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.OxMultivalued;
import org.xdi.service.cdi.util.CdiUtil;

/**
 * Custom serializer for the SCIM 2.0 User class.
 *
 * @author Val Pecaoco
 * @link User
 */
@Stateless
@Named
public class UserSerializer extends JsonSerializer<User> {

    private Logger log= LoggerFactory.getLogger(getClass());

    private AttributeService attributeService;

    protected String attributesArray;
    protected Set<String> attributes;

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
            throw new IOException(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    protected void serializeUserExtension(Map.Entry<String, JsonNode> rootNodeEntry, ObjectMapper mapper, User user, JsonGenerator jsonGenerator) throws Exception {
        Extension extension = user.getExtension(rootNodeEntry.getKey());

        Map<String, Object> list = new HashMap<String, Object>();
        attributeService= CdiUtil.bean(AttributeService.class);
        boolean enclosingWritten = false;
        for (Map.Entry<String, Extension.Field> extEntry : extension.getFields().entrySet()) {

            if (attributes != null && attributes.size() > 0) {

                for (String attribute : attributes) {

                    attribute = FilterUtil.stripScim2Schema(attribute);

                    if (extEntry.getKey().equalsIgnoreCase(attribute)) {

                        if (!enclosingWritten) {

                            jsonGenerator.writeFieldName(rootNodeEntry.getKey());
                            enclosingWritten = true;
                        }
                        break;
                    }
                }

            } else {
                if (!enclosingWritten) {
                    jsonGenerator.writeFieldName(rootNodeEntry.getKey());
                    enclosingWritten = true;
                }
            }

            if (enclosingWritten) {

                GluuAttribute gluuAttribute = attributeService.getAttributeByName(extEntry.getKey());
                GluuAttributeDataType attributeDataType = gluuAttribute.getDataType();

                if ((gluuAttribute.getOxMultivaluedAttribute() != null) && gluuAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {

                    if (attributeDataType.equals(GluuAttributeDataType.STRING) || attributeDataType.equals(GluuAttributeDataType.PHOTO)) {

                        List<String> stringList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), String[].class));
                        list.put(extEntry.getKey(), stringList);

                    } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                        //Take generalized time dates and convert back to ISO format, namely,
                        //the converse of org.gluu.oxtrust.service.scim2.jackson.custom.ExtensionDeserializer.deserialize()
                        List<String> strDates=Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), String[].class));
                        List<String> formattedStrDates = new ArrayList<String>();
                        for (String strDate : strDates){
                            formattedStrDates.add(DateUtil.generalizedToISOStringDate(strDate));
                        }
                        list.put(extEntry.getKey(), formattedStrDates);
                        /*
                        List<Date> dateList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), Date[].class));
                        List<String> stringList = new ArrayList<String>();
                        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
                        for (Date date : dateList) {
                            String dateString = dateTimeFormatter.print(date.getTime());
                            stringList.add(dateString);
                        }
                        list.put(extEntry.getKey(), stringList);
                        */
                    } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {

                        List<BigDecimal> numberList = Arrays.asList(mapper.readValue(extEntry.getValue().getValue(), BigDecimal[].class));
                        list.put(extEntry.getKey(), numberList);
                    }

                } else {
                    list.put(extEntry.getKey(), extEntry.getValue().getValue());
                }
            }
        }

        if (enclosingWritten) {
            jsonGenerator.writeObject(list);
        }
    }

    public void setAttributesArray(String attributesArray) {
        this.attributesArray = attributesArray;
    }
}
