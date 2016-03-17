/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.ArrayList;
import java.util.List;

/**
 * SCIM 2.0 bulk request
 * 
 * @author Rahat Ali Date: 05.08.2015
 */

public class BulkRequest {
	private List<String> schemas;
	private Integer failOnErrors; 
	private List<BulkOperation> operations;
	
	public BulkRequest() {
		schemas = new ArrayList<String>();
		schemas.add("urn:ietf:params:scim:api:messages:2.0:BulkRequest");
		operations = new ArrayList<BulkOperation>();
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
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
