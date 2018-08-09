/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.User;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.operation.DuplicateEntryException;
import org.gluu.persist.model.AttributeData;
import org.gluu.search.filter.Filter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.ArrayHelper;
import org.xdi.util.INumGenerator;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;

/**
 * Provides operations with persons
 *
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Stateless
@Named
public class PersonService implements Serializable, IPersonService {

    private static final long serialVersionUID = 6685720517520443399L;

    @Inject
    private Logger log;

    @Inject
    private PersistenceEntryManager ldapEntryManager;

    @Inject
    private AttributeService attributeService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private OrganizationService organizationService;

    private List<GluuCustomAttribute> mandatoryAttributes;

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#addCustomObjectClass(org.gluu.oxtrust.model.GluuCustomPerson)
     */
    @Override
    public void addCustomObjectClass(GluuCustomPerson person) {
        String customObjectClass = attributeService.getCustomOrigin();
        String[] customObjectClassesArray = person.getCustomObjectClasses();
        if (ArrayHelper.isNotEmpty(customObjectClassesArray)) {
            List<String> customObjectClassesList = Arrays.asList(customObjectClassesArray);
            if (!customObjectClassesList.contains(customObjectClass)) {
                List<String> customObjectClassesListUpdated = new ArrayList<String>();
                customObjectClassesListUpdated.addAll(customObjectClassesList);
                customObjectClassesListUpdated.add(customObjectClass);
                customObjectClassesList = customObjectClassesListUpdated;
            }

            person.setCustomObjectClasses(customObjectClassesList.toArray(new String[0]));

        } else {
            person.setCustomObjectClasses(new String[] { customObjectClass });
        }
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#addPerson(org.gluu.oxtrust.model.GluuCustomPerson)
     */
    // TODO: Review this methods. We need to check if uid is unique in outside
    // method
    @Override
    public void addPerson(GluuCustomPerson person) throws Exception {
        GluuCustomPerson uidPerson = new GluuCustomPerson();
        uidPerson.setUid(person.getUid());
        List<GluuCustomPerson> persons = findPersons(uidPerson, 1);
        if (persons == null || persons.size() == 0) {
            person.setCreationDate(new Date());
            ldapEntryManager.persist(person);
        } else {
            throw new DuplicateEntryException("Duplicate UID value: " + person.getUid());
        }
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#updatePerson(org.gluu.oxtrust.model.GluuCustomPerson)
     */
    @Override
    public void updatePerson(GluuCustomPerson person) {

        Date updateDate = new Date();
        person.setUpdatedAt(updateDate);
        //Update oxTrustMetaLastModified if it has been previously set
        if (person.getAttribute("oxTrustMetaLastModified") != null) {
            person.setAttribute("oxTrustMetaLastModified", ISODateTimeFormat.dateTime().withZoneUTC().print(updateDate.getTime()));
        }
        ldapEntryManager.merge(person);

    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#removePerson(org.gluu.oxtrust.model.GluuCustomPerson)
     */
    @Override
    public void removePerson(GluuCustomPerson person) {
        // Remove person
        ldapEntryManager.removeRecursively(person.getDn());
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#searchPersons(java.lang.String, int)
     */
    @Override
    public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit) {
        String[] targetArray = new String[] { pattern };
        Filter uidFilter = Filter.createSubstringFilter(OxConstants.UID, null, targetArray, null);
        Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
        Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter);

        List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter, sizeLimit);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#searchPersons(java.lang.String)
     */
    @Override
    public List<GluuCustomPerson> searchPersons(String pattern) {
        String[] targetArray = new String[] { pattern };
        Filter uidFilter = Filter.createSubstringFilter(OxConstants.UID, null, targetArray, null);
        Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
        Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter ppidFilter = Filter.createSubstringFilter(OxTrustConstants.ppid, null, targetArray, null);
        Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter, ppidFilter, inumFilter);

        List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, searchFilter);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#findPersons(org.gluu.oxtrust.model.GluuCustomPerson, int)
     */
    @Override
    public List<GluuCustomPerson> findPersons(GluuCustomPerson person, int sizeLimit) {
        person.setBaseDn(getDnForPerson(null));
        return ldapEntryManager.findEntries(person, sizeLimit);
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#searchPersons(java.lang.String, int, java.util.List)
     */
    @Override
    public List<GluuCustomPerson> searchPersons(String pattern, int sizeLimit, List<GluuCustomPerson> excludedPersons) throws Exception {
        String[] targetArray = new String[] { pattern };
        Filter uidFilter = Filter.createSubstringFilter(OxConstants.UID, null, targetArray, null);
        Filter mailFilter = Filter.createSubstringFilter(OxTrustConstants.mail, null, targetArray, null);
        Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);

        Filter orFilter = Filter.createORFilter(uidFilter, mailFilter, nameFilter, inameFilter);

        Filter searchFilter = orFilter;

        if (excludedPersons != null && excludedPersons.size() > 0) {
            List<Filter> excludeFilters = new ArrayList<Filter>();
            for (GluuCustomPerson excludedPerson : excludedPersons) {
                Filter eqFilter = Filter.createEqualityFilter(OxConstants.UID, excludedPerson.getUid());
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

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#findAllPersons(java.lang.String[])
     */
    @Override
    public List<GluuCustomPerson> findAllPersons(String[] returnAttributes)  {
        List<GluuCustomPerson> result = ldapEntryManager.findEntries(getDnForPerson(null), GluuCustomPerson.class, null, returnAttributes);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#findPersonsByUids(java.util.List, java.lang.String[])
     */
    @Override
    public List<GluuCustomPerson> findPersonsByUids(List<String> uids, String[] returnAttributes) throws Exception {
        List<Filter> uidFilters = new ArrayList<Filter>();
        for (String uid : uids) {
            uidFilters.add(Filter.createEqualityFilter(OxConstants.UID, uid));
        }

        Filter filter = Filter.createORFilter(uidFilters);

        List<GluuCustomPerson> result = ldapEntryManager
                .findEntries(getDnForPerson(null), GluuCustomPerson.class, filter, returnAttributes);

        return result;
    }
    
    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#findPersonsByMailds(java.util.List, java.lang.String[])
     */
    @Override
    public List<GluuCustomPerson> findPersonsByMailids(List<String> mailids, String[] returnAttributes) throws Exception {
        List<Filter> mailidFilters = new ArrayList<Filter>();
        for (String mailid : mailids) {
        	mailidFilters.add(Filter.createEqualityFilter(OxTrustConstants.mail, mailid));
        }

        Filter filter = Filter.createORFilter(mailidFilters);

        List<GluuCustomPerson> result = ldapEntryManager
                .findEntries(getDnForPerson(null), GluuCustomPerson.class, filter, returnAttributes);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#findPersonByDn(java.lang.String, java.lang.String)
     */
    @Override
    public GluuCustomPerson findPersonByDn(String dn, String... returnAttributes) {
        return ldapEntryManager.find(GluuCustomPerson.class, dn, returnAttributes);
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#containsPerson(org.gluu.oxtrust.model.GluuCustomPerson)
     */
    @Override
    public boolean containsPerson(GluuCustomPerson person) {
        boolean result=false;
        try {
            result = ldapEntryManager.contains(person);
        }
        catch (Exception e){
            log.debug(e.getMessage(), e);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#contains(java.lang.String)
     */
    @Override
    public boolean contains(String dn) {
        return ldapEntryManager.contains(GluuCustomPerson.class, dn);
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonByDn(java.lang.String)
     */
    @Override
    public GluuCustomPerson getPersonByDn(String dn) {
        GluuCustomPerson result = ldapEntryManager.find(GluuCustomPerson.class, dn);

        return result;

    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonByInum(java.lang.String)
     */
    @Override
    public GluuCustomPerson getPersonByInum(String inum) {
        GluuCustomPerson person = null;
        try{
            person = ldapEntryManager.find(GluuCustomPerson.class, getDnForPerson(inum));
        }catch (Exception e) {
            log.error("Failed to find Person by Inum " + inum, e);
        }


        return person;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonByUid(java.lang.String)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#countPersons()
     */
    @Override
    public int countPersons() {
        GluuCustomPerson gluuBasePerson = new GluuCustomPerson();
        gluuBasePerson.setBaseDn(getDnForPerson(null));

        return ldapEntryManager.countEntries(gluuBasePerson);
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#generateInumForNewPerson()
     */
    @Override
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
        String orgInum = organizationService.getInumForOrganization();
        return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_PERSON_OBJECTTYPE + OxTrustConstants.inumDelimiter + generateInum();
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#generateInameForNewPerson(java.lang.String)
     */
    @Override
    public String generateInameForNewPerson(String uid) {
        return String.format("%s*person*%s", appConfiguration.getOrgIname(), uid);
    }

    private String generateInum() {
        String inum = "";
        while (true) {
            inum = INumGenerator.generate(4);
            try {
                BigInteger value = new BigInteger(inum.replace(".", ""), 16);
                if (value.doubleValue() < 7.0) {
                    continue;
                }
            } catch (Exception ex) {
                log.error("Error generating inum: ", ex);
            }
            break;
        }
        return inum;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getDnForPerson(java.lang.String)
     */
    @Override
    public String getDnForPerson(String inum) {
        String orgDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=people,%s", orgDn);
        }

        return String.format("inum=%s,ou=people,%s", inum, orgDn);
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#authenticate(java.lang.String, java.lang.String)
     */
    @Override
    public boolean authenticate(String userName, String password) {
        boolean result = ldapEntryManager.authenticate(appConfiguration.getBaseDN(), userName, password);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getMandatoryAtributes()
     */
    @Override
    public List<GluuCustomAttribute> getMandatoryAtributes() {
        if (this.mandatoryAttributes == null) {
            mandatoryAttributes = new ArrayList<GluuCustomAttribute>();
            mandatoryAttributes.add(new GluuCustomAttribute("uid", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("givenName", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("displayName", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("sn", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("mail", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("userPassword", "", true, true));
            mandatoryAttributes.add(new GluuCustomAttribute("gluuStatus", "", true, true));
        }

        return mandatoryAttributes;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonUid(java.util.List)
     */
    @Override
    public String getPersonUids(List<GluuCustomPerson> persons) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (Iterator<GluuCustomPerson> iterator = persons.iterator(); iterator.hasNext();) {
            GluuCustomPerson call = iterator.next();
            sb.append('\'').append(call.getUid()).append('\'');
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonMailid(java.util.List)
     */
    @Override
    public String getPersonMailids(List<GluuCustomPerson> persons) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (Iterator<GluuCustomPerson> iterator = persons.iterator(); iterator.hasNext();) {
            GluuCustomPerson call = iterator.next();
            sb.append('\'').append(call.getMail()).append('\'');
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#createEntities(java.util.Map)
     */
    @Override
    public List<GluuCustomPerson> createEntities(Map<String, List<AttributeData>> entriesAttributes) throws Exception {
        List<GluuCustomPerson> result = ldapEntryManager.createEntities(GluuCustomPerson.class, entriesAttributes);

        return result;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonByEmail(java.lang.String)
     */
    @Override
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
    
    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonsByUid(java.lang.String)
     */
    @Override
    public List<GluuCustomPerson> getPersonsByUid(String uid) {
        GluuCustomPerson person = new GluuCustomPerson();
        person.setBaseDn(getDnForPerson(null));
        person.setUid(uid);

        List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);

        return persons;
    }


    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonsByEmail(java.lang.String)
     */
    @Override
    public List<GluuCustomPerson> getPersonsByEmail(String email) {
        GluuCustomPerson person = new GluuCustomPerson();
        person.setBaseDn(getDnForPerson(null));
        person.setMail(email);

        List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);

        return persons ;
    }

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getPersonByAttribute(java.lang.String, java.lang.String)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.gluu.oxtrust.ldap.service.IPersonService#getUserByUid(java.lang.String)
     */
    @Override
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

    /**
     * Get list of persons by attribute
     *
     * @param attribute
     *            attribute
     * @param value
     *            value
     * @return List <Person>
     */
    @Override
    public List<GluuCustomPerson> getPersonsByAttribute(String attribute, String value) throws Exception {
        log.info("atttriburte : " + attribute + "      value : "+value);
        GluuCustomPerson person = new GluuCustomPerson();
        person.setBaseDn(getDnForPerson(null));
        person.setAttribute(attribute, value);

        List<GluuCustomPerson> persons = ldapEntryManager.findEntries(person);
        log.info("list : " + (persons));
        if ((persons != null) && (persons.size() > 0)) {
            return persons;
        }

        return null;
    }

}