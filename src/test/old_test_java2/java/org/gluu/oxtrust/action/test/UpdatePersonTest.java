package org.gluu.oxtrust.action.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Dejan Maric
 * Date: 22.10.11.
 */
public class UpdatePersonTest extends AbstractAuthorizationTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	@Test
	@Parameters({"userPropsKey", "updatePropsKey"})
	public void testUpdatePerson(String userPropsKey, String updatePropsKey) throws Exception {
        loginAndCheckLoggedInFacesRequest(userPropsKey);

        updatePerson(updatePropsKey);
    }

    private void updatePerson(final String updatePropsKey) throws Exception {

        
        new NonFacesRequest("/admin/person/update/"){

            protected void renderResponse() throws Exception {
                setValue("#{updatePersonAction.inum}", getConf().getString(updatePropsKey + ".inum"));
                invokeAction("#{updatePersonAction.update}");
                assert (Boolean) getValue("#{updatePersonAction.person ne null}");
            }
        }.run();

        new FacesRequest("/admin/person/update"){
            protected void updateModelValues() throws Exception {
                setValue("#{updatePersonAction.inum}", getConf().getString(updatePropsKey + ".inum"));
                invokeAction("#{updatePersonAction.update}");
            }

            protected void invokeApplication()throws Exception {
                setValue("#{updatePersonAction.person.givenName}", getConf().getString(updatePropsKey + ".givenName"));
                invokeAction("#{updatePersonAction.save}");
            }

            protected void renderResponse() throws Exception {
                assert getValue("#{updatePersonAction.person.givenName}").equals(getConf().getString(updatePropsKey + ".givenName"));
            }

        }.run();
    }
}
