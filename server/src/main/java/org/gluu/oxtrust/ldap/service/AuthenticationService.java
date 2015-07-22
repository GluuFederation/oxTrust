/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.util.OxTrustConstants;
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
import org.xdi.model.SimpleProperty;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.ArrayHelper;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with persons
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Scope(ScopeType.STATELESS)
@Name("authenticationService")
@AutoCreate
public class AuthenticationService implements Serializable {

	private static final long serialVersionUID = 6685720517520443399L;

	@Logger
	private Log log;

	@In(required = false)
	private GluuLdapConfiguration ldapAuthConfig;

	@In
	private LdapEntryManager ldapAuthEntryManager;

	@In
	GroupService groupService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private List<GluuCustomAttribute> mandatoryAttributes;

	/**
	 * Add new person
	 * 
	 * @param person
	 *            Person
	 */
	public void addPerson(GluuCustomPerson person) {
		ldapAuthEntryManager.persist(person);

	}

	/**
	 * Add person entry
	 * 
	 * @param person
	 *            Person
	 */
	public void updatePerson(GluuCustomPerson person) {
		ldapAuthEntryManager.merge(person);

	}

	/**
	 * Remove person
	 * 
	 * @param person
	 *            Person
	 */
	public void removePerson(GluuCustomPerson person) throws Exception {
		// //TODO: Do we really need to remove group if owner is removed?
		// List<GluuGroup> groups = groupService.getAllGroups();
		// for (GluuGroup group : groups) {
		// if(group.getOwner().equals(person.getDn())){
		// groupService.removeGroup(group);
		// }
		// }

		// Remove person
		ldapAuthEntryManager.remove(person);

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
	public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit) throws Exception {
		String[] targetArray = new String[] { pattern };
		Filter uidFilter = Filter.createSubstringFilter(OxTrustConstants.uid, null, targetArray, null);
		Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
		Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter);

