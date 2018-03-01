/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class Error {

	@XmlElement(name = "description")
	protected String description;

	@XmlElement(name = "code")
	protected int code;

	@XmlElement(name = "uri")
	protected String uri;

	public Error() {
		// empty constructor
	}

	public Error(String description, int code, String uri) {
		this.description = description;
		this.code = code;
		this.uri = uri;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String value) {
		this.uri = value;
	}
}
