package org.gluu.oxtrust.api.test;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.persist.model.base.GluuStatus;

public class PeopleTestScenary {

	private OxTrustClient client;
	private static final Logger logger = LogManager.getLogger(GroupTestScenary.class);

	public static class Builder {
		private OxTrustClient myClient;

		public Builder withOxTrustClient(OxTrustClient client) {
			this.myClient = client;
			return this;
		}

		public PeopleTestScenary build() {
			PeopleTestScenary peopleTestScenary = new PeopleTestScenary();
			peopleTestScenary.setClient(this.myClient);
			return peopleTestScenary;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private PeopleTestScenary() {
	}

	private void setClient(OxTrustClient client) {
		this.client = client;
	}

	public void performAdd() {
		String name = "UserToBeAdded";
		GluuCustomPerson person = new GluuCustomPerson();
		person.setDisplayName(name);
		person.setCommonName(name);
		person.setMail(name + "@yahoo.fr");
		person.setUserPassword(name);
		person.setStatus(GluuStatus.ACTIVE);
		try {
			client.getPeopleClient().add(person);
		} catch (OxTrustAPIException e) {
			logger.debug("", e);
		}
	}

	public void performFetchAll() {
		List<GluuCustomPerson> people = client.getPeopleClient().list();
		logger.info("People size:" + people.size());
		if (!people.isEmpty()) {
			logger.info("First person:" + people.get(0).toString());
		}
	}

	public void performFetchById(String inum) {
		GluuGroup person = client.getGroupClient().getGroup(inum);
		if (person != null) {
			logger.info("People found:" + person.toString());
		} else {
			logger.info("Not person with inum" + inum + "found");
		}

	}

	public void performDelete(String inum) {
		System.out.println("Deletion");
		client.getPeopleClient().remove(inum);
		System.out.println("Deleted");
	}

}
