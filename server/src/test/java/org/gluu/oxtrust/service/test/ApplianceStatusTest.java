/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import javax.inject.Inject;import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

import org.gluu.oxtrust.action.ApplianceStatusAction;
import org.gluu.oxtrust.action.Authenticator;
import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Oleksiy Tataryn
 */
public class ApplianceStatusTest extends BaseTest {

    @Inject
    private Identity identity;

    @Inject
    private Authenticator authenticator;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private ApplianceStatusAction applianceStatusAction;

    public void loginUuser(String userUid) {
        identity.getOauthData().setUserUid(userUid);
        
        boolean loggedIn = authenticator.authenticate();
        assertTrue(loggedIn, "User is not logged in");
    }

	@Test
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsApplianceStatus1(String userUid) {
	    loginUuser(userUid);

	    GluuAppliance appliance = applianceService.getAppliance();

		Date currentDateTime = new Date();
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsApplianceStatus1" })
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsApplianceStatus2(String userUid) {
        loginUuser(userUid);

		GluuAppliance appliance = applianceService.getAppliance();

		long currentTime = System.currentTimeMillis() - 50 * 1000;
		Date currentDateTime = new Date(currentTime);
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsApplianceStatus2" })
    @Parameters({ "test.login.user.admin.uid" })
	public void testIsApplianceStatus3(String userUid) {
        loginUuser(userUid);

		GluuAppliance appliance = applianceService.getAppliance();

		long currentTime = System.currentTimeMillis() - 101 * 1000;
		Date currentDateTime = new Date(currentTime);
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "FAIL");
	}

}
