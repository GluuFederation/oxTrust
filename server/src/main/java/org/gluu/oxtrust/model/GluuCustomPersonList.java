package org.gluu.oxtrust.model;

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
import org.gluu.oxtrust.model.scim.ScimPerson;

/**
 * SCIM persons list
 * 
 * @author Reda Zerrad Date: 04.18.2012
 */

@XmlRootElement(name = "Resources")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "totalResults", "schemas", "Resources" })
@XmlType(propOrder = { "totalResults", "Resources" })
public class GluuCustomPersonList implements Serializable {

	private static final long serialVersionUID = -1879582184398161112L;
	@XmlElement
	private long totalResults;
	@XmlTransient
	private List<String> schemas;

	@XmlElementWrapper(name = "Resources")
	@XmlElement(name = "Resource")
	@JsonProperty
	private List<ScimPerson> Resources;
	List<Person> personList = new ArrayList<Person>();

	public GluuCustomPersonList() {
		Resources = new ArrayList<ScimPerson>();
		schemas = new ArrayList<String>();
	}

	public long getTotalResults() {

		return this.totalResults;
	}

	public void setTotalResults(long totalResults) {

		this.totalResults = totalResults;
	}

	public List<String> getSchemas() {

		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;

	}

	public List<ScimPerson> getResources() {
		return Resources;
	}

	public void setResources(List<ScimPerson> Resources) {
		this.Resources = Resources;
	}

	public List<Person> getPersonList() {
		return personList;
	}

	public void setPersonList(List<Person> personList) {
		this.personList = personList;
	}

}
