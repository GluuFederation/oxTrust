package org.gluu.oxtrust.security;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SessionScoped
@Named
public class Identity extends org.xdi.model.security.Identity {

	private static final long serialVersionUID = 2751659008033189259L;

	private OauthData oauthData;

	public OauthData getOauthData() {
		return oauthData;
	}

	public void setOauthData(OauthData oauthData) {
		this.oauthData = oauthData;
	}

}
