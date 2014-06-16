package org.gluu.site.ldap.persistence.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.gluu.site.model.GluuAttribute;
import org.gluu.site.model.GluuAttributeDataType;
import org.gluu.site.model.GluuAttributePrivacyLevel;
import org.gluu.site.model.GluuCustomAttribute;
import org.gluu.site.model.GluuCustomPerson;
import org.gluu.site.model.GluuStatus;
import org.gluu.site.model.GluuUserRole;
import org.gluu.site.test.AbstractTest;
import org.gluu.site.util.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.unboundid.ldap.sdk.Filter;

/**
 * Test class for to test CRUD operations
 *
 * @author Yuriy Movchan
 */
//TODO: Fix this test according to the new person architecture.
public class LdapEntryManagerUnitTest extends AbstractTest {

	private static LdapEntryManager ldapEntryManager;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setSuperBeforeClass();
		ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tearSuperDownAfterClass();
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#persist(java.lang.Object)}.
	 */
	@Test
	public final void testPersist() {
		// Persist GluuAttribute
		GluuAttribute gluuAttribute = new GluuAttribute();
		gluuAttribute.setDn(conf.getString("ldapEmTest.persist.gluuAttribute.dn"));

		gluuAttribute.setInum(conf.getString("ldapEmTest.persist.gluuAttribute.inum"));
		gluuAttribute.setName(conf.getString("ldapEmTest.persist.gluuAttribute.name"));
		gluuAttribute.setDisplayName(conf.getString("ldapEmTest.persist.gluuAttribute.displayName"));
		gluuAttribute.setDescription(conf.getString("ldapEmTest.persist.gluuAttribute.description"));
		gluuAttribute.setOrigin(conf.getString("ldapEmTest.persist.gluuAttribute.origin"));
		gluuAttribute.setStatus(GluuStatus.getByValue(conf.getString("ldapEmTest.persist.gluuAttribute.status")));
		gluuAttribute.setDataType(GluuAttributeDataType.getByValue(conf.getString("ldapEmTest.persist.gluuAttribute.dataType")));
		gluuAttribute.setEditType(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.persist.gluuAttribute.editType")));
		gluuAttribute.setViewType(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.persist.gluuAttribute.viewType")));
		gluuAttribute.setPrivacyLevel(GluuAttributePrivacyLevel.getByValue(conf.getString("ldapEmTest.persist.gluuAttribute.privacyLevel")));

		ldapEntryManager.persist(gluuAttribute);

		// Persist GluuPerson
		GluuCustomPerson gluuPerson = new GluuCustomPerson();
		gluuPerson.setDn(conf.getString("ldapEmTest.persist.gluuPerson.dn"));
		gluuPerson.setCustomObjectClasses(conf.getStringArray("ldapEmTest.persist.gluuPerson.customObjectClasses"));

		gluuPerson.setInum(conf.getString("ldapEmTest.persist.gluuPerson.inum"));
		gluuPerson.setIname(conf.getString("ldapEmTest.persist.gluuPerson.iname"));
		gluuPerson.setCommonName(conf.getString("ldapEmTest.persist.gluuPerson.cn"));
		gluuPerson.setMail(conf.getString("ldapEmTest.persist.gluuPerson.mail"));
		gluuPerson.setGivenName(conf.getString("ldapEmTest.persist.gluuPerson.givenName"));
		gluuPerson.setAttribute("sn", conf.getString("ldapEmTest.persist.gluuPerson.sn"));
		gluuPerson.setStatus(GluuStatus.getByValue(conf.getString("ldapEmTest.persist.gluuPerson.gluuStatus")));
		gluuPerson.setUserPassword(conf.getString("ldapEmTest.persist.gluuPerson.userPassword"));
		gluuPerson.setUid(conf.getString("ldapEmTest.persist.gluuPerson.uid"));
		gluuPerson.setMemberOf(Arrays.asList(new String[] { conf.getString("ldapEmTest.persist.gluuPerson.memberOf") }));

		gluuPerson.getCustomAttributes().add(new GluuCustomAttribute("title", conf.getString("ldapEmTest.persist.gluuPerson.title")));

		ldapEntryManager.persist(gluuPerson);
	}


	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#merge(java.lang.Object)}.
	 */
	@Test
	public final void testMerge() {
		// Update GluuAttribute
		GluuAttribute gluuAttribute = new GluuAttribute();
		gluuAttribute.setDn(conf.getString("ldapEmTest.merge.gluuAttribute.dn"));

		gluuAttribute.setInum(conf.getString("ldapEmTest.merge.gluuAttribute.inum"));
		gluuAttribute.setName(conf.getString("ldapEmTest.merge.gluuAttribute.name"));
		gluuAttribute.setDisplayName(conf.getString("ldapEmTest.merge.gluuAttribute.displayName"));
		gluuAttribute.setDescription(conf.getString("ldapEmTest.merge.gluuAttribute.description"));
		gluuAttribute.setStatus(GluuStatus.getByValue(conf.getString("ldapEmTest.merge.gluuAttribute.status")));
		gluuAttribute.setOrigin(conf.getString("ldapEmTest.merge.gluuAttribute.origin"));
		gluuAttribute.setDataType(GluuAttributeDataType.getByValue(conf.getString("ldapEmTest.merge.gluuAttribute.dataType")));
		gluuAttribute.setEditType(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.merge.gluuAttribute.editType")));
		gluuAttribute.setViewType(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.merge.gluuAttribute.viewType")));
		gluuAttribute.setPrivacyLevel(GluuAttributePrivacyLevel.getByValue(conf.getString("ldapEmTest.merge.gluuAttribute.privacyLevel")));

		GluuAttribute gluuAttributeAfterMerge = ldapEntryManager.merge(gluuAttribute);

		// Check attributes after update
		assertNotNull(String.format("Entry %s after merge is null", conf.getString("ldapEmTest.merge.gluuAttribute.dn")), gluuAttributeAfterMerge);
		assertTrue(String.format("Invalid attribute %s value", "DN"), conf.getString("ldapEmTest.merge.gluuAttribute.dn").equals(gluuAttributeAfterMerge.getDn()));
		assertTrue(String.format("Invalid attribute %s value", "Inum"), conf.getString("ldapEmTest.merge.gluuAttribute.inum").equals(gluuAttributeAfterMerge.getInum()));
		assertTrue(String.format("Invalid attribute %s value", "Name"), conf.getString("ldapEmTest.merge.gluuAttribute.name").equals(gluuAttributeAfterMerge.getName()));
		assertTrue(String.format("Invalid attribute %s value", "DisplayName"), conf.getString("ldapEmTest.merge.gluuAttribute.displayName").equals(
				gluuAttributeAfterMerge.getDisplayName()));
		assertTrue(String.format("Invalid attribute %s value", "Description"), conf.getString("ldapEmTest.merge.gluuAttribute.description").equals(
				gluuAttributeAfterMerge.getDescription()));
		assertTrue(String.format("Invalid attribute %s value", "Status"), conf.getString("ldapEmTest.merge.gluuAttribute.status").equals(
				gluuAttributeAfterMerge.getStatus().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "Origin"), conf.getString("ldapEmTest.merge.gluuAttribute.origin").equals(
				gluuAttributeAfterMerge.getOrigin()));
		assertTrue(String.format("Invalid attribute %s value", "DataType"), conf.getString("ldapEmTest.merge.gluuAttribute.dataType").equals(
				gluuAttributeAfterMerge.getDataType().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "EditType"), GluuUserRole.equals(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.merge.gluuAttribute.editType")),
				gluuAttributeAfterMerge.getEditType()));
		assertTrue(String.format("Invalid attribute %s value", "ViewType"), GluuUserRole.equals(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.merge.gluuAttribute.viewType")),
				gluuAttributeAfterMerge.getViewType()));
		assertTrue(String.format("Invalid attribute %s value", "PrivacyLevel"), conf.getString("ldapEmTest.merge.gluuAttribute.privacyLevel").equals(
				gluuAttributeAfterMerge.getPrivacyLevel().getValue()));

		// Update GluuPerson
		GluuCustomPerson gluuPerson = new GluuCustomPerson();
		String gluuPersonDn = conf.getString("ldapEmTest.merge.gluuPerson.dn");
		gluuPerson.setDn(gluuPersonDn);

		gluuPerson.setInum(conf.getString("ldapEmTest.merge.gluuPerson.inum"));
		gluuPerson.setIname(conf.getString("ldapEmTest.merge.gluuPerson.iname"));
		gluuPerson.setMail(conf.getString("ldapEmTest.merge.gluuPerson.mail"));
		gluuPerson.setCommonName(conf.getString("ldapEmTest.merge.gluuPerson.cn"));
		gluuPerson.setGivenName(conf.getString("ldapEmTest.merge.gluuPerson.givenName"));
		gluuPerson.setDisplayName(conf.getString("ldapEmTest.merge.gluuPerson.displayName"));
		gluuPerson.setStatus(GluuStatus.getByValue(conf.getString("ldapEmTest.merge.gluuPerson.gluuStatus")));
		gluuPerson.setUid(conf.getString("ldapEmTest.merge.gluuPerson.uid"));
		gluuPerson.setMemberOf(Arrays.asList(new String[] { conf.getString("ldapEmTest.merge.gluuPerson.memberOf") }));
		gluuPerson.setCustomObjectClasses(conf.getStringArray("ldapEmTest.merge.gluuPerson.customObjectClasses"));
		gluuPerson.setAttribute("sn", conf.getString("ldapEmTest.merge.gluuPerson.sn"));
		gluuPerson.getCustomAttributes().add(new GluuCustomAttribute("title", conf.getString("ldapEmTest.merge.gluuPerson.title")));

		GluuCustomPerson gluuPersonAfterMerge = ldapEntryManager.merge(gluuPerson);

		// Check attributes after update
		assertNotNull(String.format("Entry %s after merge is null", gluuPersonDn), gluuPersonAfterMerge);
		assertNotNull(String.format("Custom attributes list in entry %s after merge is null", gluuPersonDn), gluuPersonAfterMerge.getCustomAttributes());
		assertTrue(String.format("Custom attributes count in entry %s after merge is 0", gluuPersonDn), gluuPersonAfterMerge.getCustomAttributes().size() > 0);

		assertTrue(String.format("Invalid attribute %s value", "DN"), conf.getString("ldapEmTest.merge.gluuPerson.dn").equals(gluuPersonAfterMerge.getDn()));
		assertTrue(String.format("Invalid attribute %s value", "Inum"), conf.getString("ldapEmTest.merge.gluuPerson.inum").equals(gluuPersonAfterMerge.getInum()));
		assertTrue(String.format("Invalid attribute %s value", "Iname"), conf.getString("ldapEmTest.merge.gluuPerson.iname").equals(gluuPersonAfterMerge.getIname()));
		assertTrue(String.format("Invalid attribute %s value", "Mail"), conf.getString("ldapEmTest.merge.gluuPerson.mail").equals(gluuPersonAfterMerge.getMail()));
		assertTrue(String.format("Invalid attribute %s value", "CommonName"), conf.getString("ldapEmTest.merge.gluuPerson.cn").equals(gluuPersonAfterMerge.getCommonName()));
		assertTrue(String.format("Invalid attribute %s value", "GivenName"), conf.getString("ldapEmTest.merge.gluuPerson.givenName").equals(gluuPersonAfterMerge.getGivenName()));
		assertTrue(String.format("Invalid attribute %s value", "DisplayName"), conf.getString("ldapEmTest.merge.gluuPerson.displayName").equals(
				gluuPersonAfterMerge.getDisplayName()));
		assertTrue(String.format("Invalid attribute %s value", "Status"), conf.getString("ldapEmTest.merge.gluuPerson.gluuStatus").equals(
				gluuPersonAfterMerge.getStatus().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "Uid"), conf.getString("ldapEmTest.merge.gluuPerson.uid").equals(gluuPersonAfterMerge.getUid()));
		assertNotNull(String.format("Invalid attribute %s value", "MemberOf"), gluuPersonAfterMerge.getMemberOf());
		assertTrue(String.format("Invalid attribute %s value", "MemberOf"), conf.getString("ldapEmTest.merge.gluuPerson.memberOf").equals(gluuPersonAfterMerge.getMemberOf().get(0)));

		String keyPrefix = "ldapEmTest.merge.gluuPerson.";

		for (GluuCustomAttribute gluuPersonAttribute : gluuPersonAfterMerge.getCustomAttributes()) {
			String attributeName = gluuPersonAttribute.getName();
			String confValue = conf.getString(keyPrefix + attributeName);

			// System.out.println(gluuAttribute.getName());
			assertTrue(String.format("Invalid attribute %s value", attributeName), confValue.equals(gluuPersonAttribute.getValue()));
		}
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#remove(java.lang.Object)}.
	 */
	@Test
	public final void testRemove() {
		// Remove GluuAttribute
		GluuAttribute gluuAttribute = new GluuAttribute();
		String gluuAttributeDn = conf.getString("ldapEmTest.remove.gluuAttribute.dn");
		gluuAttribute.setDn(gluuAttributeDn);

		// Attempt to load GluuAttribute after removal
		ldapEntryManager.remove(gluuAttribute);
		try {
			GluuAttribute gluuAttributeAfterRemove = ldapEntryManager.find(GluuAttribute.class, gluuAttributeDn);
			assertNull(String.format("Entry %s exist in DS after removal", gluuAttributeDn), gluuAttributeAfterRemove);
		} catch (LdapMappingException ex) {
		}

		// Remove GluuPerson
		GluuCustomPerson gluuPerson = new GluuCustomPerson();
		String gluuPersonDn = conf.getString("ldapEmTest.remove.gluuPerson.dn");
		gluuPerson.setDn(gluuPersonDn);

		// Attempt to load GluuPerson after removal
		ldapEntryManager.remove(gluuPerson);
		try {
			GluuCustomPerson gluuPersonAfterRemove = ldapEntryManager.find(GluuCustomPerson.class, gluuPersonDn);
			assertNull(String.format("Entry %s exist in DS after removal", gluuPersonDn), gluuPersonAfterRemove);
		} catch (LdapMappingException ex) {
		}
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#contains(java.lang.Object)}.
	 */
	@Test
	public final void testContains() {
		// Update GluuAttribute
		GluuAttribute gluuAttribute = new GluuAttribute();
		gluuAttribute.setBaseDn(conf.getString("ldapEmTest.contains.gluuAttribute.dn"));

		gluuAttribute.setName(conf.getString("ldapEmTest.contains.gluuAttribute.name"));
		gluuAttribute.setDisplayName(conf.getString("ldapEmTest.contains.gluuAttribute.displayName"));

		boolean contains1 = ldapEntryManager.contains(gluuAttribute);
		assertTrue(String.format("Failed to find entry %s", gluuAttribute), contains1);

		GluuCustomPerson gluuPerson = new GluuCustomPerson();
		gluuPerson.setBaseDn(conf.getString("ldapEmTest.contains.gluuPerson.dn"));

		gluuPerson.getCustomAttributes().add(new GluuCustomAttribute("iname", conf.getString("ldapEmTest.contains.gluuPerson.iname")));
		gluuPerson.getCustomAttributes().add(new GluuCustomAttribute("mail", conf.getString("ldapEmTest.contains.gluuPerson.mail")));

		boolean contains2 = ldapEntryManager.contains(gluuPerson);
		assertTrue(String.format("Failed to find entry %s", gluuPerson), contains2);
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#find(java.lang.Class, java.lang.Object)}.
	 */
	@Test
	public final void testFind() {
		// Find GluuAttribute
		GluuAttribute gluuAttribute = ldapEntryManager.find(GluuAttribute.class, conf.getString("ldapEmTest.find.gluuAttribute.dn"));

		assertNotNull(String.format("Failed to load entry %s", conf.getString("ldapEmTest.find.gluuAttribute.dn")), gluuAttribute);
		assertTrue(String.format("Invalid attribute %s value", "DN"), conf.getString("ldapEmTest.find.gluuAttribute.dn").equals(gluuAttribute.getDn()));
		assertTrue(String.format("Invalid attribute %s value", "Inum"), conf.getString("ldapEmTest.find.gluuAttribute.inum").equals(gluuAttribute.getInum()));
		assertTrue(String.format("Invalid attribute %s value", "Name"), conf.getString("ldapEmTest.find.gluuAttribute.name").equals(gluuAttribute.getName()));
		assertTrue(String.format("Invalid attribute %s value", "DisplayName"), conf.getString("ldapEmTest.find.gluuAttribute.displayName").equals(gluuAttribute.getDisplayName()));
		assertTrue(String.format("Invalid attribute %s value", "Description"), conf.getString("ldapEmTest.find.gluuAttribute.description").equals(gluuAttribute.getDescription()));
		assertTrue(String.format("Invalid attribute %s value", "Status"), conf.getString("ldapEmTest.find.gluuAttribute.status").equals(gluuAttribute.getStatus().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "Origin"), conf.getString("ldapEmTest.find.gluuAttribute.origin").equals(gluuAttribute.getOrigin()));
		assertTrue(String.format("Invalid attribute %s value", "DataType"), conf.getString("ldapEmTest.find.gluuAttribute.dataType").equals(gluuAttribute.getDataType().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "EditType"), GluuUserRole.equals(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.find.gluuAttribute.editType")), gluuAttribute.getEditType()));
		assertTrue(String.format("Invalid attribute %s value", "ViewType"), GluuUserRole.equals(GluuUserRole.getByValues(conf.getStringArray("ldapEmTest.find.gluuAttribute.viewType")), gluuAttribute.getViewType()));
		assertTrue(String.format("Invalid attribute %s value", "PrivacyLevel"), conf.getString("ldapEmTest.find.gluuAttribute.privacyLevel").equals(
				gluuAttribute.getPrivacyLevel().getValue()));

		// Find GluuPerson
		GluuCustomPerson gluuPerson = ldapEntryManager.find(GluuCustomPerson.class, conf.getString("ldapEmTest.find.gluuPerson.dn"));
		assertNotNull(String.format("Failed to load entry %s", conf.getString("ldapEmTest.find.gluuPerson.dn")), gluuPerson);

		assertTrue(String.format("Custom attributes count in entry %s after merge is 0", conf.getString("ldapEmTest.find.gluuPerson.dn")),
				gluuPerson.getCustomAttributes().size() > 0);

		assertTrue(String.format("Invalid attribute %s value", "DN"), conf.getString("ldapEmTest.find.gluuPerson.dn").equals(gluuPerson.getDn()));
		assertTrue(String.format("Invalid attribute %s value", "Inum"), conf.getString("ldapEmTest.find.gluuPerson.inum").equals(gluuPerson.getInum()));
		assertTrue(String.format("Invalid attribute %s value", "Iname"), conf.getString("ldapEmTest.find.gluuPerson.iname").equals(gluuPerson.getIname()));
		assertTrue(String.format("Invalid attribute %s value", "Mail"), conf.getString("ldapEmTest.find.gluuPerson.mail").equals(gluuPerson.getMail()));
		assertTrue(String.format("Invalid attribute %s value", "CommonName"), conf.getString("ldapEmTest.find.gluuPerson.cn").equals(gluuPerson.getCommonName()));
		assertTrue(String.format("Invalid attribute %s value", "GivenName"), conf.getString("ldapEmTest.find.gluuPerson.givenName").equals(gluuPerson.getGivenName()));
		assertTrue(String.format("Invalid attribute %s value", "DisplayName"), conf.getString("ldapEmTest.find.gluuPerson.displayName").equals(gluuPerson.getDisplayName()));
		assertTrue(String.format("Invalid attribute %s value", "Status"), conf.getString("ldapEmTest.find.gluuPerson.gluuStatus").equals(gluuPerson.getStatus().getValue()));
		assertTrue(String.format("Invalid attribute %s value", "Uid"), conf.getString("ldapEmTest.find.gluuPerson.uid").equals(gluuPerson.getUid()));
		assertNotNull(String.format("Invalid attribute %s value", "MemberOf"), gluuPerson.getMemberOf());
		assertTrue(String.format("Invalid attribute %s value", "MemberOf"), conf.getString("ldapEmTest.find.gluuPerson.memberOf").equals(gluuPerson.getMemberOf().get(0)));

		String keyPrefix = "ldapEmTest.find.gluuPerson.";

		for (GluuCustomAttribute gluuPersonAttribute : gluuPerson.getCustomAttributes()) {
			String attributeName = gluuPersonAttribute.getName();
			String confValue = conf.getString(keyPrefix + attributeName);

			// System.out.println(gluuAttribute.getName());
			assertTrue(String.format("Invalid attribute %s value", attributeName), confValue.equals(gluuPersonAttribute.getValue()));
		}
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#findEntries(java.lang.String, java.lang.Class, com.unboundid.ldap.sdk.Filter)}.
	 */
	@Test
	public final void testFindEntries() {
		Filter filter1 = Filter.createEqualityFilter(conf.getString("ldapEmTest.findEntries.gluuAttribute.attribute"), conf.getString("ldapEmTest.findEntries.gluuAttribute.pattern"));
		List<GluuAttribute> gluuAttributeList = ldapEntryManager.findEntries(conf.getString("ldapEmTest.findEntries.gluuAttribute.dn"), GluuAttribute.class, filter1);
		assertNotNull("Failed to find GluuAttribute entries", gluuAttributeList);
		assertTrue("Failed to find GluuAttribute entries", gluuAttributeList.size() > 0);

		Filter filter2 = Filter.createSubstringFilter(conf.getString("ldapEmTest.findEntries.gluuPerson.attribute"), null, new String[] { conf
				.getString("ldapEmTest.findEntries.gluuPerson.pattern") }, null);
		List<GluuCustomPerson> personList = ldapEntryManager.findEntries(conf.getString("ldapEmTest.findEntries.gluuPerson.dn"), GluuCustomPerson.class, filter2);
		assertNotNull("Failed to find GluuPerson entries", personList);
		assertTrue("Failed to find GluuPerson entries", personList.size() > 0);
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#findEntries(java.lang.String, java.lang.Class, com.unboundid.ldap.sdk.Filter, int)}.
	 */
	@Test
	public final void testFindEntriesWithSizeLimit() {
		Filter filter1 = Filter.createEqualityFilter(conf.getString("ldapEmTest.findEntriesWithSizeLimit.gluuAttribute.attribute"), conf.getString("ldapEmTest.findEntriesWithSizeLimit.gluuAttribute.pattern"));
		List<GluuAttribute> gluuAttributeList = ldapEntryManager.findEntries(conf.getString("ldapEmTest.findEntriesWithSizeLimit.gluuAttribute.dn"), GluuAttribute.class, filter1,
												conf.getInt("ldapEmTest.findEntriesWithSizeLimit.gluuAttribute.sizeLimit"));
		assertNotNull("Failed to find GluuAttribute entries", gluuAttributeList);
		assertTrue("Failed to find GluuAttribute entries", gluuAttributeList.size() == conf.getInt("ldapEmTest.findEntriesWithSizeLimit.gluuAttribute.sizeLimit"));

		Filter filter2 = Filter.createSubstringFilter(conf.getString("ldapEmTest.findEntriesWithSizeLimit.gluuPerson.attribute"), null, new String[] { conf
				.getString("ldapEmTest.findEntriesWithSizeLimit.gluuPerson.pattern") }, null);
		List<GluuCustomPerson> personList = ldapEntryManager.findEntries(conf.getString("ldapEmTest.findEntriesWithSizeLimit.gluuPerson.dn"), GluuCustomPerson.class, filter2,
										  conf.getInt("ldapEmTest.findEntriesWithSizeLimit.gluuPerson.sizeLimit"));
		assertNotNull("Failed to find GluuPerson entries", personList);
		assertTrue("Failed to find GluuPerson entries", personList.size() == conf.getInt("ldapEmTest.findEntriesWithSizeLimit.gluuPerson.sizeLimit"));
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#findEntries(java.lang.Object)}.
	 */
	@Test
	public final void testFindEntriesByExample() {
		GluuAttribute gluuAttribute = new GluuAttribute();
		gluuAttribute.setBaseDn(conf.getString("ldapEmTest.findEntriesByExample.gluuAttribute.dn"));
		gluuAttribute.setDataType(GluuAttributeDataType.getByValue(conf.getString("ldapEmTest.findEntriesByExample.gluuAttribute.dataType")));

		List<GluuAttribute> gluuAttributeList = ldapEntryManager.findEntries(gluuAttribute);
		assertNotNull("Failed to find GluuAttribute entries", gluuAttributeList);
		assertTrue("Failed to find GluuAttribute entries", gluuAttributeList.size() > 0);

		GluuCustomPerson gluuPerson = new GluuCustomPerson();
		gluuPerson.setBaseDn(conf.getString("ldapEmTest.findEntriesByExample.gluuPerson.dn"));
		gluuPerson.setMail(conf.getString("ldapEmTest.findEntriesByExample.gluuPerson.mail"));

		List<GluuCustomPerson> personList = ldapEntryManager.findEntries(gluuPerson);
		assertNotNull("Failed to find GluuPerson entries", personList);
		assertTrue("Failed to find GluuPerson entries", personList.size() > 0);
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#authenticate(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAuthenticate() {
		boolean authenticated = ldapEntryManager.authenticate(conf.getString("ldapEmTest.authenticate.userName"), conf.getString("ldapEmTest.authenticate.password"), Configuration.instance().getBaseDN());
		assertTrue(String.format("Failed to authenticate user %s", conf.getString("ldapEmTest.authenticate.userName")), authenticated);
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#sortListByProperties(java.lang.Class, java.util.List, java.lang.String[])}.
	 */
	@Test
	public final void testSortListByProperties() {
		@SuppressWarnings("unused")
		class Order {
			private String num;

			public Order(String num) {
				this.num = num;
			}

			public String getNum() {
				return num;
			}

			public void setNum(String num) {
				this.num = num;
			}
		}

		@SuppressWarnings("unused")
		class Item {
			private Order order;
			private String owner;
			private String product;

			public Item(String owner, String product, String num) {
				this.order = new Order(num);
				this.owner = owner;
				this.product = product;
			}

			public Order getOrder() {
				return order;
			}

			public void setOrder(Order order) {
				this.order = order;
			}

			public String getOwner() {
				return owner;
			}

			public void setOwner(String owner) {
				this.owner = owner;
			}

			public String getProduct() {
				return product;
			}

			public void setProduct(String product) {
				this.product = product;
			}
		}

		Item item1 = new Item("Z", "3", "5");
		Item item2 = new Item("Z", "4", "1");
		Item item3 = new Item("Z", "2", "3");
		Item item4 = new Item("B", "1", null);
		Item item5 = new Item("T", "2", "2");

		List<Item> items = new ArrayList<Item>();
		items.add(item1);
		items.add(item2);
		items.add(item3);
		items.add(item4);
		items.add(item5);

		ldapEntryManager.sortListByProperties(Item.class, items, "owner", "order.num", "product");

		assertTrue("Invalid sort order in list", System.identityHashCode(item4) == System.identityHashCode(items.get(0)));
		assertTrue("Invalid sort order in list", System.identityHashCode(item5) == System.identityHashCode(items.get(1)));
		assertTrue("Invalid sort order in list", System.identityHashCode(item2) == System.identityHashCode(items.get(2)));
		assertTrue("Invalid sort order in list", System.identityHashCode(item3) == System.identityHashCode(items.get(3)));
		assertTrue("Invalid sort order in list", System.identityHashCode(item1) == System.identityHashCode(items.get(4)));
	}
	public static class ItemGroup {
		private String owner;
		private String product;
		private int num;
		private float sum;

		public ItemGroup() {}

		public ItemGroup(String owner, String product, int num, float sum) {
			this.owner = owner;
			this.product = product;
			this.num = num;
			this.sum = sum;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getProduct() {
			return product;
		}

		public void setProduct(String product) {
			this.product = product;
		}

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public float getSum() {
			return sum;
		}

		public void setSum(float sum) {
			this.sum = sum;
		}
	}

	/**
	 * Test method for {@link com.bt.mp.ldap.persistence.LdapEntryManager#groupListByProperties(java.lang.Class, java.util.List, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGroupListByProperties() {
		ItemGroup item1 = new ItemGroup("Z", "3", 5, 23.0f);
		ItemGroup item2 = new ItemGroup("K", "2", 1, 13.0f);
		ItemGroup item3 = new ItemGroup("K", "2", 3, 17.0f);
		ItemGroup item4 = new ItemGroup("Z", "3", 4, 19.0f);
		ItemGroup item5 = new ItemGroup("Z", "3", 2, 22.0f);

		List<ItemGroup> items = new ArrayList<ItemGroup>();
		items.add(item1);
		items.add(item2);
		items.add(item3);
		items.add(item4);
		items.add(item5);

		Map<ItemGroup, List<ItemGroup>> groups = ldapEntryManager.groupListByProperties(ItemGroup.class, items, "owner, product", "sum");

		assertTrue("Invalid number of groups", groups.size() == 2);

		List<Integer> group1HashCodes = new ArrayList<Integer>();
		group1HashCodes.add(System.identityHashCode(item1));
		group1HashCodes.add(System.identityHashCode(item4));
		group1HashCodes.add(System.identityHashCode(item5));

		List<Integer> group2HashCodes = new ArrayList<Integer>();
		group2HashCodes.add(System.identityHashCode(item2));
		group2HashCodes.add(System.identityHashCode(item3));

		List<ItemGroup> group1Items = null;
		List<ItemGroup> group2Items = null;

		for (List<ItemGroup> groupItems : groups.values()) {
			assertNotNull("Group items list is null", groupItems);
			if (groupItems.size() == group1HashCodes.size()) {
				group1Items = groupItems;
			} else if (groupItems.size() == group2HashCodes.size()) {
				group2Items = groupItems;
			}
		}
		assertNotNull("Group 1 should has non null value", group1Items);
		assertNotNull("Group 2 should has non null value", group2Items);

		for (ItemGroup item : group1Items) {
			boolean found = false;
			for (Integer itemHashCode : group1HashCodes) {
				if (System.identityHashCode(item) == itemHashCode) {
					found = true;
					break;
				}
			}
			assertTrue("Group 1 contains wrong items", found);
		}


		for (ItemGroup item : group2Items) {
			boolean found = false;
			for (Integer itemHashCode : group2HashCodes) {
				if (System.identityHashCode(item) == itemHashCode) {
					found = true;
					break;
				}
			}
			assertTrue("Group 2 contains wrong items", found);
		}
	}

	/**
	 * Test method for {@link org.gluu.site.ldap.persistence.LdapEntryManager#countEntries(java.lang.Object)}.
	 */
	@Test
	public final void testCountEntries() {
		GluuAttribute gluuAttribute = new GluuAttribute();
		gluuAttribute.setBaseDn(conf.getString("ldapEmTest.countEntries.gluuAttribute.dn"));
		gluuAttribute.setDataType(GluuAttributeDataType.getByValue(conf.getString("ldapEmTest.countEntries.gluuAttribute.dataType")));

		int countAttributes = ldapEntryManager.countEntries(gluuAttribute);
		assertTrue("Failed to calculate count GluuAttribute entries", countAttributes > 0);


		GluuCustomPerson gluuBasePerson = new GluuCustomPerson();
		gluuBasePerson.setBaseDn(conf.getString("ldapEmTest.countEntries.gluuPerson.dn"));

		int countPersons = ldapEntryManager.countEntries(gluuBasePerson);
		assertTrue("Failed to calculate count GluuPerson entries", countPersons > 0);
	}

	public static void main(String args[]) {
		LdapEntryManagerUnitTest ldapEntryManager = new LdapEntryManagerUnitTest();
		try {
			setUpBeforeClass();

			ldapEntryManager.testSortListByProperties();
			// ldapEntryManager.testMerge();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				tearDownAfterClass();
			} catch (Exception ex) {
			}
		}
	}

}
