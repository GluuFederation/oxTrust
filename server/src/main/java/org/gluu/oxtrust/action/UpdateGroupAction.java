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

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.gluu.persist.model.base.GluuBoolean;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.DisplayNameEntry;
import org.xdi.service.LookupService;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update group form.
 * 
 * @author Yuriy Movchan Date: 11.08.2010
 */
@ConversationScoped
@Named("updateGroupAction")
@Secure("#{permissionService.hasPermission('group', 'access')}")
public class UpdateGroupAction implements Serializable {

	private static final long serialVersionUID = 572441515451149801L;

	@Inject
	private Logger log;

	private String inum;
	private boolean update;

	private GluuGroup group;

	private List<DisplayNameEntry> members;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of search string should be between 2 and 30")
	private String searchAvailableMemberPattern;

	private String oldSearchAvailableMemberPattern;

	private List<GluuCustomPerson> availableMembers;

	@Inject
	private Identity identity;
	
	@Inject
	private OrganizationService organizationService;

	@Inject
	private IGroupService groupService;

	@Inject
	private LookupService lookupService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private IPersonService personService;

	@Inject
	private AppConfiguration appConfiguration;

	public String add() throws Exception {
		if (this.group != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.group = new GluuGroup();
		this.group.setOwner(identity.getUser().getDn());
		this.group.setOrganization(organizationService.getOrganization().getDn());

		try {
			this.members = getMemberDisplayNameEntiries();
		} catch (BaseMappingException ex) {
			log.error("Failed to prepare lists", ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new group");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() throws Exception {
		if (this.group != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;
		log.debug("this.update : " + this.update);
		try {
			log.debug("inum : " + inum);
			this.group = groupService.getGroupByInum(inum);
		} catch (BaseMappingException ex) {
			log.error("Failed to find group {}", inum, ex);
		}

		if (this.group == null) {
			log.error("Failed to load group {}", inum);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find group");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		try {
			this.members = getMemberDisplayNameEntiries();
		} catch (BaseMappingException ex) {
			log.error("Failed to prepare lists", ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load group");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}
		log.debug("returning Success");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Group '#{updateGroupAction.group.displayName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New group not added");
		}

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws Exception {
		List<DisplayNameEntry> oldMembers = null;
		try {
			oldMembers = getMemberDisplayNameEntiries();
		} catch (BaseMappingException ex) {
			log.error("Failed to load person display names", ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update group");
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateMembers();
		if (update) {
			// Update group
			try {
				groupService.updateGroup(this.group);
				updatePersons(oldMembers, this.members);
			} catch (BaseMappingException ex) {
				log.error("Failed to update group {}", this.inum, ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update group '#{updateGroupAction.group.displayName}'");

				return OxTrustConstants.RESULT_FAILURE;
			}

			facesMessages.add(FacesMessage.SEVERITY_INFO, "Group '#{updateGroupAction.group.displayName}' updated successfully");
		} else {
			this.inum = groupService.generateInumForNewGroup();
			String dn = groupService.getDnForGroup(this.inum);

			// Save group
			this.group.setDn(dn);
			this.group.setInum(this.inum);
			try {
				groupService.addGroup(this.group);
				updatePersons(oldMembers, this.members);
			} catch (BaseMappingException ex) {
				log.error("Failed to add new group {}", this.group.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new group");

				return OxTrustConstants.RESULT_FAILURE;
			}

			facesMessages.add(FacesMessage.SEVERITY_INFO, "New group '#{updateGroupAction.group.displayName}' added successfully");
			conversationService.endConversation();

			this.update = true;
		}
		log.debug(" returning success updating or saving group");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String delete() throws Exception {
		if (update) {
			// Remove group
			try {
				groupService.removeGroup(this.group);

				facesMessages.add(FacesMessage.SEVERITY_INFO, "Group '#{updateGroupAction.group.displayName}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BaseMappingException ex) {
				log.error("Failed to remove group {}", this.group.getInum(), ex);
			}
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove group '#{updateGroupAction.group.displayName}'");

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
			this.availableMembers = personService.searchPersons(this.searchAvailableMemberPattern, OxTrustConstants.searchGroupSizeLimit);
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
		if (this.members == null || this.members.size() == 0) {
			this.group.setMembers(null);
			return;
		}

		List<String> tmpMembers = new ArrayList<String>();
		for (DisplayNameEntry member : this.members) {
			tmpMembers.add(member.getDn());
		}

		this.group.setMembers(tmpMembers);
	}

	private void updatePersons(List<DisplayNameEntry> oldMembers, List<DisplayNameEntry> newMembers) throws Exception {
		log.debug("Old members: {}", oldMembers);
		log.debug("New members: {}", newMembers);

		String groupDn = this.group.getDn();

		GluuOrganization organization = organizationService.getOrganization();
		String organizationGroups[] = { organization.getManagerGroup() };

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
			log.debug("Adding group {} to person {} memberOf", groupDn, person.getDisplayName());

			if (appConfiguration.isUpdateApplianceStatus()) {
				GluuBoolean slaManager = isSLAManager(organizationGroups, person);
				person.setSLAManager(slaManager);
			}

			List<String> personMemberOf = new ArrayList<String>(person.getMemberOf());
			personMemberOf.add(groupDn);
			person.setMemberOf(personMemberOf);

			personService.updatePerson(person);
		}

		for (String dn : removedMembers) {
			GluuCustomPerson person = personService.getPersonByDn(dn);
			log.debug("Removing group {} from person {} memberOf", groupDn, person.getDisplayName());

			if (appConfiguration.isUpdateApplianceStatus()) {
				GluuBoolean slaManager = isSLAManager(organizationGroups, person);
				person.setSLAManager(slaManager);
			}

			List<String> personMemberOf = new ArrayList<String>(person.getMemberOf());
			personMemberOf.remove(groupDn);
			person.setMemberOf(personMemberOf);

			personService.updatePerson(person);
		}

		if (appConfiguration.isUpdateApplianceStatus()) {
			// Update existing members if needed
			for (String dn : existingMembers) {
				GluuCustomPerson person = personService.getPersonByDn(dn);
				log.debug("Updating group {} to person {} memberOf", groupDn, person.getDisplayName());

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
		return GluuBoolean.getByValue(String.valueOf(groupService.isMemberOrOwner(organizationGroups, person.getDn())));
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
