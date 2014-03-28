package org.gluu.oxtrust.model.scim;

/**
 * SCIM response Status
 * 
 * @author Reda Zerrad Date: 04.19.2012
 */

public class BulkResponseStatus {

	private String code;
	private String description;

	public BulkResponseStatus() {
		code = "";
		description = "";
	}

	public String getCode() {

		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {

		return this.description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

}
