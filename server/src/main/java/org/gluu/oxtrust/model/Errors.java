/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "errors")
public class Errors {

	protected List<Error> error = new ArrayList<Error>();

	public Errors() {
		// empty constructor
	}

	public List<Error> getError() {
		return error;
	}

	public void setError(List<Error> error) {
		this.error = error;
	}

}
