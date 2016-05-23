/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import java.util.Arrays;
import java.util.List;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.Component;
import org.jboss.seam.mock.JUnitSeamTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.xdi.util.StringHelper;

/**
 * @author Yuriy Movchan
 * @version 0.1, 03/24/2014
 */

public class CleanUpTest extends ConfigurableTest {

	
	/**
	 * Test search 
	 * @throws Exception
	 */
	//@Test
	public void cleanUpPersons() throws Exception {
		new JUnitSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				System.out.println("cleanup person Test initialted ");
				IPersonService personService = (IPersonService) getInstance("personService");
				String usedPersons = testData.getString("test.keep.persons");
				Assert.assertNotNull(usedPersons);
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
			
					Assert.assertNotNull(persons);
					System.out.println("Found persons: " + persons.size());
					System.out.println("Total persons: " + countResults);
			
					for (GluuCustomPerson person : persons) {
						//String clientId = person.getClientId();
						if (!usedPersonsList.contains(person.getInum())) {
							try {
								personService.removePerson(person);
							} catch (EntryPersistenceException ex) {
								System.out.println("Failed to remove person: " + ex.getMessage());
							}
							countRemoved++;
						}
					}
				}

				System.out.println("Removed Persons: " + countRemoved);
			}
		}.run();
	}

	
	/**
	 * Test search 
	 * @throws Exception
	 */
	//@Test
	public void cleanUpGroups() throws Exception {
		new JUnitSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				System.out.println("cleanup person Test initialted ");
				IGroupService groupsService = (IGroupService) getInstance("groupService");
				String usedGroups = testData.getString("test.keep.groups");
				Assert.assertNotNull(usedGroups);
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
			
					Assert.assertNotNull(groups);
					System.out.println("Found groups: " + groups.size());
					System.out.println("Total groups: " + countResults);
			
					for (GluuGroup group : groups) {
						//String clientId = person.getClientId();
						if (!usedGroupsList.contains(group.getInum())) {
							try {
								groupsService.removeGroup(group);
							} catch (EntryPersistenceException ex) {
								System.out.println("Failed to remove person: " + ex.getMessage());
							}
							countRemoved++;
						}
					}
				}

				System.out.println("Removed Persons: " + countRemoved);
			}
		}.run();
	}
}
