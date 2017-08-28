/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema.strategy;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.schema.AttributeHolder;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.service.cdi.util.CdiUtil;

/**
 * Loading strategy for the User Extension schema.
 *
 * @author Val Pecaoco
 */
@Named
public class UserExtensionLoadingStrategy implements LoadingStrategy {

    private Logger log= LoggerFactory.getLogger(getClass());

    private AttributeService attributeService;

    @Override
    public SchemaType load(AppConfiguration appConfiguration, SchemaType schemaType) throws Exception {
        log.info(" load() ");

        Meta meta = new Meta();
        meta.setLocation(appConfiguration.getBaseEndpoint() + "/scim/v2/Schemas/" + schemaType.getId());
        meta.setResourceType("Schema");
        schemaType.setMeta(meta);

        attributeService= CdiUtil.bean(AttributeService.class);

        // List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributesImpl(attributeService.getCustomAttributes());
        List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributes();

        List<AttributeHolder> attributeHolders = new ArrayList<AttributeHolder>();

        for (GluuAttribute scimCustomAttribute : scimCustomAttributes) {

            AttributeHolder attributeHolder = new AttributeHolder();
            log.debug("Custom attribute name {}", scimCustomAttribute.getName());
            attributeHolder.setName(scimCustomAttribute.getName());

            if (scimCustomAttribute.getDataType() != null) {

                String typeStr = "";
                GluuAttributeDataType attributeDataType = scimCustomAttribute.getDataType();

                if (attributeDataType.equals(GluuAttributeDataType.STRING)) {
                    typeStr = "string";
                } else if (attributeDataType.equals(GluuAttributeDataType.PHOTO)) {
                    typeStr = "reference";
                    attributeHolder.getReferenceTypes().add("external");
                } else if (attributeDataType.equals(GluuAttributeDataType.DATE)) {
                    typeStr = "dateTime";
                } else if (attributeDataType.equals(GluuAttributeDataType.NUMERIC)) {
                    typeStr = "decimal";
                } else {
                    log.info(" NO MATCH: scimCustomAttribute.getDataType().getDisplayName() = " + scimCustomAttribute.getDataType().getDisplayName());
                    typeStr = "string";
                }
                log.debug("of type {}", typeStr);
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
