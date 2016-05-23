/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import static org.testng.Assert.assertEquals;

import org.gluu.oxtrust.action.test.BaseComponentTest;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * User: Oleksiy Tataryn
 */
public class ApplianceStatusTest extends BaseComponentTest {

	@Override
	public void beforeClass() {
		
	}

	@Override
	public void afterClass() {
	}

	 @Test
	public void testIsApplianceStatus() throws Exception {
		new SeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime));
        		applianceService.updateAppliance(appliance);
                assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("OK");
            }
        }.run();
    
		new SeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime-50));
        		applianceService.updateAppliance(appliance);
                assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("OK");
            }
        }.run();
        
		new SeamTest.FacesRequest() {
            @Override
            public void invokeApplication() throws Exception {
            	ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
            	GluuAppliance appliance = applianceService.getAppliance();
        		int currentTime = (int) (System.currentTimeMillis() / 1000);
        		appliance.setLastUpdate(Integer.toString(currentTime-101));
        		applianceService.updateAppliance(appliance);
                assertEquals(invokeMethod("#{applianceStatusAction.checkHealth}"), OxTrustConstants.RESULT_SUCCESS);
            }
            
            @Override
            protected void renderResponse() {
            	assert getValue("#{applianceStatusAction.health}").equals("FAIL");
            }
        }.run();
    }

}
