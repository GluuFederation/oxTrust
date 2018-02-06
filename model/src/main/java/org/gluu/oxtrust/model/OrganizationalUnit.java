/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

@LdapEntry
@LdapObjectClass(values = { "top", "organizationalunit" })
public class OrganizationalUnit extends Entry implements Serializable {

	private static final long serialVersionUID = -1585717575485030550L;

	@LdapAttribute
	private String ou;

	public String getOu() {
		return ou;
	}
	public void setOu(String ou) {
		this.ou = ou;
	}

}