		List<GluuCustomPerson> result = ldapAuthEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter,
				sizeLimit);

		return result;
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

		List<GluuCustomPerson> result = ldapAuthEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter,
				sizeLimit);

		return result;
	}

	public List<GluuCustomPerson> findAllPersons(String[] returnAttributes) throws Exception {
		List<GluuCustomPerson> result = ldapAuthEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, returnAttributes,
				null);

		return result;

	}

	public List<GluuCustomPerson> findPersonsByUids(List<String> uids, String[] returnAttributes) throws Exception {
		List<Filter> uidFilters = new ArrayList<Filter>();
		for (String uid : uids) {
			uidFilters.add(Filter.createEqualityFilter(OxTrustConstants.uid, uid));
		}

		Filter filter = Filter.createORFilter(uidFilters);

		List<GluuCustomPerson> result = ldapAuthEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, returnAttributes,
				filter);

		return result;

	}

	/**
	 * Check if LDAP server contains person with specified attributes
	 * 
	 * @return True if person with specified attributes exist
	 */
	public boolean containsPerson(GluuCustomPerson person) throws Exception {
		boolean result = ldapAuthEntryManager.contains(person);

		return result;
	}

	/**
	 * Get person by inum
	 * 
	 * @param inum
	 *            Inum
	 * @return Person
	 */
	public GluuCustomPerson getPersonByInum(String inum) throws Exception {
		GluuCustomPerson result = ldapAuthEntryManager.find(GluuCustomPerson.class, getDnForPerson(inum));

		return result;
	}

	/**
	 * Get person by DN
	 * 
	 * @param dn
	 *            Dn
	 * @return Person
	 */
	public GluuCustomPerson getPersonByDn(String dn) throws Exception {
		GluuCustomPerson result = ldapAuthEntryManager.find(GluuCustomPerson.class, dn);

		return result;
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

		List<User> users = ldapAuthEntryManager.findEntries(user);// getLdapEntryManagerInstance().findEntries(person);
		if ((users != null) && (users.size() > 0)) {

			return users.get(0);
		}

		return null;
	}

	/**
	 * Get person by uid
	 * 
	 * @param uid
	 *            Uid
	 * @return Person
	 */
	public GluuCustomPerson getPersonByUid(String uid) throws Exception {

		GluuCustomPerson person = new GluuCustomPerson();

		person.setBaseDn(getDnForPerson(null));
		person.setUid(uid);

		List<GluuCustomPerson> persons = ldapAuthEntryManager.findEntries(person);// getLdapEntryManagerInstance().findEntries(person);
		if ((persons != null) && (persons.size() > 0)) {

			return persons.get(0);
		}

		return null;
	}

	/**
	 * Generate new inum for person
	 * 
	 * @return New inum for person
	 */
	public String generateInumForNewPerson() throws Exception {
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
	private String generateInumForNewPersonImpl() throws Exception {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_PERSON_OBJECTTYPE + OxTrustConstants.inumDelimiter + generateInum();
	}

	public String generateInameForNewPerson(String uid) throws Exception {
		return String.format("%s*person*%s", applicationConfiguration.getOrgIname(), uid);
	}

	private String generateInum() {
		String inum = "";
		int value;
		while (true) {
			inum = INumGenerator.generate(1);
			try {
				value = Integer.parseInt(inum, 16);
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
		String orgDn = AuthOrganizationService.instance().getDnForOrganization();
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
        log.debug("Authenticating User with LDAP: username: {0}", userName);
        if (ldapAuthConfig == null) {
        	return ldapAuthEntryManager.authenticate(userName, password, applicationConfiguration.getBaseDN());
       } else {
	        String primaryKey = "uid";
	        if (StringHelper.isNotEmpty(ldapAuthConfig.getPrimaryKey())) {
	            primaryKey = ldapAuthConfig.getPrimaryKey();
	        }
	
//	        String localPrimaryKey = "uid";
//	        if (StringHelper.isNotEmpty(ldapAuthConfig.getLocalPrimaryKey())) {
//	            localPrimaryKey = ldapAuthConfig.getLocalPrimaryKey();
//	        }
        
	        log.debug("Attempting to find userDN by primary key: {0}", primaryKey);

            final List<SimpleProperty> baseDNs = ldapAuthConfig.getBaseDNs();
            if (baseDNs != null && !baseDNs.isEmpty()) {
                for (SimpleProperty baseDnProperty : baseDNs) {
                    String baseDn = baseDnProperty.getValue();

                    GluuCustomPerson user = getUserByAttribute(baseDn, primaryKey, userName);
                    if (user != null) {
                        String userDn = user.getDn();
                        log.debug("Attempting to authenticate userDN: {0}", userDn);
                        if (ldapAuthEntryManager.authenticate(userDn, password)) {
                            log.debug("User authenticated: {0}", userDn);
                            
                            // TODO: If we will get issues we need to use localPrimaryKey to map remote user to local user. Please contact me about this.
                            // We don't need this in oxTrsut+oxAuth mode
                            return true;
                        }
                    }
                }
            } else {
                log.error("There are no baseDns specified in authentication configuration.");
            }
        }

        return false;
    }

    public GluuCustomPerson getUserByAttribute(String baseDn, String attributeName, String attributeValue) {
        log.debug("Getting user information from LDAP: attributeName = '{0}', attributeValue = '{1}'", attributeName, attributeValue);

        GluuCustomPerson user = new GluuCustomPerson();
        user.setDn(baseDn);
        
        List<GluuCustomAttribute> customAttributes =  new ArrayList<GluuCustomAttribute>();
        customAttributes.add(new GluuCustomAttribute(attributeName, attributeValue));

        user.setCustomAttributes(customAttributes);

        List<GluuCustomPerson> entries = ldapAuthEntryManager.findEntries(user);
        log.debug("Found '{0}' entries", entries.size());

        if (entries.size() > 0) {
            return entries.get(0);
        } else {
            return null;
        }
    }

	/**
	 * Get personService instance
	 * 
	 * @return PersonService instance
	 */
	public static PersonService instance() throws Exception {
		return (PersonService) Component.getInstance(PersonService.class);
	}

	public List<GluuCustomAttribute> getMandatoryAtributes() throws Exception {
		if (this.mandatoryAttributes == null) {
			mandatoryAttributes = new ArrayList<GluuCustomAttribute>();
			mandatoryAttributes.add(new GluuCustomAttribute("uid", ""));
			mandatoryAttributes.add(new GluuCustomAttribute("givenName", ""));
			mandatoryAttributes.add(new GluuCustomAttribute("displayName", ""));
			mandatoryAttributes.add(new GluuCustomAttribute("sn", ""));
			mandatoryAttributes.add(new GluuCustomAttribute("mail", ""));
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
		List<GluuCustomPerson> result = ldapAuthEntryManager.createEntities(GluuCustomPerson.class, entriesAttributes);

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
	public GluuCustomPerson getPersonByEmail(String email) throws Exception {
		GluuCustomPerson person = new GluuCustomPerson();
		person.setBaseDn(getDnForPerson(null));
		person.setMail(email);

		List<GluuCustomPerson> persons = ldapAuthEntryManager.findEntries(person);
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

		List<GluuCustomPerson> persons = ldapAuthEntryManager.findEntries(person);
		if ((persons != null) && (persons.size() > 0)) {
			return persons.get(0);
		}

		return null;
	}

}