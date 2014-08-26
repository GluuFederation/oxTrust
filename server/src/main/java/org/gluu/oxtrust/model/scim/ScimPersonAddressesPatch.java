package org.gluu.oxtrust.model.scim;

/**
 * SCIM person Patch addresses
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
public class ScimPersonAddressesPatch extends ScimPersonAddresses {
	private String operation;

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
