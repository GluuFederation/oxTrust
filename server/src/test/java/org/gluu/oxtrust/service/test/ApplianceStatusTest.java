/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.mock.JUnitSeamTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * User: Oleksiy Tataryn
 */
@RunWith(Arquillian.class)
public class ApplianceStatusTest extends ConfigurableTest {
//	 @Deployment
//	   public static Archive<?> createDeployment()
//	   {
//		 WebArchive web = ShrinkWrap.create(WebArchive.class, "test.war")
//                  .addClass(ApplianceStatusTest.class)
//                  .addPackage(ConfigurableTest.class.getPackage())
//                  .addAsResource(EmptyAsset.INSTANCE, "seam.properties")
//                  .setWebXML("web.xml");
//
//			web.delete("/WEB-INF/web.xml");
//			web.addAsWebInfResource("web.xml");
//
//			// TODO: Workaround
//			WebArchive web2 = ShrinkWrap.create(ZipImporter.class, "oxtrust.war").importFrom(new File("target/oxtrust-server.war"))
//					.as(WebArchive.class);
//
//			InputStream is = web2.get("/WEB-INF/components.xml").getAsset().openStream();
//			try {
//				String components = IOUtils.toString(is);
//				String marker = "<!-- Inum DB configuration -->";
//				int idx1 = components.indexOf(marker);
//				int idx2 = components.indexOf(marker, idx1 + 1);
//				components = components.substring(0, idx1 + marker.length()) + components.substring(idx2);
//				StringAsset componentsAsset = new StringAsset(components);
//				web.add(componentsAsset, "/WEB-INF/components.xml");
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			} finally {
//				IOUtils.closeQuietly(is);
//			}
//
//			//        web.addAsWebInfResource("in-container-components.xml", "components.xml");
//			return web;
//
//	   }

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
