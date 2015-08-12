/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

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
