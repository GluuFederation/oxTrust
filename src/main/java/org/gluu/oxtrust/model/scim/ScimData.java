package org.gluu.oxtrust.model.scim;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * SCIM Bulk Data
 * 
 * @author Reda Zerrad Date: 04.26.2012
 */
public class ScimData extends ScimPerson {

	private String id;
	private List<ScimGroupMembers> members;

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElementWrapper(name = "members")
	@XmlElement(name = "member")
	public List<ScimGroupMembers> getMembers() {
		return members;
	}

	public void setMembers(List<ScimGroupMembers> members) {
		this.members = members;
	}
}
