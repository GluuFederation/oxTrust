package org.gluu.oxtrust.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditConfigLogDetails {
	
	private String user;
	private String objectName;
	private String property;
	private String oldValue;
	private String newValue;
	
	
	public AuditConfigLogDetails(String user, String objectName, String property, String oldValue,
			String newValue) {
		super();
		this.user = user;
		this.objectName = objectName;
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	@Override
	public String toString() {
		return "AuditConfigLogDetails [user=" + user + ", objectName=" + objectName
				+ ", property=" + property + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
	}
	

}
