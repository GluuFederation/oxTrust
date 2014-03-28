package org.gluu.oxtrust.model.scim;

/**
 * SCIM person Patch Phones
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
public class ScimPersonPhonesPatch extends ScimPersonPhones {
	private String operation;

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
