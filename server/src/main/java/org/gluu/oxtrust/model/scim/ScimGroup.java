/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

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

	@JsonProperty("schemas")
	private List<String> schemas;

	@LdapAttribute(name = "inum")
	private String id;

	@LdapAttribute(name = "displayName")
	private String displayName;

	@LdapAttribute(name = "member")
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
