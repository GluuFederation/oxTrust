/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.*;
import java.util.List;

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
	private List<BulkResponse> Operations;

	public List<String> getSchemas() {

		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	@XmlElementWrapper(name = "Operations")
	@XmlElement(name = "operation")
	@JsonIgnore
	public List<BulkResponse> getOperations() {

		return this.Operations;
	}

	public void setOperations(List<BulkResponse> Operations) {
		this.Operations = Operations;
	}

}
