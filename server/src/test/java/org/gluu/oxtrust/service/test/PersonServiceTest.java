package org.gluu.oxtrust.service.test;

import java.util.List;

import org.gluu.oxtrust.action.test.AbstractAuthorizationTest;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for PersonService
 *
 * @author Yuriy Movchan Date: 10.14.2010
 */
@RunWith(Arquillian.class)
public class PersonServiceTest extends AbstractAuthorizationTest {

	/**
	 * Test search persons by pattern
	 * @throws Exception
	 */
	@Test
	public void testSearchPersons() throws Exception {
		new JUnitSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				PersonService personService = (PersonService) getInstance("personService");

				String pattern = testData.getString("person.search.pattern");
				List<GluuCustomPerson> persons = personService.searchPersons(pattern, OxTrustConstants.searchPersonsSizeLimit);

				Assert.assertNotNull("Failed to find persons", persons);
				Assert.assertTrue("Failed to find persons", persons.size() > 0);
			}
		}.run();
	}

	@Test
	public void testGetPersonByUid() throws Exception {
		new JUnitSeamTest.FacesRequest() {
			@Override
			protected void invokeApplication() throws Exception {
				PersonService personService = (PersonService) getInstance("personService");

				String personUid = testData.getString("person.uid");
				GluuCustomPerson person = personService.getPersonByUid(personUid);

				Assert.assertNotNull("Failed to find person", person);
				Assert.assertEquals(personUid, person.getUid());
			}
		}.run();
	}

}
