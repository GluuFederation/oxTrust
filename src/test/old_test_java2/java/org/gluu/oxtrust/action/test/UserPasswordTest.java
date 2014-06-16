package org.gluu.oxtrust.action.test;

import org.gluu.oxtrust.util.Configuration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Dejan Maric
 */
public class UserPasswordTest extends AbstractAuthorizationTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

    @Test
    @Parameters({"userPropsKey", "passwordPropsKey"})
    public void testUserPasswordUpdate(String userPropsKey, String passwordPropsKey) throws Exception {
        loginAndCheckLoggedInFacesRequest(userPropsKey);
        failUpdateUsingWrongOldPassword(passwordPropsKey);
        updateUserPassword(passwordPropsKey);
    }

    private void failUpdateUsingWrongOldPassword(final String passwordPropsKey) throws Exception {
        new FacesRequest("admin/person/update"){
            @Override
            protected void updateModelValues(){
                setValue("#{updatePersonAction.inum}", getConf().getString(passwordPropsKey + ".inum"));
                invokeAction("#{updatePersonAction.update}");
                setValue("#{userPasswordAction.person}", getValue("#{updatePersonAction.person}"));

                setValue("#{userPasswordAction.oldPassword}", getConf().getString(passwordPropsKey + ".wrongOldPassword"));
                setValue("#{userPasswordAction.newPassword}", getConf().getString(passwordPropsKey + ".newPassword"));
                setValue("#{userPasswordAction.newPasswordConfirmation}", getConf().getString(passwordPropsKey + ".newPasswordConfirmation"));
            }

            @Override
            protected void invokeApplication(){
                String result = (String) invokeAction("#{userPasswordAction.update(true)}");
                assert result.equals(Configuration.RESULT_FAILURE) : "Password update must fail";
            }
        }.run();
    }

    private void updateUserPassword(final String passwordPropsKey) throws Exception {
        new FacesRequest("admin/person/update"){
            @Override
            protected void updateModelValues(){
                setValue("#{updatePersonAction.inum}", getConf().getString(passwordPropsKey + ".inum"));
                invokeAction("#{updatePersonAction.update}");
                setValue("#{userPasswordAction.person}", getValue("#{updatePersonAction.person}"));

                setValue("#{userPasswordAction.oldPassword}", getConf().getString(passwordPropsKey + ".oldPassword"));
                setValue("#{userPasswordAction.newPassword}", getConf().getString(passwordPropsKey + ".newPassword"));
                setValue("#{userPasswordAction.newPasswordConfirmation}", getConf().getString(passwordPropsKey + ".newPasswordConfirmation"));
            }

            @Override
            protected void invokeApplication(){
                invokeAction("#{userPasswordAction.update(false)}");
            }

            @Override
            protected void renderResponse(){
                assert getValue("#{userPasswordAction.person.userPassword}").equals(getConf().getString(passwordPropsKey + ".newPassword"));
            }
        }.run();
    }
}
