package org.gluu.oxtrust.action.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * User: Dejan Maric
 * Date: 20.10.11.
 */
public class SearchPersonTest extends AbstractAuthorizationTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	@Test
	@Parameters({"userPropsKey", "searchedPropsKey"})
	public void testSearchPerson(String userPropsKey, String searchedPropsKey) throws Exception {
        loginAndCheckLoggedInFacesRequest(userPropsKey);
        searchPerson(searchedPropsKey);
    }

    private void searchPerson(final String searchedPropsKey) throws Exception {
        new NonFacesRequest("/admin/person/personInventory.xhtml"){
            protected void renderResponse() throws Exception {
                assert (Boolean) getValue("#{searchPersonAction.personList eq null}");
            }
        }.run();

        new FacesRequest("/admin/person/personInventory.xhtml"){
            protected void updateModelValues() throws Exception {
                setValue("#{searchPersonAction.searchPattern}", getConf().getString(searchedPropsKey + ".uid"));
            }

            protected void renderResponse() throws Exception {
                assert (Boolean) getValue("#{searchPersonAction.personList.size gt 0}");
            }

        }.run();
    }
}
