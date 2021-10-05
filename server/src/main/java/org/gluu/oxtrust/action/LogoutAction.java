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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.service.FacesService;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.util.OxTrustConstants;

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

    public void processSsoLogout() throws Exception {
        identity.logout();
        identity.setWorkingParameter(OxTrustConstants.OXAUTH_SSO_SESSION_STATE, Boolean.TRUE);
    }

    public String postLogout() {
        identity.logout();
        return OxTrustConstants.RESULT_SUCCESS;
    }

    protected void opLogout() throws Exception {
        Object ssoSessionState = identity.getWorkingParameter(OxTrustConstants.OXAUTH_SSO_SESSION_STATE);
        if (Boolean.TRUE.equals(ssoSessionState)) {
            facesService.redirectToExternalURL(appConfiguration.getLogoutRedirectUrl());
            return;
        }
        OauthData oauthData = identity.getOauthData();
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());
        if (oauthData.getSessionState() != null) {
            target = target.queryParam(OxTrustConstants.OXAUTH_SESSION_STATE, oauthData.getSessionState());
        }
        if (appConfiguration.isPassIdTokenHintToLogoutRedirectUri() && oauthData.getIdToken() != null) {
            target = target.queryParam(OxTrustConstants.OXAUTH_ID_TOKEN_HINT, oauthData.getIdToken());
        }
        target = target.queryParam(OxTrustConstants.OXAUTH_POST_LOGOUT_REDIRECT_URI,
                appConfiguration.getLogoutRedirectUrl());
        facesService.redirectToExternalURL(target.getUri().toString());
    }

}
