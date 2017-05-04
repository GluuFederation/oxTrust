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
import javax.inject.Named;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Loading strategy for the Group Core schema.
 *
 * @author Val Pecaoco
 */
@Named("groupCoreLoadingStrategy")
public class GroupCoreLoadingStrategy implements LoadingStrategy {

    @Inject
    private Logger log;

    @Override
    public SchemaType load(AppConfiguration applicationConfiguration, SchemaType schemaType) throws Exception {

        log.info(" load() ");

        Meta meta = new Meta();
        meta.setLocation(appConfiguration.getBaseEndpoint() + "/scim/v2/Schemas/" + schemaType.getId());
        meta.setResourceType("Schema");
        schemaType.setMeta(meta);

        // Use serializer to walk the class structure
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        SimpleModule groupCoreLoadingStrategyModule = new SimpleModule("GroupCoreLoadingStrategyModule", new Version(1, 0, 0, ""));
        SchemaTypeGroupSerializer serializer = new SchemaTypeGroupSerializer();
        serializer.setSchemaType(schemaType);
        groupCoreLoadingStrategyModule.addSerializer(Group.class, serializer);
        mapper.registerModule(groupCoreLoadingStrategyModule);

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
