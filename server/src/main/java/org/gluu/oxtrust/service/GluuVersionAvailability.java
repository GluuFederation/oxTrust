package org.gluu.oxtrust.service;

public class GluuVersionAvailability {

	private boolean newVersionAvailable;

	private String version;

	public boolean isNewVersionAvailable() {
		return newVersionAvailable;
	}

	public void setNewVersionAvailable(boolean newVersionAvailable) {
		this.newVersionAvailable = newVersionAvailable;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
