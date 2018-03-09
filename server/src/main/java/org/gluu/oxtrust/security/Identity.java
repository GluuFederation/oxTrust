package org.gluu.oxtrust.security;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;

@SessionScoped
@Named
public class Identity extends org.xdi.model.security.Identity {

	private static final long serialVersionUID = 2751659008033189259L;

	private OauthData oauthData;
	private GluuCustomPerson user;
	private Map<String, Object> sessionMap;

	private String savedRequestUri;
	
	@PostConstruct
	public void create() {
		super.create();
		this.sessionMap = new HashMap<String, Object>();
		this.oauthData = new OauthData();
	}

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

	public Map<String, Object> getSessionMap() {
		return sessionMap;
	}

    public String getSavedRequestUri() {
        return savedRequestUri;
    }

    public void setSavedRequestUri(String savedRequestUri) {
        this.savedRequestUri = savedRequestUri;
    }

}
