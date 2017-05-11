package org.gluu.oxtrust.security;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;

@SessionScoped
@Named
public class Identity extends org.xdi.model.security.Identity {

	private static final long serialVersionUID = 2751659008033189259L;

	private OauthData oauthData;
	private GluuCustomPerson user;

	public OauthData getOauthData() {
		return oauthData;
	}

	public void setOauthData(OauthData oauthData) {
		this.oauthData = oauthData;
	}

	public GluuCustomPerson getUser() {
		return user;
	}

	public void setUser(GluuCustomPerson user) {
		this.user = user;
	}

}
