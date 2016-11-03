package org.gluu.oxtrust.model.scim2;

import java.util.List;

public class ScimPatchUser {	
	List <String> Schema;
	List <Operation> operatons;
	
	public List<String> getSchema() {
		return Schema;
	}
	public void setSchema(List<String> schema) {
		Schema = schema;
	}
	public List<Operation> getOperatons() {
		return operatons;
	}
	public void setOperatons(List<Operation> operatons) {
		this.operatons = operatons;
	}
	
}
