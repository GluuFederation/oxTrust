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
import javax.xml.bind.annotation.XmlType;

/**
 * User: Dejan Maric
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groups")
public class ScimUserGroups {

	private List<MultiValuedAttribute> group;

	public List<MultiValuedAttribute> getGroup() {
		if (group == null) {
			group = new ArrayList<MultiValuedAttribute>();
		}
		return group;
	}

	public void setGroup(List<MultiValuedAttribute> group) {
		this.group = group;
	}

	public ScimUserGroups() {

		group = new ArrayList<MultiValuedAttribute>();
	}
}
