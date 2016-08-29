/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2.schema.strategy.serializers;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.model.scim2.schema.AttributeHolder;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.model.scim2.schema.core.fido.FidoDeviceCoreSchema;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
@Name("schemaTypeFidoDeviceSerializer")
public class SchemaTypeFidoDeviceSerializer extends JsonSerializer<FidoDevice> {

	@Logger
	private static Log log;

	private SchemaType schemaType;

	private List<AttributeHolder> attributeHolders = new ArrayList<AttributeHolder>();

	@Override
	public void serialize(FidoDevice fidoDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		log.info(" serialize() ");

		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

			JsonNode rootNode = mapper.convertValue(fidoDevice, JsonNode.class);

			Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.getFields();

			while (iterator.hasNext()) {

				Map.Entry<String, JsonNode> rootNodeEntry = iterator.next();

				if (!rootNodeEntry.getKey().equalsIgnoreCase("meta") && !rootNodeEntry.getKey().equalsIgnoreCase("externalId")) {

					AttributeHolder attributeHolder = new AttributeHolder();
					attributeHolder.setName(rootNodeEntry.getKey());

					if (rootNodeEntry.getValue().isBoolean()) {
						attributeHolder.setType("boolean");
					} else {
						attributeHolder.setType("string");
					}

					if (rootNodeEntry.getKey().equalsIgnoreCase("userId")) {
						attributeHolder.setDescription("User ID that owns the device. Using this in a query filter is not supported.");
					} else if (rootNodeEntry.getKey().equalsIgnoreCase("schemas")) {
						attributeHolder.setDescription("schemas list");
					} else {
						attributeHolder.setDescription(rootNodeEntry.getKey());
					}

					if (rootNodeEntry.getKey().equalsIgnoreCase("id") || rootNodeEntry.getKey().equalsIgnoreCase("schemas") || rootNodeEntry.getKey().equalsIgnoreCase("userId")) {
						attributeHolder.setUniqueness("server");
						attributeHolder.setReturned("always");
						attributeHolder.setCaseExact(Boolean.TRUE);
					}

					if (rootNodeEntry.getKey().equalsIgnoreCase("displayName")) {
						attributeHolder.setReturned("always");
					}

					if (!rootNodeEntry.getKey().equalsIgnoreCase("displayName") && !rootNodeEntry.getKey().equalsIgnoreCase("description")) {
						attributeHolder.setMutability("readOnly");
					}

					attributeHolders.add(attributeHolder);
				}
			}

			FidoDeviceCoreSchema fidoDeviceCoreSchema = (FidoDeviceCoreSchema) schemaType;
			fidoDeviceCoreSchema.setAttributeHolders(attributeHolders);
			schemaType = fidoDeviceCoreSchema;

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Unexpected processing error; please check the FidoDevice class structure.");
		}
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(SchemaType schemaType) {
		this.schemaType = schemaType;
	}
}
