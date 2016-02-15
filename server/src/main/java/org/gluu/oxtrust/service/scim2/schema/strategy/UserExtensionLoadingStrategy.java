/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema.strategy;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.schema.AttributeHolder;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Loading strategy for the User Extension schema.
 *
 * @author Val Pecaoco
 */
@Name("userExtensionLoadingStrategy")
public class UserExtensionLoadingStrategy implements LoadingStrategy {

    @Logger
    private static Log log;

    @Override
    public SchemaType load(SchemaType schemaType) throws Exception {

        log.info(" load() ");

        AttributeService attributeService = AttributeService.instance();

        List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributesImpl(attributeService.getCustomAttributes());

        List<AttributeHolder> attributeHolders = new ArrayList<AttributeHolder>();

        for (GluuAttribute scimCustomAttribute : scimCustomAttributes) {

            AttributeHolder attributeHolder = new AttributeHolder();

            attributeHolder.setName(scimCustomAttribute.getName());

            if (scimCustomAttribute.getDataType() != null) {
                String typeStr = scimCustomAttribute.getDataType().getValue();
                if (typeStr.equalsIgnoreCase("Text")) {
                    typeStr = "string";
                } else if (typeStr.equalsIgnoreCase("Photo")) {
                    typeStr = "reference";
                    attributeHolder.getReferenceTypes().add("external");
                } else if (typeStr.equalsIgnoreCase("Date")) {
                    typeStr = "dateTime";
                } else if (typeStr.equalsIgnoreCase("Numeric")) {
                    typeStr = "decimal";
                } else {
                    typeStr = "string";
                }
                attributeHolder.setType(typeStr);
            }

            attributeHolder.setDescription(scimCustomAttribute.getDescription());
            attributeHolder.setRequired(scimCustomAttribute.isRequred());

            if (scimCustomAttribute.getOxMultivaluedAttribute() != null) {
                Boolean multiValued = Boolean.parseBoolean(scimCustomAttribute.getOxMultivaluedAttribute().getValue());
                attributeHolder.setMultiValued(multiValued);
            }

            attributeHolders.add(attributeHolder);
        }

        UserExtensionSchema userExtensionSchema = (UserExtensionSchema) schemaType;
        userExtensionSchema.setAttributeHolders(attributeHolders);

        return userExtensionSchema;
    }
}
