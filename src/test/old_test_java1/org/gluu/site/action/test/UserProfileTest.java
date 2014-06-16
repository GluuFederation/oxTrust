package org.gluu.site.action.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class UserProfileTest extends AbstractAuthorizationTest {
 
	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	@Test
	@Parameters(value = { "updateUserPropsKey" })
	public void testUpdateProfile(String updateUserPropsKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(updateUserPropsKey);
		updateProfileFacadeRequest(updateUserPropsKey);

	}

	private void updateProfileFacadeRequest(final String updateUserPropsKey) throws Exception {
		new FacesRequest("/profile/person/viewProfile.xhtml") {

			@Override
			protected void updateModelValues() throws Exception {
				assert !isSessionInvalid();
				invokeAction("#{userProfileAction.show}");
			}

			@Override
			protected void invokeApplication() {
				setValue("#{userProfileAction.person.mail}", getConf().getString((updateUserPropsKey + ".mail")));
				setValue("#{userProfileAction.person.displayName}", getConf().getString((updateUserPropsKey + ".displayName")));
				invokeMethod("#{userProfileAction.update}");
			}

			@Override
			protected void renderResponse() {
				assert getValue("#{userProfileAction.person.displayName}").equals(getConf().getString((updateUserPropsKey + ".displayName")));
			}
		}.run();

	}
}
