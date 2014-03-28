package org.gluu.oxtrust.model.scim;

/**
 * SCIM person Patch certificates
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
public class Scimx509CertificatesPatch extends Scimx509Certificates {
	private String operation;

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
