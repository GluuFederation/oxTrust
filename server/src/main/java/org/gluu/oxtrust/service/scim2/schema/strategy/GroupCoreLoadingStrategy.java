/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema.strategy;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.MemberRef;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeGroupSerializer;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Loading strategy for the Group Core schema.
 *
 * @author Val Pecaoco
 */
@Name("groupCoreLoadingStrategy")
public class GroupCoreLoadingStrategy implements LoadingStrategy {

    @Logger
    private static Log log;

    @Override
    public SchemaType load(ApplicationConfiguration applicationConfiguration, SchemaType schemaType) throws Exception {

        log.info(" load() ");

        Meta meta = new Meta();
        meta.setLocation(applicationConfiguration.getBaseEndpoint() + "/scim/v2/Schemas/" + schemaType.getId());
        meta.setResourceType("Schema");
        schemaType.setMeta(meta);

        // Use serializer to walk the class structure
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        SimpleModule groupCoreLoadingStrategyFilterModule = new SimpleModule("GroupCoreLoadingStrategyFilterModule", new Version(1, 0, 0, ""));
        SchemaTypeGroupSerializer serializer = new SchemaTypeGroupSerializer();
        serializer.setSchemaType(schemaType);
        groupCoreLoadingStrategyFilterModule.addSerializer(Group.class, serializer);
        mapper.registerModule(groupCoreLoadingStrategyFilterModule);

        mapper.writeValueAsString(createDummyGroup());

        return serializer.getSchemaType();
    }

    private Group createDummyGroup() {

        Group group = new Group();

        group.setId("");
        group.setExternalId("");

        group.setDisplayName("");

        Set<MemberRef> members = new HashSet<MemberRef>();
        MemberRef memberRef = new MemberRef();
        memberRef.setOperation("");
        memberRef.setPrimary(false);
        memberRef.setValue("test");
        memberRef.setDisplay("");
        memberRef.setType(MemberRef.Type.USER);
        memberRef.setReference("");
        members.add(memberRef);
        group.setMembers(members);

        return group;
    }
}
