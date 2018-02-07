/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

@LdapEntry
@LdapObjectClass(values = { "top", "gluuPasswordResetRequest" })
public class PasswordResetRequest extends Entry implements Serializable {

	private static final long serialVersionUID = -3360077330096416826L;
	@LdapAttribute
	private String oxGuid;
	@LdapAttribute
	private String personInum;
	@LdapAttribute
	private Date creationDate;

	public String getOxGuid() {
		return oxGuid;
	}

	public void setOxGuid(String oxGuid) {
		this.oxGuid = oxGuid;
	}

	public String getPersonInum() {
		return personInum;
	}

	public void setPersonInum(String personInum) {
		this.personInum = personInum;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		return String
				.format("PasswordResetRequest [oxGuid=%s, personInum=%s, creationDate=%s]",
						oxGuid, personInum, creationDate);
	}

}
