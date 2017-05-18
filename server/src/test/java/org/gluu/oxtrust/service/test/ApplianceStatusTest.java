/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import javax.inject.Inject;import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

import org.gluu.oxtrust.action.ApplianceStatusAction;
import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.testng.annotations.Test;

/**
 * User: Oleksiy Tataryn
 */
public class ApplianceStatusTest extends BaseTest {

	@Inject
	private ApplianceService applianceService;

	@Inject
	private ApplianceStatusAction applianceStatusAction;

	@Test
	public void testIsApplianceStatus1() throws Exception {
		GluuAppliance appliance = applianceService.getAppliance();

		Date currentDateTime = new Date();
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsApplianceStatus1" })
	public void testIsApplianceStatus2() throws Exception {
		GluuAppliance appliance = applianceService.getAppliance();

		long currentTime = System.currentTimeMillis() - 50 * 1000;
		Date currentDateTime = new Date(currentTime);
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "OK");
	}

	@Test(dependsOnMethods = { "testIsApplianceStatus2" })
	public void testIsApplianceStatus3() throws Exception {
		GluuAppliance appliance = applianceService.getAppliance();

		long currentTime = System.currentTimeMillis() - 101 * 1000;
		Date currentDateTime = new Date(currentTime);
		appliance.setLastUpdate(currentDateTime);

		applianceService.updateAppliance(appliance);
		assertEquals(applianceStatusAction.checkHealth(), OxTrustConstants.RESULT_SUCCESS);
		assertEquals(applianceStatusAction.getHealth(), "OK");
	}

}
