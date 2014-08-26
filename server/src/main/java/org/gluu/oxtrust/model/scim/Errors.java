package org.gluu.oxtrust.model.scim;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "errors")
public class Errors {

	private List<Error> errors = new ArrayList<Error>();

	public Errors() {
		// empty constructor
	}

	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Errors [errors=");
		for (Error error : errors) {
			sb.append(error.getDescription());
		}
		sb.append("]");
		return sb.toString();
	}

}
