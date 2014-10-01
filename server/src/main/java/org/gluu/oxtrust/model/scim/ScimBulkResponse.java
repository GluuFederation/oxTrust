/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM Bulk response
 * 
 * @author Reda Zerrad Date: 04.19.2012
 */
@XmlRootElement(name = "Bulk")
@XmlAccessorType(XmlAccessType.NONE)
@JsonPropertyOrder({ "schemas", "Operations" })
public class ScimBulkResponse {

	private List<String> schemas;
	@JsonProperty
	private List<BulkResponses> Operations;

	public List<String> getSchemas() {

		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	@XmlElementWrapper(name = "Operations")
	@XmlElement(name = "operation")
	@JsonIgnore
	public List<BulkResponses> getOperations() {

		return this.Operations;
	}

	public void setOperations(List<BulkResponses> Operations) {
		this.Operations = Operations;
	}

}
