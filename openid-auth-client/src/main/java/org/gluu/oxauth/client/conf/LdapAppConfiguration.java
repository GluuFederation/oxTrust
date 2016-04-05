/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.conf;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapDN;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

@LdapEntry
@LdapObjectClass(values = {"top", "oxIdpConfiguration"})
public class LdapAppConfiguration {

	private static final long serialVersionUID = 1847361642302974184L;

	@LdapDN
    private String dn;

    @LdapAttribute(name = "oxRevision")
    private long revision;

    @LdapJsonObject
	@LdapAttribute(name = "oxConfApplication")
    private AppConfiguration application;

    public LdapAppConfiguration() {}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public AppConfiguration getApplication() {
		return application;
	}

	public void setApplication(AppConfiguration application) {
		this.application = application;
	}

}
