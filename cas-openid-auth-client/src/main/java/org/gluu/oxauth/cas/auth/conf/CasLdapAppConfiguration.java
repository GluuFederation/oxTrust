package org.gluu.oxauth.cas.auth.conf;

import org.gluu.oxauth.client.conf.LdapAppConfiguration;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.JsonObject;

public class CasLdapAppConfiguration extends LdapAppConfiguration {

	private static final long serialVersionUID = -7301311833970330177L;

	@JsonObject
	@AttributeName(name = "oxConfApplication")
    private CasAppConfiguration application;

        @Override
	public CasAppConfiguration getApplication() {
		return application;
	}

	public void setApplication(CasAppConfiguration application) {
		this.application = application;
	}

}
