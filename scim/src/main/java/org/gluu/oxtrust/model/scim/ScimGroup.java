/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * SCIM Group
 * 
 * @author Reda Zerrad Date: 05.03.2012
 */
@XmlRootElement(name = "Group")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "schemas", "id", "displayName", "members" })
@XmlType(propOrder = { "id", "displayName", "members" })
public class ScimGroup implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6956480059609846850L;
	private List<String> schemas;
	private String id;
	private String displayName;
	private List<ScimGroupMembers> members;

	@XmlTransient
	public List<String> getSchemas() {

		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {

		this.schemas = schemas;
	}

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@XmlElementWrapper(name = "members")
	@XmlElement(name = "member")
	public List<ScimGroupMembers> getMembers() {
		return members;
	}

	public void setMembers(List<ScimGroupMembers> members) {
		this.members = members;
	}

	public ScimGroup() {
		id = "";
		displayName = "";
		schemas = new ArrayList<String>();
		members = new ArrayList<ScimGroupMembers>();

	}
}
