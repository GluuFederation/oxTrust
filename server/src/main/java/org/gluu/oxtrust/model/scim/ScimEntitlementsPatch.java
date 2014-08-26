package org.gluu.oxtrust.model.scim;

/**
 * SCIM person Patch Entitlements
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
public class ScimEntitlementsPatch extends ScimEntitlements {
	private String operation;

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
