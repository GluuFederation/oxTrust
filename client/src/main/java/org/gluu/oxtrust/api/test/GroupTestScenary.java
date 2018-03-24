package org.gluu.oxtrust.api.test;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.persist.model.base.GluuStatus;

public class GroupTestScenary {

	private OxTrustClient client;
	private static final Logger logger = LogManager.getLogger(GroupTestScenary.class);

	public static class Builder {
		private OxTrustClient myClient;

		public Builder withOxTrustClient(OxTrustClient client) {
			this.myClient = client;
			return this;
		}

		public GroupTestScenary build() {
			GroupTestScenary groupTestScenary = new GroupTestScenary();
			groupTestScenary.setClient(this.myClient);
			return groupTestScenary;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private GroupTestScenary() {
	}

	private void setClient(OxTrustClient client) {
		this.client = client;
	}

	public void performAdd() {
		GluuGroup group = new GluuGroup();
		group.setDisplayName("Students");
		group.setDescription("Students group");
		group.setStatus(GluuStatus.ACTIVE);
		try {
			client.getGroupClient().add(group);
		} catch (OxTrustAPIException e) {
			logger.debug("", e);
		}
	}

	public void performFetchAll() {
		List<GluuGroup> groups = client.getGroupClient().list();
		logger.info("Group size:" + groups.size());
		if (!groups.isEmpty()) {
			logger.info("First group:" + groups.get(0).toString());
		}
	}

	public void performFetchById(String inum) {
		GluuGroup group = client.getGroupClient().getGroup(inum);
		if (group != null) {
			logger.info("Group found:" + group.toString());
		} else {
			logger.info("Not group with inum" + inum + "found");
		}

	}

	public void performDelete(String inum) {
		System.out.println("Deletion");
		client.getGroupClient().remove(inum);
		System.out.println("Deleted");
	}

}
