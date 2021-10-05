/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.security.OauthData;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;
import org.gluu.oxauth.client.EndSessionClient;
import org.gluu.oxauth.client.EndSessionRequest;
import org.gluu.oxauth.client.EndSessionResponse;

@SessionScoped
@Named
public class AuthenticationSessionService implements Serializable {

	private static final long serialVersionUID = 8569580900768794363L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

	@Inject
	private OpenIdService openIdService;
	
	@Inject
	private AppConfiguration appConfiguration;

    @PreDestroy
    public void sessionDestroyed() {
    	OauthData oauthData = identity.getOauthData();
    	if ((oauthData == null) || StringHelper.isEmpty(oauthData.getSessionState())) {
    		return;
    	}

    	String userUid = oauthData.getUserUid();
    	log.debug("Calling oxAuth logout method at the end of HTTP session. User: '{}'", userUid);
    	try {
            String endSessionState = UUID.randomUUID().toString();

            EndSessionRequest endSessionRequest = new EndSessionRequest(oauthData.getIdToken(), appConfiguration.getLogoutRedirectUrl(), endSessionState);
            endSessionRequest.setSid(oauthData.getSessionState());

            EndSessionClient endSessionClient = new EndSessionClient(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());
            endSessionClient.setRequest(endSessionRequest);
            EndSessionResponse endSessionResponse = endSessionClient.exec();
 
            if ((endSessionResponse == null) || (endSessionResponse.getStatus() != 302)) {
    	    	log.error("Invalid response code at oxAuth logout. User: '{}'", userUid);
            }
		} catch (Exception ex) {
	    	log.error("Exception happened at oxAuth logout. User: '{}'", userUid, ex);
		}
    }

}
