package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
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

	public void removePerson(GluuCustomPerson person) {
		// TODO: Do we realy need to remove group if owner is removed?
		List<GluuGroup> groups = groupService.getAllGroups();
		for (GluuGroup group : groups) {
			if (StringHelper.equalsIgnoreCase(group.getOwner(), person.getDn())) {
				groupService.removeGroup(group);
			}
		}

		// Remove person
		personService.removePerson(person);
	}

}
