/**
 * 
 */
package org.gluu.oxtrust.model;

import java.io.Serializable;

import lombok.Data;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;


@LdapEntry
@LdapObjectClass(values = { "top", "gluuPasswordResetRequest" })
public @Data class PasswordResetRequest extends Entry implements Serializable {

	private static final long serialVersionUID = -3360077330096416826L;
	@LdapAttribute
	private String oxGuid;
	@LdapAttribute
	private String personInum;
	@LdapAttribute
	private String creationDate;
}
