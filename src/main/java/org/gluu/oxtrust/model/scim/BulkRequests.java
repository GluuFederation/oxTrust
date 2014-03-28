package org.gluu.oxtrust.model.scim;

/**
 * SCIM bulk request
 * 
 * @author Reda Zerrad Date: 04.17.2012
 */

public class BulkRequests {
	private String bulkId;
	private String version;
	private String method;
	// private String bulkId;
	// private String version;
	private String path;
	private String location;
	private ScimData data;

	public BulkRequests() {
		bulkId = "";
		version = "";
		method = "";
		path = "";
		location = "";
		data = new ScimData();
	}

	public String getBulkId() {
		return this.bulkId;
	}

	public void setBulkId(String bulkId) {
		this.bulkId = bulkId;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMethod() {

		return this.method;
	}

	public void setMethod(String method) {

		this.method = method;
	}

	public String getPath() {

		return this.path;
	}

	public void setPath(String path) {

		this.path = path;
	}

	public String getLocation() {

		return this.location;
	}

	public void setLocation(String location) {

		this.location = location;
	}

	public ScimData getData() {

		return this.data;
	}

	public void setData(ScimData data) {

		this.data = data;
	}

}
