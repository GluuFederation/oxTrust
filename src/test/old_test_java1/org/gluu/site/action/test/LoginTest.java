package org.gluu.site.action.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @author Pankaj
 */
public class LoginTest extends AbstractAuthorizationTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	@Test
	@Parameters(value = "userPropsKey")
	public void testLogin(String userPropsKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(userPropsKey);
		logoutUserFacesRequest();
	}

}
