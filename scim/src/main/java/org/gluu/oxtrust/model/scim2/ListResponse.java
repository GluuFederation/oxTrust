/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM List Response
 * 
 * @author Rahat Ali Date: 05.08.2015
 */

@XmlRootElement(name = "Resources")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "totalResults", "startIndex","itemsPerPage","schemas", "Resources" })
@XmlType(propOrder = { "totalResults","startIndex","itemsPerPage", "Resources" })
public class ListResponse implements Serializable {

	private static final long serialVersionUID = 433980309301930837L;
	@XmlElement
	private int totalResults;
	@XmlElement
	private int startIndex;
	@XmlElement
	private int itemsPerPage;  
	@XmlTransient
	private List<String> schemas;

	@XmlElementWrapper(name = "Resources")
	@XmlElement(name = "Resource")
	//@JsonProperty
	@JsonIgnore
	private List<Resource> Resources;

	public ListResponse() {

		schemas = new ArrayList<String>();
		Resources = new ArrayList<Resource>();

	}

	public List<String> getSchemas() {
		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public List<Resource> getResources() {
		return this.Resources;
	}

	public void setResources(List<Resource> Resources) {
		this.Resources = Resources;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
}
