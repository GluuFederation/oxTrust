package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person MetaData
 * 
 * @author Reda Zerrad Date: 04.23.2012
 */
@JsonPropertyOrder({ "created", "lastModified", "version", "location" })
@XmlType(propOrder = { "created", "lastModified", "version", "location" })
public class PersonMeta {

	private String created;
	private String lastModified;
	private String version;
	private String location;

	public PersonMeta() {
		created = "";
		lastModified = "";
		version = "";
		location = "";
	}

	public String getCreated() {
		return this.created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
