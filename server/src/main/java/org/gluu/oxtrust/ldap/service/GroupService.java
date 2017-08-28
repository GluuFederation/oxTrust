/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuGroupVisibility;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.ArrayHelper;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Stateless
@Named
public class GroupService implements Serializable, IGroupService {

	private static final long serialVersionUID = -9167587377957719152L;

	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private LdapEntryManager ldapEntryManager;	

	@Inject
	private OrganizationService organizationService;
	
	@Inject
	private PersonService personService;

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#addGroup(org.gluu.oxtrust.model.GluuGroup)
	 */
	@Override
	public void addGroup(GluuGroup group) throws Exception {
		GluuGroup displayNameGroup = new GluuGroup();
		displayNameGroup.setDisplayName(group.getDisplayName());
		List<GluuGroup> groups= findGroups(displayNameGroup, 1);
		if (groups == null || groups.size() == 0) {
			ldapEntryManager.persist(group);
		} else {
			throw new DuplicateEntryException("Duplicate displayName: " + group.getDisplayName());
		}
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#updateGroup(org.gluu.oxtrust.model.GluuGroup)
	 */
	@Override
	public void updateGroup(GluuGroup group) throws Exception {
		ldapEntryManager.merge(group);

	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#removeGroup(org.gluu.oxtrust.model.GluuGroup)
	 */
	@Override
	public void removeGroup(GluuGroup group) {
		if (group.getMembers() != null) {
			List<String> memberDNs = group.getMembers();
			for (String memberDN : memberDNs) {
				GluuCustomPerson person = personService.getPersonByDn(memberDN);
				List<String> groupDNs = person.getMemberOf();
				List<String> updatedGroupDNs = new ArrayList<String>();
				updatedGroupDNs.addAll(groupDNs);
				updatedGroupDNs.remove(group.getDn());
				person.setMemberOf(updatedGroupDNs);
				personService.updatePerson(person);
			}
		}

		ldapEntryManager.remove(group);
		// clear references in gluuPerson entries
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getAllGroups()
	 */
	@Override
	public List<GluuGroup> getAllGroups() {
		return ldapEntryManager.findEntries(getDnForGroup(null), GluuGroup.class, null);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#isMemberOrOwner(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isMemberOrOwner(String groupDN, String personDN) {
		Filter ownerFilter = Filter.createEqualityFilter(OxTrustConstants.owner, personDN);
		Filter memberFilter = Filter.createEqualityFilter(OxTrustConstants.member, personDN);
		Filter searchFilter = Filter.createORFilter(ownerFilter, memberFilter);

		boolean isMemberOrOwner = false;
		try {
			isMemberOrOwner = ldapEntryManager.findEntries(groupDN, GluuGroup.class, searchFilter, 0, 1).size() > 0;

		} catch (EntryPersistenceException ex) {
			log.error("Failed to determine if person '{}' memeber or owner of group '{}'", personDN, groupDN, ex);
		}

		return isMemberOrOwner;
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getGroupByInum(java.lang.String)
	 */
	@Override
	public GluuGroup getGroupByInum(String inum) {
		GluuGroup result = null;
		try{
			result = ldapEntryManager.find(GluuGroup.class, getDnForGroup(inum));
		}catch(Exception e){
			log.error("Failed to find group by Inum " + inum, e);
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getDnForGroup(java.lang.String)
	 */
	@Override
	public String getDnForGroup(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=groups,%s", orgDn);
		}

		return String.format("inum=%s,ou=groups,%s", inum, orgDn);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#countGroups()
	 */
	@Override
	public int countGroups() {
		GluuGroup gluuGroup = new GluuGroup();
		gluuGroup.setBaseDn(getDnForGroup(null));

		return ldapEntryManager.countEntries(gluuGroup);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#generateInumForNewGroup()
	 */
	@Override
	public String generateInumForNewGroup() throws Exception {
		GluuGroup group = new GluuGroup();
		String newInum = null;
		do {
			newInum = generateInumForNewGroupImpl();
			String newDn = getDnForGroup(newInum);
			group.setDn(newDn);
		} while (containsGroup(group));

		return newInum;
	}

    private boolean containsGroup(GluuGroup group) {
        boolean result=false;
        try {
            result = ldapEntryManager.contains(group);
        }
        catch (Exception e){
            log.debug(e.getMessage(), e);
        }
        return result;
    }

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#generateInameForNewGroup(java.lang.String)
	 */
	@Override
	public String generateInameForNewGroup(String name) throws Exception {
		return String.format("%s*group*%s", appConfiguration.getOrgIname(), name);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#searchGroups(java.lang.String, int)
	 */
	@Override
	public List<GluuGroup> searchGroups(String pattern, int sizeLimit) throws Exception {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);

		List<GluuGroup> result = ldapEntryManager.findEntries(getDnForGroup(null), GluuGroup.class, searchFilter, 0, sizeLimit);

		return result;
	}
	
	@Override
	public List<GluuGroup> getAllGroups(int sizeLimit) {		
		return ldapEntryManager.findEntries(getDnForGroup(null), GluuGroup.class, null, 0, sizeLimit);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getVisibilityTypes()
	 */
	@Override
	public GluuGroupVisibility[] getVisibilityTypes() throws Exception {
		return GluuGroupVisibility.values();
	}

	/**
	 * Generate new inum for group
	 * 
	 * @return New inum for group
	 */
	private String generateInumForNewGroupImpl() throws Exception {
		String orgInum = organizationService.getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_GROUP_OBJECTTYPE + OxTrustConstants.inumDelimiter
				+ INumGenerator.generate(2);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getAllGroupsList()
	 */

	@Override
	public List<GluuGroup> getAllGroupsList() throws Exception {

		List<GluuGroup> result = ldapEntryManager
				.findEntries(getDnForGroup(null), GluuGroup.class, Filter.createPresenceFilter("inum"), 0, 10);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getGroupByDn(java.lang.String)
	 */

	@Override
	public GluuGroup getGroupByDn(String Dn) {
		GluuGroup result = ldapEntryManager.find(GluuGroup.class, Dn);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getGroupByIname(java.lang.String)
	 */
	@Override
	public GluuGroup getGroupByIname(String iname) throws Exception {
		GluuGroup group = new GluuGroup();
		group.setBaseDn(getDnForGroup(null));
		group.setIname(iname);

		List<GluuGroup> groups = ldapEntryManager.findEntries(group);

		if ((groups != null) && (groups.size() > 0)) {
			return groups.get(0);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IGroupService#getGroupByDisplayName(java.lang.String)
	 */
	@Override
	public GluuGroup getGroupByDisplayName(String DisplayName) throws Exception {
		GluuGroup group = new GluuGroup();
		group.setBaseDn(getDnForGroup(null));
		group.setDisplayName(DisplayName);

		List<GluuGroup> groups = ldapEntryManager.findEntries(group);

		if ((groups != null) && (groups.size() > 0)) {
			return groups.get(0);
		}

		return null;
	}

	/**
	 * Search groups by attributes present in object
	 *
	 * @param group
	 * @param sizeLimit
	 * @return
	 */
	@Override
	public List<GluuGroup> findGroups(GluuGroup group, int sizeLimit) {
		group.setBaseDn(getDnForGroup(null));
		return ldapEntryManager.findEntries(group, 0, sizeLimit);
	}

	/* (non-Javadoc)
	 * @see org.gluu.oxtrust.ldap.service.IPersonService#isMemberOrOwner(java.lang.String[], java.lang.String)
	 */
	@Override
	public boolean isMemberOrOwner(String[] groupDNs, String personDN) throws Exception {
		boolean result = false;
		if (ArrayHelper.isEmpty(groupDNs)) {
			return result;
		}

		for (String groupDN : groupDNs) {
			if (StringHelper.isEmpty(groupDN)) {
				continue;
			}

			result = isMemberOrOwner(groupDN, personDN);
			if (result) {
				break;
			}
		}

		return result;
	}

}
