/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.UUID;

import org.gluu.oxtrust.security.OauthData;
import org.jboss.seam.Component;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import org.jboss.seam.annotations.Destroy;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.EndSessionClient;
import org.xdi.oxauth.client.EndSessionRequest;
import org.xdi.oxauth.client.EndSessionResponse;
import org.xdi.util.StringHelper;

@Scope(ScopeType.SESSION)
@Named("authenticationSessionService")
public class AuthenticationSessionService {

	@Inject
	private Logger log;

	@Inject
	private OpenIdService openIdService;
	
	@Inject
	private AppConfiguration appConfiguration;

    @Destroy
    public void sessionDestroyed() {
    	OauthData oauthData = (OauthData) Component.getInstance(OauthData.class, false);
    	if ((oauthData == null) || StringHelper.isEmpty(oauthData.getSessionState())) {
    		return;
    	}

    	String userUid = oauthData.getUserUid();
    	log.debug("Calling oxAuth logout method at the end of HTTP session. User: '{0}'", userUid);
    	try {
            String endSessionState = UUID.randomUUID().toString();

            EndSessionRequest endSessionRequest = new EndSessionRequest(oauthData.getIdToken(), appConfiguration.getLogoutRedirectUrl(), endSessionState);
            endSessionRequest.setSessionState(oauthData.getSessionState());

            EndSessionClient endSessionClient = new EndSessionClient(openIdService.getOpenIdConfiguration().getEndSessionEndpoint());
            endSessionClient.setRequest(endSessionRequest);
            EndSessionResponse endSessionResponse = endSessionClient.exec();
 
            if ((endSessionResponse == null) || (endSessionResponse.getStatus() != 302)) {
    	    	log.error("Invalid response code at oxAuth logout. User: '{0}'", userUid);
            }
		} catch (Exception ex) {
	    	log.error("Exception happened at oxAuth logout. User: '{0}'", ex, userUid);
		}
    }

}
