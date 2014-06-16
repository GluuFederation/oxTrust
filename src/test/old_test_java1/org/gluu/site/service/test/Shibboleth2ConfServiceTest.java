package org.gluu.site.service.test;

import org.gluu.site.action.test.AbstractAuthorizationTest;
import org.gluu.site.ldap.service.Shibboleth2ConfService;
import org.gluu.site.ldap.service.TrustService;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test class for AttributeService
 *
 * @author Yuriy Movchan Date: 11.15.2010
 */
public class Shibboleth2ConfServiceTest extends AbstractAuthorizationTest {

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
	@Parameters(value = "userKey")
	public void testShibboleth2ConfFilesGeneration(String userKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(userKey);
		new FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				// Generate configuration files
				Shibboleth2ConfService.instance().generateConfigurationFiles(TrustService.instance().getAllActiveTrustRelationships());
			}
		}.run();

		logoutUserFacesRequest();
	}

}
