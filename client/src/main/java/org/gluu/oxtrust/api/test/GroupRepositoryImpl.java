package org.gluu.oxtrust.api.test;

import java.util.List;

import org.gluu.oxtrust.api.GluuGroupApi;
import org.gluu.oxtrust.api.proxy.GroupApiProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class GroupRepositoryImpl extends BaseRepository {

	private String PATH = "restv1/api/groups";
	private ResteasyClient client;

	public GroupRepositoryImpl() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<GluuGroupApi> getAllGroups() {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getGroups(0);
	}

	public List<GluuGroupApi> getGroups(int size) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getGroups(size);
	}

	public List<GluuGroupApi> searchGroup(String pattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy groupClient = target.proxy(GroupApiProxy.class);
		return groupClient.getSearchGroups(pattern, size);
	}

	public GluuGroupApi createGroup(GluuGroupApi group) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		GluuGroupApi createdGroup = simpleClient.createGroup(group);
		return createdGroup;
	}

	public GluuGroupApi updateGroup(GluuGroupApi group) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		GluuGroupApi createdGroup = simpleClient.updateGroup(group);
		return createdGroup;
	}

	public void deleteGroup(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		simpleClient.deleteGroup(inum);
	}

	public GluuGroupApi getGroup(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		GroupApiProxy simpleClient = target.proxy(GroupApiProxy.class);
		return simpleClient.getGroup(inum);
	}

	public void testAll() {
		String inum = null;
		list();
		inum = add();
		update(inum);
		getById();
		search();
		delete(inum);
	}

	private void getById() {
		System.out.println("*******************");
		String inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0003!60B7";
		System.out.println("Getting Group " + inum);
		GluuGroupApi group = this.getGroup(inum);
		System.out.println("Group retrive:" + group);
	}

	private void delete(String id) {
		System.out.println("*******************");
		System.out.println("Deleting  Group " + id);
		if (id != null) {
			this.deleteGroup(id);
		}
	}

	private String add() {
		System.out.println("*******************");
		System.out.println("Add new Group");
		GluuGroupApi group = new GluuGroupApi();
		group.setDescription("UDM Students");
		group.setDisplayName("UDM");
		group.setOrganization("o=@!619C.061B.1A7E.5AF4!0001!4377.CD0A,o=gluu");
		group.setMembers(null);
		GluuGroupApi gluuGroupApi = this.createGroup(group);
		System.out.println("Group Added");
		return gluuGroupApi.getInum();
	}

	private String update(String inum) {
		System.out.println("*******************");
		System.out.println("Update Group");
		GluuGroupApi group = new GluuGroupApi();
		group.setInum(inum);
		group.setDisplayName("UDM");
		group.setDescription("Machine learning students");
		group.setOrganization("o=@!619C.061B.1A7E.5AF4!0001!4377.CD0A,o=gluu");
		GluuGroupApi gluuGroupApi = this.updateGroup(group);
		System.out.println("Group updated");
		return gluuGroupApi.getInum();
	}

	private void search() {
		System.out.println("Search groups");
		List<GluuGroupApi> values = this.searchGroup("gluu", 1);
		for (GluuGroupApi group : values) {
			System.out.println("INUM: " + group.getInum() + "GROUP NAME: " + group.getDisplayName() + " GROUP DESC: "
					+ group.getDescription());
		}
		System.out.println("*******************");
	}

	private void list() {
		System.out.println("List of groups");
		List<GluuGroupApi> values = this.getAllGroups();
		for (GluuGroupApi group : values) {
			System.out.println("INUM: " + group.getInum() + "GROUP NAME: " + group.getDisplayName() + " GROUP DESC: "
					+ group.getDescription());
		}
		System.out.println("*******************");
	}

}
