/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * User: Oleksiy Tataryn
 */
@RunWith(Arquillian.class)
public class ApplianceStatusTest extends ConfigurableTest {

	@Test
	public void testIsApplianceStatus() throws Exception {
		new JUnitSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime));
        		applianceService.updateAppliance(appliance);
                Assert.assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("OK");
            }
        }.run();
        
		new JUnitSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime-50));
        		applianceService.updateAppliance(appliance);
                Assert.assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("OK");
            }
        }.run();
        
		new JUnitSeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime-101));
        		applianceService.updateAppliance(appliance);
                Assert.assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("FAIL");
            }
        }.run();
    }

}
