/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.model.TrustContact;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.service.TrustService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.XmlService;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;

@ConversationScoped
@Named("trustContactsAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class TrustContactsAction implements Serializable {

	private static final long serialVersionUID = -1032167044333943680L;
	@Inject
	XmlService xmlService;
	@Inject
	private TrustService trustService;

	private List<TrustContact> contacts;

	private GluuSAMLTrustRelationship trustRelationship;

	public String initContacts(GluuSAMLTrustRelationship trustRelationship) {
		if (contacts != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		// GluuSAMLTrustRelationship trustRelationship =
		// trustService.getRelationshipByInum(inum);
		this.trustRelationship = trustRelationship;
		contacts = trustService.getContacts(trustRelationship);
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<TrustContact> getTrustContacts() {
		return contacts;
	}

	public void removeContact(TrustContact contact) {
		contacts.remove(contact);
	}

	public void addEmptyContact() {
		// Util.removeDuplicateWithOrder(contacts);
		contacts.add(new TrustContact());
	}

	public void saveContacts() {
		removeEmptyContacts();
		Util.removeDuplicateWithOrder(contacts);
		trustService.saveContacts(trustRelationship, contacts);

	}

	private void removeEmptyContacts() {
		TrustContact emptyContact = new TrustContact();
		emptyContact.setMail("");
		emptyContact.setName("");
		emptyContact.setPhone("");
		emptyContact.setTitle("");
		List<TrustContact> trustContacts = new ArrayList<TrustContact>(contacts);
		for (TrustContact contact : trustContacts) {
			if (contact.equals(emptyContact)) {
				contacts.remove(contact);
			}
		}
	}
}
