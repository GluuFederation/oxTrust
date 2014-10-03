/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import lombok.Data;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;

@LdapEntry
@LdapObjectClass(values = { "top", "organizationalunit" })
public @Data class OrganizationalUnit extends Entry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1585717575485030550L;
	@LdapAttribute
	private String ou;

}
