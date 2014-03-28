package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xdi.util.Util;
import org.xdi.model.TrustContact;
import org.xdi.service.XmlService;

@Scope(ScopeType.CONVERSATION)
@Name("trustContactsAction")
public class TrustContactsAction implements Serializable {

	private static final long serialVersionUID = -1032167044333943680L;
	@In
	XmlService xmlService;
	@In
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
