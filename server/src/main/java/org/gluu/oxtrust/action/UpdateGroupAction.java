/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.model.DisplayNameEntry;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update group form.
 * 
 * @author Yuriy Movchan Date: 11.08.2010
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateGroupAction")
@Restrict("#{identity.loggedIn}")
public class UpdateGroupAction implements Serializable {

	private static final long serialVersionUID = 572441515451149801L;

	@Logger
	private Log log;

	private String inum;
	private boolean update;

	private GluuGroup group;

	private List<DisplayNameEntry> members;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of search string should be between 2 and 30")
	private String searchAvailableMemberPattern;

	private String oldSearchAvailableMemberPattern;

	private List<GluuCustomPerson> availableMembers;

	@In
	protected GluuCustomPerson currentPerson;

	@In
	private GroupService groupService;

	@In
	private LookupService lookupService;

	@In
	private PersonService personService;

	@In
	private FacesMessages facesMessages;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String add() throws Exception {
		if (this.group != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.group = new GluuGroup();
		this.group.setOwner(currentPerson.getDn());
		this.group.setOrganization(OrganizationService.instance().getOrganization().getDn());

		try {
			this.members = getMemberDisplayNameEntiries();
		} catch (LdapMappingException ex) {
			log.error("Failed to load person display names", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String update() throws Exception {
		if (this.group != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;
		log.info("this.update : " + this.update);
		try {
			log.info("inum : " + inum);
			this.group = groupService.getGroupByInum(inum);
		} catch (LdapMappingException ex) {
			log.error("Failed to find group {0}", ex, inum);

		}

		if (this.group == null) {
			log.info("Group is null ");
			return OxTrustConstants.RESULT_FAILURE;
		}

		try {
			this.members = getMemberDisplayNameEntiries();
		} catch (LdapMappingException ex) {
			log.error("Failed to load person display names", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}
		log.info("returning Success");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('group', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String save() throws Exception {
		List<DisplayNameEntry> oldMembers = null;
		try {
			oldMembers = getMemberDisplayNameEntiries();
		} catch (LdapMappingException ex) {
			log.info("error getting oldmembers");
			log.error("Failed to load person display names", ex);

			facesMessages.add(Severity.ERROR, "Failed to update group");
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateMembers();
		if (update) {
			// Update group
			try {
				groupService.updateGroup(this.group);
				updatePersons(oldMembers, this.members);
			} catch (LdapMappingException ex) {

				log.info("error updating group ", ex);
				log.error("Failed to update group {0}", ex, this.inum);

				facesMessages.add(Severity.ERROR, "Failed to update group");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			this.inum = groupService.generateInumForNewGroup();
			String dn = groupService.getDnForGroup(this.inum);

			// Save group
			this.group.setDn(dn);
			this.group.setInum(this.inum);
			try {
				groupService.addGroup(this.group);
				updatePersons(oldMembers, this.members);
			} catch (LdapMappingException ex) {
				log.info("error saving group ");
				log.error("Failed to add new group {0}", ex, this.group.getInum());

				facesMessages.add(Severity.ERROR, "Failed to add new group");
				return OxTrustConstants.RESULT_FAILURE;

			}

			this.update = true;
		}
		log.info(" returning success updating or saving group");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String delete() throws Exception {
		if (update) {
			// Remove group
			try {
				groupService.removeGroup(this.group);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove group {0}", ex, this.group.getInum());

			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	private List<DisplayNameEntry> getMemberDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(personService.getDnForPerson(null), this.group.getMembers());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public void addMember(GluuCustomPerson person) {
		DisplayNameEntry member = new DisplayNameEntry(person.getDn(), person.getInum(), person.getDisplayName());
		this.members.add(member);
	}

	public void removeMember(String inum) throws Exception {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeMemberInum = personService.getDnForPerson(inum);

		for (Iterator<DisplayNameEntry> iterator = this.members.iterator(); iterator.hasNext();) {
			DisplayNameEntry member = iterator.next();
			if (removeMemberInum.equals(member.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	public String getSearchAvailableMemberPattern() {
		return this.searchAvailableMemberPattern;
	}

	public void setSearchAvailableMemberPattern(String searchAvailableMemberPattern) {
		this.searchAvailableMemberPattern = searchAvailableMemberPattern;
	}

	public List<GluuCustomPerson> getAvailableMembers() {
		return this.availableMembers;
	}

	public void searchAvailableMembers() {
		if (Util.equals(this.oldSearchAvailableMemberPattern, this.searchAvailableMemberPattern)) {
			return;
		}

		try {
			this.availableMembers = personService.searchPersons(this.searchAvailableMemberPattern, OxTrustConstants.searchPersonsSizeLimit);
			this.oldSearchAvailableMemberPattern = this.searchAvailableMemberPattern;
			selectAddedMembers();
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
		}
	}

	public void selectAddedMembers() {
		if (this.availableMembers == null) {
			return;
		}

		Set<String> addedMemberInums = new HashSet<String>();
		for (DisplayNameEntry member : members) {
			addedMemberInums.add(member.getInum());
		}

		for (GluuCustomPerson person : this.availableMembers) {
			person.setSelected(addedMemberInums.contains(person.getInum()));
		}
	}

	public void acceptSelectMembers() {
		if (this.availableMembers == null) {
			return;
		}

		Set<String> addedMemberInums = new HashSet<String>();
		for (DisplayNameEntry member : members) {
			addedMemberInums.add(member.getInum());
		}

		for (GluuCustomPerson person : this.availableMembers) {
			if (person.isSelected() && !addedMemberInums.contains(person.getInum())) {
				addMember(person);
			}
		}
	}

	public void cancelSelectMembers() {
	}

	private void updateMembers() {
		List<String> members = new ArrayList<String>();
		this.group.setMembers(members);

		for (DisplayNameEntry member : this.members) {
			members.add(member.getDn());
		}
	}

	private void updatePersons(List<DisplayNameEntry> oldMembers, List<DisplayNameEntry> newMembers) throws Exception {
		log.debug("Old members: {0}", oldMembers);
		log.debug("New members: {0}", newMembers);

		String groupDn = this.group.getDn();

		GluuOrganization organization = OrganizationService.instance().getOrganization();
		String organizationGroups[] = { organization.getManagerGroup(), organization.getOwnerGroup() };

		// Convert members to array of DNs
		String[] oldMemberDns = convertToDNsArray(oldMembers);
		String[] newMemberDns = convertToDNsArray(newMembers);

		Arrays.sort(oldMemberDns);
		Arrays.sort(newMemberDns);

		boolean[] retainOldMembers = new boolean[oldMemberDns.length];
		Arrays.fill(retainOldMembers, false);

		List<String> addedMembers = new ArrayList<String>();
		List<String> removedMembers = new ArrayList<String>();
		List<String> existingMembers = new ArrayList<String>();

		// Add new values
		for (String value : newMemberDns) {
			int idx = Arrays.binarySearch(oldMemberDns, value);
			if (idx >= 0) {
				// Old members array contains member. Retain member
				retainOldMembers[idx] = true;
			} else {
				// This is new member
				addedMembers.add(value);
			}
		}

		// Remove members which we don't have in new members
		for (int i = 0; i < oldMemberDns.length; i++) {
			if (retainOldMembers[i]) {
				existingMembers.add(oldMemberDns[i]);
			} else {
				removedMembers.add(oldMemberDns[i]);
			}
		}

		for (String dn : addedMembers) {
			GluuCustomPerson person = personService.getPersonByDn(dn);
			log.debug("Adding group {0} to person {1} memberOf", groupDn, person.getDisplayName());

			if (applicationConfiguration.isUpdateApplianceStatus()) {
				GluuBoolean slaManager = isSLAManager(organizationGroups, person);
				person.setSLAManager(slaManager);
			}

			List<String> personMemberOf = new ArrayList<String>(person.getMemberOf());
			personMemberOf.add(groupDn);
			person.setMemberOf(personMemberOf);

			personService.updatePerson(person);
			Events.instance().raiseEvent(OxTrustConstants.EVENT_PERSON_ADDED_TO_GROUP, person, groupDn);
		}

		for (String dn : removedMembers) {
			GluuCustomPerson person = personService.getPersonByDn(dn);
			log.debug("Removing group {0} from person {1} memberOf", groupDn, person.getDisplayName());

			if (applicationConfiguration.isUpdateApplianceStatus()) {
				GluuBoolean slaManager = isSLAManager(organizationGroups, person);
				person.setSLAManager(slaManager);
			}

			List<String> personMemberOf = new ArrayList<String>(person.getMemberOf());
			personMemberOf.remove(groupDn);
			person.setMemberOf(personMemberOf);

			personService.updatePerson(person);
			Events.instance().raiseEvent(OxTrustConstants.EVENT_PERSON_REMOVED_FROM_GROUP, person, groupDn);
		}

		if (applicationConfiguration.isUpdateApplianceStatus()) {
			// Update existing members if needed
			for (String dn : existingMembers) {
				GluuCustomPerson person = personService.getPersonByDn(dn);
				log.debug("Updating group {0} to person {1} memberOf", groupDn, person.getDisplayName());

				GluuBoolean slaManager = isSLAManager(organizationGroups, person);
				if (slaManager.equals(person.getSLAManager())) {
					continue;
				}
	
				person.setSLAManager(slaManager);
				personService.updatePerson(person);
			}
		}
	}

	private GluuBoolean isSLAManager(String[] organizationGroups, GluuCustomPerson person) throws Exception {
		return GluuBoolean.getByValue(String.valueOf(personService.isMemberOrOwner(organizationGroups, person.getDn())));
	}

	private String[] convertToDNsArray(List<DisplayNameEntry> members) {
		String[] memberDns = new String[members.size()];
		int i = 0;
		for (DisplayNameEntry member : members) {
			memberDns[i++] = member.getDn();
		}

		return memberDns;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public GluuGroup getGroup() {
		return group;
	}

	public List<DisplayNameEntry> getMembers() {
		return members;
	}

	public boolean isUpdate() {
		return update;
	}

}
