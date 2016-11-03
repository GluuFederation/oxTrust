package org.gluu.oxtrust.model.scim2;


public class Operation {
	private String operationName;
	private String path;
	private User value;
	
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public User getValue() {
		return value;
	}
	public void setValue(User value) {
		this.value = value;
	}
	

}
