/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.UUID;

import org.gluu.oxtrust.security.OauthData;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.oxauth.client.EndSessionClient;
import org.xdi.oxauth.client.EndSessionRequest;
import org.xdi.oxauth.client.EndSessionResponse;
import org.xdi.util.StringHelper;

@Scope(ScopeType.SESSION)
@Name("authenticationSessionService")
@AutoCreate()
public class AuthenticationSessionService {

	@Logger
	private Log log;
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

    @Destroy
    public void sessionDestroyed() {
    	OauthData oauthData = (OauthData) Component.getInstance(OauthData.class, false);
    	if ((oauthData == null) || StringHelper.isEmpty(oauthData.getSessionId())) {
    		return;
    	}

    	String userUid = oauthData.getUserUid();
    	log.debug("Calling oxAuth logout method at the end of HTTP session. User: '{0}'", userUid);
    	try {
            String endSessionState = UUID.randomUUID().toString();

            EndSessionRequest endSessionRequest = new EndSessionRequest(oauthData.getIdToken(), applicationConfiguration.getLogoutRedirectUrl(), endSessionState);
            endSessionRequest.setSessionId(oauthData.getSessionId());

            EndSessionClient endSessionClient = new EndSessionClient(applicationConfiguration.getOxAuthEndSessionUrl());
            endSessionClient.setRequest(endSessionRequest);
            EndSessionResponse endSessionResponse = endSessionClient.exec();
 
            if (endSessionResponse.getStatus() != 302) {
    	    	log.error("Invalid response code at oxAuth logout. User: '{0}'", userUid);
            }
		} catch (Exception ex) {
	    	log.error("Exception happened at oxAuth logout. User: '{0}'", ex, userUid);
		}
    }

}
