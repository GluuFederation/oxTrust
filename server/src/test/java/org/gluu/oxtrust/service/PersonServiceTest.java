/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.gluu.oxtrust.action.BaseTest;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test class for PersonService
 *
 * @author Yuriy Movchan Date: 10.14.2010
 */
public class PersonServiceTest extends BaseTest {

	@Inject
	private IPersonService personService;

	/**
	 * Test search persons by pattern
	 * 
	 * @throws Exception
	 */
	@Test
	@Parameters({ "person.search.pattern" })
	public void testSearchPersons(final String pattern) throws Exception {
		List<GluuCustomPerson> persons = personService.searchPersons(pattern, OxTrustConstants.searchPersonsSizeLimit);

		assertNotNull(persons, "Failed to find persons");
		assertTrue(persons.size() > 0, "Failed to find persons");
	}

	@Test
	@Parameters({ "person.uid" })
	public void testGetPersonByUid(final String personUid) throws Exception {
		GluuCustomPerson person = personService.getPersonByUid(personUid);

		assertNotNull(person, "Failed to find person");
		assertEquals(person.getUid(), personUid);
	}

}
