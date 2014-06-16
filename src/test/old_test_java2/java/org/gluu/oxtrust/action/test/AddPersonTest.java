package org.gluu.oxtrust.action.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Dejan Maric
 * Date: 23.10.11.
 */
public class AddPersonTest extends AbstractAuthorizationTest {

    @BeforeTest
    public void initTestConfiguration() throws Exception {
        initTest();
    }

	@Test
	@Parameters({"userPropsKey", "addPropsKey"})
	public void testUpdatePerson(String userPropsKey, String addPropsKey) throws Exception {
        loginAndCheckLoggedInFacesRequest(userPropsKey);

        addPerson(addPropsKey);
    }

    private void addPerson(final String addPropsKey) throws Exception {

        new FacesRequest("/admin/person/add"){
            protected void updateModelValues() throws Exception {
                invokeAction("#{updatePersonAction.add}");
            }

            protected void invokeApplication()throws Exception {
                setValue("#{updatePersonAction.person.displayName}", getConf().getString(addPropsKey + ".displayName"));
                setValue("#{updatePersonAction.person.mail}", getConf().getString(addPropsKey + ".mail"));
                setValue("#{updatePersonAction.person.givenName}", getConf().getString(addPropsKey + ".givenName"));
                setValue("#{updatePersonAction.person.surname}", getConf().getString(addPropsKey + ".sn"));
                invokeAction("#{updatePersonAction.save}");
            }

            protected void renderResponse() throws Exception {
                assert getValue("#{updatePersonAction.person.givenName}").equals(getConf().getString(addPropsKey + ".givenName"));

                //clean up
                invokeAction("#{updatePersonAction.delete}");
            }

        }.run();
    }


}
