package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

@Named
public class MemberService implements Serializable {

	private static final long serialVersionUID = -3545641785714134933L;

	@Inject
	private GroupService groupService;

	@Inject
	private PersonService personService;
	
	@Inject
	private Logger log;

	public void removePerson(GluuCustomPerson person) {
		// Remove groups where user is owner
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
		log.debug("Removing person from associated group before deletion");
        String pesonDn = person.getDn();
		// Remove person from associated groups
		List<String> associatedGroupsDn = person.getMemberOf();
		for (String groupDn : associatedGroupsDn) {
		    if (!groupService.contains(groupDn)) {
		        continue;
		    }

		    GluuGroup group = groupService.getGroupByDn(groupDn);

			List<String> members = new ArrayList<String>(group.getMembers());
			members.remove(pesonDn);
			group.setMembers(members);
			try {
				groupService.updateGroup(group);
			} catch (EntryPersistenceException ex) {
			    log.error("Failed to remove preson '{}' from group '{}'", ex);
			}
		}
		log.debug("All group updated");
	}

}
