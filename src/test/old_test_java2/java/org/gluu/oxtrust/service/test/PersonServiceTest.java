package org.gluu.oxtrust.service.test;

import java.util.List;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.Configuration;
import org.jboss.seam.mock.AbstractSeamTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for PersonService
 *
 * @author Yuriy Movchan Date: 10.14.2010
 */
public class PersonServiceTest extends ConfigurableTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	/**
	 * Test search persons by pattern
	 * @throws Exception
	 */
	@Test
	public void testSearchPersons() throws Exception {
		new AbstractSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				PersonService personService = (PersonService) getInstance("personService");
				List<GluuCustomPerson> persons = personService.searchPersons(getConf().getString("personServiceTest.testSearchPersons.pattern"),
						Configuration.searchPersonsSizeLimit);

				Assert.assertNotNull(persons, "Failed to find persons");
				Assert.assertTrue(persons.size() > 0, "Failed to find persons");
			}
		}.run();
	}

    @Test
    public void testGetPersonByUid() throws Exception {
        new AbstractSeamTest.FacesRequest(){
			@Override
			protected void invokeApplication() throws Exception {
                PersonService personService = (PersonService) getInstance("personService");
                GluuCustomPerson person = personService.getPersonByUid(getConf().getString("personServiceTest.testGetPersonByUid.uid"));
                Assert.assertNotNull(person, "Failed to find person");
                Assert.assertEquals(getConf().getString("personServiceTest.testGetPersonByUid.uid"), person.getUid());
            }
        }.run();
    }
}
