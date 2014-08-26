package org.gluu.oxtrust.model.scim;

import java.util.List;

/**
 * SCIM person Patch Meta patch
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
public class PersonMetaPatch extends PersonMeta {
	private List<String> attributes;

	public List<String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
}
