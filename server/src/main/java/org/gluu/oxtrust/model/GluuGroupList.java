/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

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
import org.gluu.oxtrust.model.scim.ScimGroup;

/**
 * SCIM Group list
 * 
 * @author Reda Zerrad Date: 04.13.2012
 */

@XmlRootElement(name = "Resources")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "totalResults", "schemas", "Resources" })
@XmlType(propOrder = { "totalResults", "Resources" })
public class GluuGroupList implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 433980309301930837L;
	@XmlElement
	private long totalResults;
	@XmlTransient
	private List<String> schemas;

	@XmlElementWrapper(name = "Resources")
	@XmlElement(name = "Resource")
	@JsonProperty
	private List<ScimGroup> Resources;

	public GluuGroupList() {

		schemas = new ArrayList<String>();
		Resources = new ArrayList<ScimGroup>();

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

	public List<ScimGroup> getResources() {
		return this.Resources;
	}

	public void setResources(List<ScimGroup> Resources) {
		this.Resources = Resources;
	}
}
