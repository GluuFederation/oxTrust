/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Dejan Maric Date: 4.4.12.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "addresses")
public class ScimAddresses {

	private List<ScimAddress> address;

	@XmlElement(name = "address")
	public List<ScimAddress> getAddress() {
		if (address == null) {
			address = new ArrayList<ScimAddress>();
		}
		return address;
	}

	public void setAddress(List<ScimAddress> address) {
		this.address = address;
	}

	public ScimAddresses() {
		address = new ArrayList<ScimAddress>();
	}
}
