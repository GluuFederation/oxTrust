/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import javax.inject.Inject;

import org.gluu.oxtrust.action.ConfigurationStatusAction;
import org.gluu.oxtrust.action.Authenticator;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Oleksiy Tataryn
 */
public class ConfigurationStatusTest extends BaseTest {

    @Inject
    private Identity identity;

    @Inject
    private Authenticator authenticator;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ConfigurationStatusAction configurationStatusAction;

    public void loginUuser(String userUid) {
        identity.getOauthData().setUserUid(userUid);
        identity.getOauthData().setIdToken("dummy_id_token");
        
        String loggedIn = authenticator.authenticate();
        assertEquals(loggedIn, OxTrustConstants.RESULT_SUCCESS, "User is not logged in");
        assertTrue(identity.isLoggedIn(), "User is not logged in");
    }

	@Test
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsConfigurationStatus1(String userUid) {
	    loginUuser(userUid);

	    GluuConfiguration configuration = configurationService.getConfiguration();

		Date currentDateTime = new Date();
		configuration.setLastUpdate(currentDateTime);

		configurationService.updateConfiguration(configuration);
		assertEquals(configurationStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(configurationStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsConfigurationStatus1" })
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsConfigurationStatus2(String userUid) {
        loginUuser(userUid);

		GluuConfiguration configuration = configurationService.getConfiguration();

		long currentTime = System.currentTimeMillis() - 50 * 1000;
		Date currentDateTime = new Date(currentTime);
		configuration.setLastUpdate(currentDateTime);

		configurationService.updateConfiguration(configuration);
		assertEquals(configurationStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(configurationStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsConfigurationStatus2" })
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsConfigurationStatus3(String userUid) {
        loginUuser(userUid);

		GluuConfiguration configuration = configurationService.getConfiguration();

		long currentTime = System.currentTimeMillis() - 101 * 1000;
		Date currentDateTime = new Date(currentTime);
		configuration.setLastUpdate(currentDateTime);

		configurationService.updateConfiguration(configuration);
		assertEquals(configurationStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(configurationStatusAction.getHealth(), "FAIL");
	}

}
