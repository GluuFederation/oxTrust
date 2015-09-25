/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.AttributeData;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.util.ArrayHelper;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.util.StaticUtils;

/**
 * Provides operations with persons
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Scope(ScopeType.STATELESS)
@Name("personService")
@AutoCreate
public class PersonService implements Serializable {

	private static final long serialVersionUID = 6685720517520443399L;

	@Logger
	private Log log;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private GroupService groupService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private List<GluuCustomAttribute> mandatoryAttributes;

	/**
	 * Add new person
	 * 
	 * @param person
	 *            Person
	 * @throws DuplicateEntryException
	 */
	// TODO: Review this methods. We need to check if uid is unique in outside
	// method
	public void addPerson(GluuCustomPerson person) throws DuplicateEntryException {
		GluuCustomPerson uidPerson = new GluuCustomPerson();
		uidPerson.setUid(person.getUid());
		List<GluuCustomPerson> persons = findPersons(uidPerson, 1);
		if (persons == null || persons.size() == 0) {
			ldapEntryManager.persist(person);
		} else {
			throw new DuplicateEntryException("Duplicate UID value: " + person.getUid());
		}
	}

	/**
	 * Add person entry
	 * 
	 * @param person
	 *            Person
	 */
	public void updatePerson(GluuCustomPerson person) {
		ldapEntryManager.merge(person);

	}

	/**
	 * Remove person with persona and contacts branches
	 * 
	 * @param person
	 *            Person
	 */
	public void removePerson(GluuCustomPerson person) {
		// TODO: Do we realy need to remove group if owner is removed?
		List<GluuGroup> groups = groupService.getAllGroups();
		for (GluuGroup group : groups) {
			if (StringHelper.equalsIgnoreCase(group.getOwner(), person.getDn())) {
				groupService.removeGroup(group);
			}
		}

		// Remove person
		ldapEntryManager.remove(person);
	}

	/**
	 * Search persons by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of persons
	 */
	public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter uidFilter = Filter.createSubstringFilter(OxTrustConstants.uid, null, targetArray, null);
		Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
		Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter);

		List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Search persons by sample object
	 * 
	 * @param person
	 *            Person with set attributes relevant to he current search (for
	 *            example gluuAllowPublication)
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of persons
	 */
	public List<GluuCustomPerson> findPersons(GluuCustomPerson person, int sizeLimit) {
		person.setBaseDn(getDnForPerson(null));
		return ldapEntryManager.findEntries(person, sizeLimit);
	}

	/**
	 * Search persons by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @param excludedPersons
	 *            list of uids that we don't want returned by service
	 * @return List of persons
	 */
	public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit, List<GluuCustomPerson> excludedPersons) throws Exception {
		String[] targetArray = new String[] { pattern };
		Filter uidFilter = Filter.createSubstringFilter(OxTrustConstants.uid, null, targetArray, null);
		Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
		Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);

		Filter orFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter);

		Filter searchFilter = orFilter;

		if (excludedPersons != null && excludedPersons.size() > 0) {
			List<Filter> excludeFilters = new ArrayList<Filter>();
			for (GluuCustomPerson excludedPerson : excludedPersons) {
				Filter eqFilter = Filter.createEqualityFilter(OxTrustConstants.uid, excludedPerson.getUid());
				excludeFilters.add(eqFilter);
			}
			Filter orExcludeFilter = null;
			if (excludedPersons.size() == 1) {
				orExcludeFilter = excludeFilters.get(0);
			} else {
				orExcludeFilter = Filter.createORFilter(excludeFilters);
			}
			Filter notFilter = Filter.createNOTFilter(orExcludeFilter);
			searchFilter = Filter.createANDFilter(orFilter, notFilter);
		}

		List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter, sizeLimit);

		return result;

	}

	public List<GluuCustomPerson> findAllPersons(String[] returnAttributes)  {
		List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, returnAttributes, null);

		return result;
	}

	public List<GluuCustomPerson> findPersonsByUids(List<String> uids, String[] returnAttributes) throws Exception {
		List<Filter> uidFilters = new ArrayList<Filter>();
		for (String uid : uids) {
			uidFilters.add(Filter.createEqualityFilter(OxTrustConstants.uid, uid));
		}

		Filter filter = Filter.createORFilter(uidFilters);

		List<GluuCustomPerson> result = ldapEntryManager
				.findEntries(getDnForPerson(null), GluuCustomPerson.class, returnAttributes, filter);

		return result;
	}

	public GluuCustomPerson findPersonByDn(String dn, String... returnAttributes) {
		return ldapEntryManager.find(GluuCustomPerson.class, dn, returnAttributes);
	}

	/**
	 * Check if LDAP server contains person with specified attributes
	 * 
	 * @return True if person with specified attributes exist
	 */
	public boolean containsPerson(GluuCustomPerson person) {
		boolean result = ldapEntryManager.contains(person);

		return result;
	}

	public boolean contains(String dn) {
		return ldapEntryManager.contains(GluuCustomPerson.class, dn);
	}

	/**
	 * Get person by DN
	 * 
	 * @param dn
	 *            Dn
	 * @return Person
	 */
	public GluuCustomPerson getPersonByDn(String dn) {
		GluuCustomPerson result = ldapEntryManager.find(GluuCustomPerson.class, dn);

		return result;

	}

	/**
	 * Get person by inum
	 * 
	 * @param returnClass
	 *            POJO class which EntryManager should use to return entry
	 *            object
	 * @param inum
	 *            Inum
	 * @return Person
	 */
	public GluuCustomPerson getPersonByInum(String inum) {
		GluuCustomPerson person = null;
		try{
			person = ldapEntryManager.find(GluuCustomPerson.class, getDnForPerson(inum));
		}catch (Exception e) {
			log.error("Failed to find Person by Inum " + inum, e);
		}
		
		
		return person;
	}

	/**
	 * Get person by uid
	 * 
	 * @param uid
	 *            Uid
	 * @return Person
	 */
	public GluuCustomPerson getPersonByUid(String uid) {
		GluuCustomPerson person = new GluuCustomPerson();
		person.setBaseDn(getDnForPerson(null));
		person.setUid(uid);

		List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);

		if ((persons != null) && (persons.size() > 0)) {
			return persons.get(0);
		}

		return null;
	}

	public int countPersons() {
		GluuCustomPerson gluuBasePerson = new GluuCustomPerson();
		gluuBasePerson.setBaseDn(getDnForPerson(null));

		return ldapEntryManager.countEntries(gluuBasePerson);
	}

	/**
	 * Generate new inum for person
	 * 
	 * @return New inum for person
	 */
	public String generateInumForNewPerson() {
		GluuCustomPerson person = null;
		String newInum = null;

		do {
			newInum = generateInumForNewPersonImpl();
			String newDn = getDnForPerson(newInum);
			person = new GluuCustomPerson();
			person.setDn(newDn);
		} while (containsPerson(person));

		return newInum;
	}

	/**
	 * Generate new inum for person
	 * 
	 * @return New inum for person
	 * @throws Exception
	 */
	private String generateInumForNewPersonImpl() {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_PERSON_OBJECTTYPE + OxTrustConstants.inumDelimiter + generateInum();
	}

	public String generateInameForNewPerson(String uid) {
		return String.format("%s*person*%s", applicationConfiguration.getOrgIname(), uid);
	}

	private String generateInum() {
		String inum = "";
		long value;
		while (true) {
			inum = INumGenerator.generate(2);
			try {
				value = Long.parseLong(inum.replace(".", ""), 16);
				if (value < 7) {
					continue;
				}
			} catch (Exception ex) {
				log.error("Error generating inum: ", ex);
			}
			break;
		}
		return inum;
	}

	/**
	 * Build DN string for person
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified person or DN for persons branch if inum
	 *         is null
	 * @throws Exception
	 */
	public String getDnForPerson(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=people,%s", orgDn);
		}

		return String.format("inum=%s,ou=people,%s", inum, orgDn);
	}

	/**
	 * Authenticate user
	 * 
	 * @param userName
	 *            User name
	 * @param password
	 *            User password
	 * @return
	 */
	public boolean authenticate(String userName, String password) {
		boolean result = ldapEntryManager.authenticate(userName, password, applicationConfiguration.getBaseDN());

		return result;
	}

	/**
	 * Get personService instance
	 * 
	 * @return PersonService instance
	 */
	public static PersonService instance() {
		return (PersonService) Component.getInstance(PersonService.class);
	}

	public List<GluuCustomAttribute> getMandatoryAtributes() {
		if (this.mandatoryAttributes == null) {
			mandatoryAttributes = new ArrayList<GluuCustomAttribute>();
			mandatoryAttributes.add(new GluuCustomAttribute("uid", "", true, true));
			mandatoryAttributes.add(new GluuCustomAttribute("givenName", "", true, true));
			mandatoryAttributes.add(new GluuCustomAttribute("displayName", "", true, true));
			mandatoryAttributes.add(new GluuCustomAttribute("sn", "", true, true));
			mandatoryAttributes.add(new GluuCustomAttribute("mail", "", true, true));
		}

		return mandatoryAttributes;
	}

	public String getPersonString(List<GluuCustomPerson> persons) throws Exception {
		StringBuilder sb = new StringBuilder();

		for (Iterator<GluuCustomPerson> iterator = persons.iterator(); iterator.hasNext();) {
			GluuCustomPerson call = iterator.next();
			sb.append('\'').append(call.getDisplayName()).append('\'');
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public List<GluuCustomPerson> createEntities(Map<String, List<AttributeData>> entriesAttributes) throws Exception {
		List<GluuCustomPerson> result = ldapEntryManager.createEntities(GluuCustomPerson.class, entriesAttributes);

		return result;
	}

	public boolean isMemberOrOwner(String[] groupDNs, String personDN) throws Exception {
		boolean result = false;
		if (ArrayHelper.isEmpty(groupDNs)) {
			return result;
		}

		for (String groupDN : groupDNs) {
			if (StringHelper.isEmpty(groupDN)) {
				continue;
			}

			result = groupService.isMemberOrOwner(groupDN, personDN);
			if (result) {
				break;
			}
		}

		return result;
	}

	/**
	 * Get person by email
	 * 
	 * @param email
	 *            email
	 * @return Person
	 */
	public GluuCustomPerson getPersonByEmail(String email) {
		GluuCustomPerson person = new GluuCustomPerson();
		person.setBaseDn(getDnForPerson(null));
		person.setMail(email);

		List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);

		if ((persons != null) && (persons.size() > 0)) {
			return persons.get(0);
		}

		return null;
	}

	/**
	 * Get person by attribute
	 * 
	 * @param attribute
	 *            attribute
	 * @param value
	 *            value
	 * @return Person
	 */
	public GluuCustomPerson getPersonByAttribute(String attribute, String value) throws Exception {
		GluuCustomPerson person = new GluuCustomPerson();
		person.setBaseDn(getDnForPerson(null));
		person.setAttribute(attribute, value);

		List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);

		if ((persons != null) && (persons.size() > 0)) {
			return persons.get(0);
		}

		return null;
	}

	/**
	 * Remove custom attribute from all persons.
	 */
	public void removeAttribute(GluuAttribute attribute) {
		ldapEntryManager.removeAttributeFromEntries(getDnForPerson(null), GluuCustomPerson.class, attribute.getName());
		
	}

	/**
	 * @param time
	 * @return
	 */
	public List<GluuCustomPerson> findPersonsForExpiration(Date time) {
		Filter timeFilter = Filter.createLessOrEqualFilter("oxCreationTimestamp", StaticUtils.encodeGeneralizedTime(time));
		Filter linkFilter = Filter.createPresenceFilter("oxInviteCode");
		Filter statusFilter = Filter.createEqualityFilter("gluuStatus", GluuStatus.ACTIVE.getValue());
		Filter searchFilter = Filter.createANDFilter(timeFilter, linkFilter, statusFilter);
		
		List<GluuCustomPerson> people = ldapEntryManager.findEntries(getDnForPerson(null),  GluuCustomPerson.class, searchFilter);
		return people;
	}


	/**
	 * Get user by uid
	 * 
	 * @param uid
	 *            Uid
	 * @return User
	 */
	public User getUserByUid(String uid) {

		User user = new User();

		user.setBaseDn(getDnForPerson(null));
		user.setUid(uid);

		List<User> users = ldapEntryManager.findEntries(user);// getLdapEntryManagerInstance().findEntries(person);
		if ((users != null) && (users.size() > 0)) {

			return users.get(0);
		}

		return null;
	}

}
