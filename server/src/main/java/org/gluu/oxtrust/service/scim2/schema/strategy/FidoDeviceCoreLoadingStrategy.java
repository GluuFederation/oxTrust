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
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeFidoDeviceSerializer;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;

/**
 * @author Val Pecaoco
 */
@Named("fidoDeviceCoreLoadingStrategy")
public class FidoDeviceCoreLoadingStrategy implements LoadingStrategy {

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
		SimpleModule userCoreLoadingStrategyModule = new SimpleModule("FidoDeviceCoreLoadingStrategyModule", new Version(1, 0, 0, ""));
		SchemaTypeFidoDeviceSerializer serializer = new SchemaTypeFidoDeviceSerializer();
		serializer.setSchemaType(schemaType);
		userCoreLoadingStrategyModule.addSerializer(FidoDevice.class, serializer);
		mapper.registerModule(userCoreLoadingStrategyModule);

		mapper.writeValueAsString(createDummyFidoDevice());

		return serializer.getSchemaType();
	}

	private FidoDevice createDummyFidoDevice() {

		FidoDevice fidoDevice = new FidoDevice();

		fidoDevice.setId("");
		fidoDevice.setCreationDate("");
		fidoDevice.setApplication("");
		fidoDevice.setCounter("");
		fidoDevice.setDeviceData("");
		fidoDevice.setDeviceHashCode("");
		fidoDevice.setDeviceKeyHandle("");
		fidoDevice.setDeviceRegistrationConf("");
		fidoDevice.setLastAccessTime("");
		fidoDevice.setStatus("");
		fidoDevice.setDisplayName("");
		fidoDevice.setDescription("");

		return fidoDevice;
	}
}
