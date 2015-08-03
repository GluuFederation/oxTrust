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
import java.util.ArrayList;
import java.util.List;

/**
 * SCIM Bulk operation
 * 
 * @author Reda Zerrad Date: 04.18.2012
 */

@XmlRootElement(name = "Bulk")
@XmlAccessorType(XmlAccessType.NONE)
@JsonPropertyOrder({ "schemas", "Operations" })
public class BulkOperation {

	private List<String> schemas;
	@JsonProperty
	private List<BulkRequests> Operations;

	public BulkOperation() {
		schemas = new ArrayList<String>();
		Operations = new ArrayList<BulkRequests>();
	}

	public List<String> getSchemas() {

		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	@JsonIgnore
	@XmlElementWrapper(name = "Operations")
	@XmlElement(name = "operation")
	public List<BulkRequests> getOperations() {

		return this.Operations;
	}

	public void setOperations(List<BulkRequests> Operations) {
		this.Operations = Operations;
	}
}
