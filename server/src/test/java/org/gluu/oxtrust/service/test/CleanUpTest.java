/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

import org.gluu.oxtrust.action.test.BaseTest;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.MemberService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.persist.exception.mapping.EntryPersistenceException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xdi.util.StringHelper;

/**
 * @author Yuriy Movchan
 * @version 0.1, 03/24/2014
 */

public class CleanUpTest extends BaseTest {

	@Inject
	private IGroupService groupsService;

	@Inject
	private IPersonService personService;

	@Inject
	private MemberService memberService;
	/**
	 * Test search
	 * 
	 * @throws Exception
	 */
//	@Test
	@Parameters(value = "test.keep.persons")
	public void cleanUpPersons(String usedPersons) throws Exception {
		System.out.println("cleanup person Test initialted ");
		assertNotNull(usedPersons);
		List<String> usedPersonsList = Arrays.asList(StringHelper.split(usedPersons, ",", true, false));
		System.out.println("Used persons: " + usedPersonsList);

		int personsResultSetSize = 50;

		int countResults = 0;
		int countRemoved = 0;
		boolean existsMorePersons = true;
		while (existsMorePersons && countResults < 10000) {
			List<GluuCustomPerson> persons = personService.findAllPersons(new String[] { "inum" });

			existsMorePersons = persons.size() == personsResultSetSize;
			countResults += persons.size();

			assertNotNull(persons);
			System.out.println("Found persons: " + persons.size());
			System.out.println("Total persons: " + countResults);

			for (GluuCustomPerson person : persons) {
				// String clientId = person.getClientId();
				if (!usedPersonsList.contains(person.getInum())) {
					try {
						memberService.removePerson(person);
						countRemoved++;
					} catch (EntryPersistenceException ex) {
						System.out.println("Failed to remove person: " + ex.getMessage());
					}
				}
			}
		}

		System.out.println("Removed Persons: " + countRemoved);
	}

	/**
	 * Test search
	 * 
	 * @throws Exception
	 */
//	@Test
	@Parameters(value = "test.keep.persons")
	public void cleanUpGroups(String usedGroups) throws Exception {
		System.out.println("cleanup person Test initialted ");
		assertNotNull(usedGroups);
		List<String> usedGroupsList = Arrays.asList(StringHelper.split(usedGroups, ",", true, false));
		System.out.println("Used Groups: " + usedGroupsList);

		int groupsResultSetSize = 50;

		int countResults = 0;
		int countRemoved = 0;
		boolean existsMoreGroups = true;
		while (existsMoreGroups && countResults < 10000) {
			List<GluuGroup> groups = groupsService.getAllGroups();

			existsMoreGroups = groups.size() == groupsResultSetSize;
			countResults += groups.size();

			assertNotNull(groups);
			System.out.println("Found groups: " + groups.size());
			System.out.println("Total groups: " + countResults);

			for (GluuGroup group : groups) {
				// String clientId = person.getClientId();
				if (!usedGroupsList.contains(group.getInum())) {
					try {
						groupsService.removeGroup(group);
						countRemoved++;
					} catch (EntryPersistenceException ex) {
						System.out.println("Failed to remove group: " + ex.getMessage());
					}
				}
			}
		}

		System.out.println("Removed Persons: " + countRemoved);
	}
}
