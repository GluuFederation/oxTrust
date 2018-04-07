package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.slf4j.Logger;
import org.xdi.util.StringHelper;

import javax.ejb.Stateless;

@Stateless
@Named
public class MemberService implements Serializable {

	private static final long serialVersionUID = -3545641785714134933L;

	@Inject
	private GroupService groupService;

	@Inject
	private PersonService personService;
	@Inject
	private Logger logger;

	public void removePerson(GluuCustomPerson person) {
		// TODO: Do we realy need to remove group if owner is removed?
		List<GluuGroup> groups = groupService.getAllGroups();
		for (GluuGroup group : groups) {
			if (StringHelper.equalsIgnoreCase(group.getOwner(), person.getDn())) {
				groupService.removeGroup(group);
			}
		}
		// Remove person from associated groups
		removePersonFromGroups(person);
		// Remove person
		personService.removePerson(person);
	}

	private void removePersonFromGroups(GluuCustomPerson person) {
		logger.debug("**************************");
		logger.debug("Removing person from associated group before deletion");
		String currentPersonDn = personService.getDnForPerson(person.getInum());
		// Remove person from associated groups
		List<String> associatedGroupsDn = person.getMemberOf();
		for (String groupDn : associatedGroupsDn) {
			String uncompleteinum = groupDn.split(",")[0];
			String inum = uncompleteinum.split("=")[1];
			GluuGroup group = groupService.getGroupByInum(inum);
			List<String> members = new ArrayList<String>(group.getMembers());
			members.remove(currentPersonDn);
			group.setMembers(members);
			try {
				groupService.updateGroup(group);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.debug("Done");
	}

}
