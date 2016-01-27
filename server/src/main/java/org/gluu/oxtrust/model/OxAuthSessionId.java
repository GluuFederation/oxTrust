package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;

@LdapEntry(sortBy = { "uniqueIdentifier" })
@LdapObjectClass(values = { "top", "oxAuthSessionId" })
public class OxAuthSessionId extends Entry implements Serializable {

	private static final long serialVersionUID = -4317830415164467231L;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String uniqueIdentifier;

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
}
