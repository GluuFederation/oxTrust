/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.ArrayList;
import java.util.List;

/**
 * SCIM Bulk responses
 * 
 * @author Rahat Ali Date: 05.08.2015
 */

public class BulkResponse {

	private List<String> schemas;	
	private List<BulkOperation> Operations;		

	public BulkResponse() {
		schemas = new ArrayList<String>();
		schemas.add("urn:ietf:params:scim:api:messages:2.0:BulkResponse");
		Operations = new ArrayList<BulkOperation>();
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public List<BulkOperation> getOperations() {
		return Operations;
	}

	public void setOperations(List<BulkOperation> operations) {
		Operations = operations;
	}
	
}
