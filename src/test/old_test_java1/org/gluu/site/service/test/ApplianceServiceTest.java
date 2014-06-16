package org.gluu.site.service.test;

import java.util.List;

import org.gluu.site.action.test.ConfigurableTest;
import org.gluu.site.ldap.service.ApplianceService;
import org.gluu.site.model.GluuAppliance;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for AttributeService
 * 
 * @author Yuriy Movchan Date: 10.14.2010
 */
public class ApplianceServiceTest extends ConfigurableTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	/**
	 * Test getting appliances list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAppliances() throws Exception {
		new FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				// Get attributes
				ApplianceService applianceService = (ApplianceService) getInstance("applianceService");
				List<GluuAppliance> appliances = applianceService.getAppliances();

				Assert.assertNotNull(appliances, "Failed to load appliances");
				Assert.assertTrue(appliances.size() > 0, "Failed to load appliances");
				System.out.println(appliances.get(0));
			}
		}.run();
	}

}
