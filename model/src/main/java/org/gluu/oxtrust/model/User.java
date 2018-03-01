/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapAttributesList;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

/**
 * User
 * 
 * @author Yuriy Movchan Date: 06/10/2013
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "gluuPerson" })
public class User extends CustomEntry implements Serializable, Cloneable {

	private static final long serialVersionUID = -7779582184398161112L;

	@LdapAttributesList(name = "name", value = "values", sortByName = true, attributesConfiguration = {
			@LdapAttribute(name = "iname", ignoreDuringUpdate = true), @LdapAttribute(name = "inum", ignoreDuringUpdate = true),
			@LdapAttribute(name = "uid", ignoreDuringUpdate = false), @LdapAttribute(name = "userPassword", ignoreDuringRead = true) })
	protected List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();

	public List<GluuCustomAttribute> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public String getInum() {
		return getAttribute("inum");
	}

	public void setInum(String value) {
		setAttribute("inum", value);
	}

	public String getUid() {
		return getAttribute("uid");
	}

	public void setUid(String value) {
		setAttribute("uid", value);
	}

	public String getDisplayName() {
		return getAttribute("displayName");
	}

	public void setDisplayName(String value) {
		setAttribute("displayName", value);
	}
 
}
