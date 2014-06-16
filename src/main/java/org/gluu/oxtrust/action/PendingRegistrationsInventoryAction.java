package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RegistrationLinkService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.xdi.ldap.model.GluuStatus;

/**
 * Action class for listing of registration links
 * @author Oleksiy Tataryn Date: 23.03.2014
 * 
 */
@Scope(ScopeType.CONVERSATION)
@Name("pendingRegistrationsInventoryAction")
@Restrict("#{identity.loggedIn}")
public @Data class PendingRegistrationsInventoryAction implements Serializable {

	private static final long serialVersionUID = 3337947202256952024L;

	@In
	private RegistrationLinkService registrationLinkService;
	
	@In 
	private Credentials credentials;
	
	private String currentPerson;
	
	@In 
	private PersonService personService;

	@Logger
	private Log log;

	private List<GluuCustomPerson> pendingRegistrations;

	public String start() {
		if (pendingRegistrations != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		search();

		
		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * 
	 */
	private void search() {
		pendingRegistrations = new ArrayList<GluuCustomPerson>();
		List<OxLink> links = registrationLinkService.getAllLinks();
		for (OxLink link: links){
			if(link.getGuid() != null && link.getLinkModerated()){
				List<String> moderators = link.getLinkModerators();
				GluuCustomPerson currentPerson = personService.getPersonByUid(credentials.getUsername());
				if(moderators != null && moderators.contains(currentPerson.getUid()) && link.getLinkPending() != null) {
					for(String inum: link.getLinkPending()){
						pendingRegistrations.add(personService.getPersonByInum(inum));
					}
				}
			}
		}
	}

	public String approve() {
		GluuCustomPerson person = personService.getPersonByUid(currentPerson);
		person.setStatus(GluuStatus.ACTIVE);
		personService.updatePerson(person);
		List<String> remainingList = new ArrayList<String>();
		List<OxLink> links = registrationLinkService.getAllLinks();
		for (OxLink link: links){
			if(link.getGuid() != null && link.getLinkModerated()){
				List<String> moderators = link.getLinkModerators();
				GluuCustomPerson currentPerson = personService.getPersonByUid(credentials.getUsername());
				if(moderators != null && moderators.contains(currentPerson.getUid()) && link.getLinkPending() != null) {
					remainingList.addAll(link.getLinkPending());
					remainingList.remove(person.getInum());
					if(remainingList.isEmpty()){
						remainingList = null;
					}
					link.setLinkPending(remainingList);
					registrationLinkService.update(link);
				}
			}
		}
		search();
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String decline() {
		GluuCustomPerson person = personService.getPersonByUid(currentPerson);
		personService.removePerson(person);
		List<String> remainingList = new ArrayList<String>();
		List<OxLink> links = registrationLinkService.getAllLinks();
		for (OxLink link: links){
			if(link.getGuid() != null && link.getLinkModerated()){
				List<String> moderators = link.getLinkModerators();
				GluuCustomPerson currentPerson = personService.getPersonByUid(credentials.getUsername());
				if(moderators != null && moderators.contains(currentPerson.getUid()) && link.getLinkPending() != null) {
					remainingList.addAll(link.getLinkPending());
					remainingList.remove(person.getInum());
					if(remainingList.isEmpty()){
						remainingList = null;
					}
					link.setLinkPending(remainingList);
					registrationLinkService.update(link);
				}
			}
		}
		search();
		return OxTrustConstants.RESULT_SUCCESS;
	}
}
