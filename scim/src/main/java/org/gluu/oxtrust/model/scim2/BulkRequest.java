/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.io.Serializable;
import java.util.*;

/**
 * SCIM 2.0 bulk request
 * 
 * @author Rahat Ali Date: 05.08.2015
 */
public class BulkRequest implements Serializable {

	private Integer failOnErrors;

	private Set<String> schemas = new HashSet<String>();
	private List<BulkOperation> operations = new LinkedList<BulkOperation>();
	
	public BulkRequest() {
		schemas.add(Constants.BULK_REQUEST_SCHEMA_ID);
	}

	public Set<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(Set<String> schemas) {
		this.schemas = schemas;
	}

	public Integer getFailOnErrors() {
		return failOnErrors;
	}

	public void setFailOnErrors(Integer failOnErrors) {
		this.failOnErrors = failOnErrors;
	}

	public List<BulkOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<BulkOperation> operations) {
		this.operations = operations;
	}
}
