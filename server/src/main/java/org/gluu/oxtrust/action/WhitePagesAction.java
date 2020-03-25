/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuUserRole;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

/**
 * Action class for view white pages
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Named("whitePagesAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('profile', 'access')}")
public class WhitePagesAction implements Serializable {

	private static final long serialVersionUID = 6730313815008211305L;

	private List<String> tableAttributes;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private PersonService personService;

	private String tableState;

	private List<GluuCustomPerson> persons;
	private Set<Integer> selectedPersons;

	@PostConstruct
	public void init() {
		this.tableAttributes = Arrays.asList("cn", "photo1", "mail", "phone");
	}

	@Secure("#{permissionService.hasPermission(configurationService.configuration, 'whitePagesEnabled')}")
	public String start() {
		if (persons != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		return search();
	}

	@Secure("#{permissionService.hasPermission(configurationService.configuration, 'whitePagesEnabled')}")
	public String search() {
		try {
			GluuCustomPerson person = new GluuCustomPerson();
			person.setGluuAllowPublication("true");
			this.persons = personService.findPersons(person, 0);
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find persons white pages");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuCustomPerson> getPersons() {
		return persons;
	}

	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}

	public Set<Integer> getSelectedPersons() {
		return selectedPersons;
	}

	public void setSelectedPersons(Set<Integer> selectedPersons) {
		this.selectedPersons = selectedPersons;
	}

	public GluuCustomPerson getSelectedPerson() {
		if ((this.selectedPersons == null) || (this.selectedPersons.isEmpty())) {
			return null;
		} else {
			int index = this.selectedPersons.iterator().next();
			return this.persons.get(index);
		}
	}

	public List<GluuCustomAttribute> getReleasedAttributes(GluuCustomPerson person) {
		if (person == null) {
			return Arrays.asList(new GluuCustomAttribute[0]);
		}

		List<GluuCustomAttribute> releasedAttributes = new ArrayList<GluuCustomAttribute>();
		for (GluuCustomAttribute attribute : person.getCustomAttributes()) {
			List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
			GluuAttribute metadata = attributeService.getAttributeByName(attribute.getName(), attributes);
			if (metadata != null && metadata.isWhitePagesCanView() && !tableAttributes.contains(attribute.getName())) {
				attribute.setMetadata(metadata);
				releasedAttributes.add(attribute);
			}
		}

		return releasedAttributes;
	}

	public boolean released(GluuCustomPerson person, String attributeName) {
		return person.getGluuOptOuts() == null || !person.getGluuOptOuts().contains(attributeName);
	}

	public boolean canContact(GluuCustomPerson person) {
		return false;
	}
}
