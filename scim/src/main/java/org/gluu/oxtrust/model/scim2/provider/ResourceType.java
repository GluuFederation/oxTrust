package org.gluu.oxtrust.model.scim2.provider;

import java.util.HashSet;
import java.util.Set;

import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.Resource;

public class ResourceType extends Resource{
	private String id;
	private String name;
	private String endpoint;
	private String description;
	private String schema;
	
	public ResourceType(){
		Meta userMeta = new Meta();
    	userMeta.setResourceType("ResourceType");
    	setMeta(userMeta);
    	Set<String> userSchemas = new HashSet<String>();
    	userSchemas.add("urn:ietf:params:scim:schemas:core:2.0:ResourceType");    	
		setSchemas(userSchemas );
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
	
	
}
