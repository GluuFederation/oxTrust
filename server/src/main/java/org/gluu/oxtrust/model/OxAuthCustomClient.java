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

@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxAuthClient"})
public class OxAuthCustomClient extends CustomEntry implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -3319774915823259905L;

	@LdapAttributesList(name = "name", value = "values", sortByName = true, attributesConfiguration = {
			@LdapAttribute(name = "iname", ignoreDuringUpdate = true), @LdapAttribute(name = "inum", ignoreDuringUpdate = true),
			@LdapAttribute(name = "userPassword", ignoreDuringRead = true) })
	private List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();

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

	public String getIname() {
		return getAttribute("iname");
	}

	public void setIname(String value) {
		setAttribute("iname", value);
	}

	public String getDisplayName() {
		return getAttribute("displayName");
	}

	public void setDisplayName(String value) {
		setAttribute("displayName", value);
	}

	public String getOxAuthClientSecret() {
		return getAttribute("oxAuthClientSecret");
	}

	public void setOxAuthClientSecret(String value) {
		setAttribute("oxAuthClientSecret", value);
	}

	public String getUserPassword() {
		return getAttribute("userPassword");
	}

	public void setUserPassword(String value) {
		setAttribute("userPassword", value);
	}

}
