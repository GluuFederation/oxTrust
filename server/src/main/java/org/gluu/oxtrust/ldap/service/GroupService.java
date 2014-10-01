/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuGroupVisibility;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Scope(ScopeType.STATELESS)
@Name("groupService")
@AutoCreate
public class GroupService implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -9167587377957719152L;

	@In
	private LdapEntryManager ldapEntryManager;

	@Logger
	private Log log;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	/**
	 * Add new group entry
	 * 
	 * @param group
	 *            Group
	 */
	public void addGroup(GluuGroup group) throws Exception {
		ldapEntryManager.persist(group);
	}

	/**
	 * Remove group entry
	 * 
	 * @param group
	 *            Group
	 */
	public void removeGroup(GluuGroup group) {
		if (group.getMembers() != null) {
			List<String> memberDNs = group.getMembers();
			for (String memberDN : memberDNs) {
				GluuCustomPerson person = PersonService.instance().getPersonByDn(memberDN);
				List<String> groupDNs = person.getMemberOf();
				List<String> updatedGroupDNs = new ArrayList<String>();
				updatedGroupDNs.addAll(groupDNs);
				updatedGroupDNs.remove(group.getDn());
				person.setMemberOf(updatedGroupDNs);
				PersonService.instance().updatePerson(person);
			}
		}

		ldapEntryManager.remove(group);
		// clear references in gluuPerson entries
	}

	/**
	 * Get all groups
	 * 
	 * @return List of groups
	 */
	public List<GluuGroup> getAllGroups() {
		return ldapEntryManager.findEntries(getDnForGroup(null), GluuGroup.class, null);
	}

	/**
	 * Check if person is a member or owner of specified group
	 * 
	 * @param groupDN
	 *            Group DN
	 * @param personDN
	 *            Person DN
	 * @return True if person is a member or owner of specified group
	 */
	public boolean isMemberOrOwner(String groupDN, String personDN) {
		Filter ownerFilter = Filter.createEqualityFilter(OxTrustConstants.owner, personDN);
		Filter memberFilter = Filter.createEqualityFilter(OxTrustConstants.member, personDN);
		Filter searchFilter = Filter.createORFilter(ownerFilter, memberFilter);

		boolean isMemberOrOwner = false;
		try {
			isMemberOrOwner = ldapEntryManager.findEntries(groupDN, GluuGroup.class, searchFilter, 1).size() > 0;

		} catch (EntryPersistenceException ex) {
			log.error("Failed to determine if person '{0}' memeber or owner of group '{1}'", ex, personDN, groupDN);
		}

		return isMemberOrOwner;
	}

	/**
	 * Get group by inum
	 * 
	 * @param inum
	 *            Group Inum
	 * @return Group
	 */
	public GluuGroup getGroupByInum(String inum) {
		GluuGroup result = null;
		try{
			result = ldapEntryManager.find(GluuGroup.class, getDnForGroup(inum));
		}catch(Exception e){
			log.error("Failed to find group by Inum " + inum, e);
		}
		return result;

	}

	/**
	 * Build DN string for group
	 * 
	 * @param inum
	 *            Group Inum
	 * @return DN string for specified group or DN for groups branch if inum is
	 *         null
	 * @throws Exception
	 */
	public String getDnForGroup(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=groups,%s", orgDn);
		}

		return String.format("inum=%s,ou=groups,%s", inum, orgDn);
	}

	/**
	 * Update group entry
	 * 
	 * @param group
	 *            Group
	 */
	public void updateGroup(GluuGroup group) throws Exception {
		ldapEntryManager.merge(group);

	}

	public int countGroups() {
		GluuGroup gluuGroup = new GluuGroup();
		gluuGroup.setBaseDn(getDnForGroup(null));

		return ldapEntryManager.countEntries(gluuGroup);
	}

	/**
	 * Generate new inum for group
	 * 
	 * @return New inum for group
	 * @throws Exception
	 */
	public String generateInumForNewGroup() throws Exception {
		GluuGroup group = new GluuGroup();
		String newInum = null;
		do {
			newInum = generateInumForNewGroupImpl();
			String newDn = getDnForGroup(newInum);
			group.setDn(newDn);
		} while (ldapEntryManager.contains(group));

		return newInum;
	}

	/**
	 * Generate new iname for group
	 * 
	 * @return New iname for group
	 */
	public String generateInameForNewGroup(String name) throws Exception {
		return String.format("%s*group*%s", applicationConfiguration.getOrgIname(), name);
	}

	/**
	 * Search groups by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of groups
	 */
	public List<GluuGroup> searchGroups(String pattern, int sizeLimit) throws Exception {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);

		List<GluuGroup> result = ldapEntryManager.findEntries(getDnForGroup(null), GluuGroup.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Get all available visibility types
	 * 
	 * @return Array of visibility types
	 */
	public GluuGroupVisibility[] getVisibilityTypes() throws Exception {
		return GluuGroupVisibility.values();
	}

	/**
	 * Generate new inum for group
	 * 
	 * @return New inum for group
	 */
	private String generateInumForNewGroupImpl() throws Exception {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_GROUP_OBJECTTYPE + OxTrustConstants.inumDelimiter
				+ INumGenerator.generate(2);
	}

	/**
	 * returns a list of all groups
	 * 
	 * @return list of groups
	 */

	public List<GluuGroup> getAllGroupsList() throws Exception {

		List<GluuGroup> result = ldapEntryManager
				.findEntries(getDnForGroup(null), GluuGroup.class, Filter.createPresenceFilter("inum"), 10);

		return result;
	}

	/**
	 * returns GluuGroup by Dn
	 * 
	 * @return GluuGroup
	 */

	public GluuGroup getGroupByDn(String Dn) {
		GluuGroup result = ldapEntryManager.find(GluuGroup.class, Dn);

		return result;
	}

	/**
	 * Get GroupService instance
	 * 
	 * @return GroupService instance
	 */
	public static GroupService instance() {
		return (GroupService) Component.getInstance(GroupService.class);
	}

	/**
	 * Get Group by iname
	 * 
	 * @param iname
	 * @return Group
	 */
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

	/**
	 * Get group by DisplayName
	 * 
	 * @param DisplayName
	 * @return group
	 */
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

}
