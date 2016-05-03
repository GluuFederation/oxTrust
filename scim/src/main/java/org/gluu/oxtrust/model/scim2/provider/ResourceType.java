/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.Resource;
import org.gluu.oxtrust.model.scim2.schema.SchemaExtensionHolder;

public class ResourceType extends Resource {

	private String id;
	private String name;
	private String endpoint;
	private String description;
	private String schema;
	private List<SchemaExtensionHolder> schemaExtensions = new ArrayList<SchemaExtensionHolder>();
	
	public ResourceType(){
		Meta userMeta = new Meta();
    	userMeta.setResourceType("ResourceType");
    	setMeta(userMeta);
    	Set<String> userSchemas = new HashSet<String>();
    	userSchemas.add(Constants.RESOURCE_TYPE_SCHEMA_ID);
		setSchemas(userSchemas);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public List<SchemaExtensionHolder> getSchemaExtensions() {
		return schemaExtensions;
	}

	public void setSchemaExtensions(List<SchemaExtensionHolder> schemaExtensions) {
		this.schemaExtensions = schemaExtensions;
	}
}
