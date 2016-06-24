/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.io.Serializable;
import java.util.*;

/**
 * SCIM Bulk responses
 * 
 * @author Rahat Ali Date: 05.08.2015
 */
public class BulkResponse implements Serializable {

	private Set<String> schemas = new HashSet<String>();
	private List<BulkOperation> operations = new LinkedList<BulkOperation>();

	public BulkResponse() {
		schemas.add(Constants.BULK_RESPONSE_SCHEMA_ID);
	}

	public Set<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(Set<String> schemas) {
		this.schemas = schemas;
	}

	public List<BulkOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<BulkOperation> operations) {
		this.operations = operations;
	}
}
