/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.service.FacesService;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.resteasy.client.ClientRequest;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.StringHelper;

/**
 * Provides OP/RP-initiated logout functionality
 * 
 * @author Yuriy Movchan Date: 021/25/2019
 */
@Named("logoutAction")
@RequestScoped
public class LogoutAction implements Serializable {

    private static final long serialVersionUID = -1887682170119210113L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

    @Inject
    private FacesService facesService;

    @Inject
    private OpenIdService openIdService;

    @Inject
    private AppConfiguration appConfiguration;

	public void processLogout() throws Exception {
	    opLogout();

        identity.logout();
	}

	public String postLogout() {
        identity.logout();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	protected void opLogout() throws Exception {
		boolean requireOpLogout = isRequireOpLogout();
		if (!requireOpLogout) {
		    return;
		}

		OauthData oauthData = identity.getOauthData();

		ClientRequest clientRequest = new ClientRequest(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());

		clientRequest.queryParameter(OxTrustConstants.OXAUTH_SESSION_STATE, oauthData.getSessionState());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_ID_TOKEN_HINT, oauthData.getIdToken());
		clientRequest.queryParameter(OxTrustConstants.OXAUTH_POST_LOGOUT_REDIRECT_URI,
				appConfiguration.getLogoutRedirectUrl());

		// Clean up OAuth token
		oauthData.setUserUid(null);
		oauthData.setIdToken(null);
		oauthData.setSessionState(null);
		oauthData = null;

		facesService.redirectToExternalURL(clientRequest.getUri());
	}

	private boolean isRequireOpLogout() {
        OauthData oauthData = identity.getOauthData();
		if (StringHelper.isEmpty(oauthData.getUserUid())) {
			return false;
		}

		return true;
    }

}
