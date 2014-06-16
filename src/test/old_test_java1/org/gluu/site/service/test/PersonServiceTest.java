package org.gluu.site.service.test;

import java.util.List;

import org.gluu.site.action.test.ConfigurableTest;
import org.gluu.site.ldap.service.PersonService;
import org.gluu.site.model.GluuCustomPerson;
import org.gluu.site.util.Configuration;
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
		new FacesRequest() {

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
}
