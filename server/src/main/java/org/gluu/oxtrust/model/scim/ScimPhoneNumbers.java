/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: Dejan Maric
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "phoneNumbers")
public class ScimPhoneNumbers {

	private List<MultiValuedAttribute> phoneNumber;

	@XmlElement(name = "phoneNumber")
	public List<MultiValuedAttribute> getPhoneNumber() {
		if (phoneNumber == null) {
			phoneNumber = new ArrayList<MultiValuedAttribute>();
		}
		return phoneNumber;
	}

	public void setPhoneNumber(List<MultiValuedAttribute> phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public ScimPhoneNumbers() {

		phoneNumber = new ArrayList<MultiValuedAttribute>();
	}
}
