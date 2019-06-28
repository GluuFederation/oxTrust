/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.conf;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DN;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.JsonObject;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass(value = "oxApplicationConfiguration")
public class LdapAppConfiguration {

	private static final long serialVersionUID = 1847361642302974184L;

	@DN
    private String dn;

    @AttributeName(name = "oxRevision")
    private long revision;

    @JsonObject
	@AttributeName(name = "oxConfApplication")
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
