package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeconstructedTrustRelationship {

	private List<GluuCustomAttribute> releasedAttributes = new ArrayList<GluuCustomAttribute>();
	private String entityId;
	private String name;

	public void setReleasedCustomAttributes(List<GluuCustomAttribute> releasedAttributes) {
		this.releasedAttributes = releasedAttributes;
	}

	public List<GluuCustomAttribute> getReleasedCustomAttributes() {
		return releasedAttributes;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
