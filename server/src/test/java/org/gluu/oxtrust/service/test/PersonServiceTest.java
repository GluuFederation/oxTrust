/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import java.util.List;

import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Test class for PersonService
 *
 * @author Yuriy Movchan Date: 10.14.2010
 */
public class PersonServiceTest extends BaseTest {

	/**
	 * Test search persons by pattern
	 * @throws Exception
	 */
	@Test
	@Parameters({ "person.search.pattern" })
	public void testSearchPersons(final String pattern) throws Exception {
		new SeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				IPersonService personService = (IPersonService) getInstance("personService");

				String pattern = testData.getString("");
				List<GluuCustomPerson> persons = personService.searchPersons(pattern, OxTrustConstants.searchPersonsSizeLimit);

				assertNotNull(persons, "Failed to find persons");
				assertTrue(persons.size() > 0, "Failed to find persons");
			}
		}.run();
	}

	@Test
	@Parameters({ "person.uid" })
	public void testGetPersonByUid(final String personUid) throws Exception {
		new SeamTest.FacesRequest() {
			@Override
			protected void invokeApplication() throws Exception {
				IPersonService personService = (IPersonService) getInstance("personService");

				GluuCustomPerson person = personService.getPersonByUid(personUid);

				assertNotNull(person, "Failed to find person");
				assertEquals(person.getUid(), personUid);
			}
		}.run();
	}

}
